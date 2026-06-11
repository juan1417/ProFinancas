import 'dart:io';
import 'package:camera/camera.dart';
import 'package:flutter/foundation.dart';
import 'package:google_mlkit_text_recognition/google_mlkit_text_recognition.dart';

/// Errors raised by the OCR pipeline. Surfaced to the UI as friendly
/// messages instead of a generic exception.
enum ScanFailure { noCamera, cameraBusy, imageUnreadable, noTextFound, noTotalFound, unknown }

class ScanException implements Exception {
  const ScanException(this.failure, [this.detail]);
  final ScanFailure failure;
  final String? detail;

  String get userMessage {
    switch (failure) {
      case ScanFailure.noCamera:
        return 'No camera found on this device. Try picking an image from the gallery.';
      case ScanFailure.cameraBusy:
        return 'The camera is busy. Wait a moment and try again.';
      case ScanFailure.imageUnreadable:
        return 'The image could not be read. Try a clearer, well-lit photo of the invoice.';
      case ScanFailure.noTextFound:
        return 'No text was detected in the image. Make sure the invoice is in focus and well lit.';
      case ScanFailure.noTotalFound:
        return 'A total amount could not be detected. You can still enter it manually.';
      case ScanFailure.unknown:
        return detail ?? 'Something went wrong while scanning. Please try again.';
    }
  }

  @override
  String toString() => 'ScanException($failure${detail != null ? ", $detail" : ""})';
}

class InvoiceScannerService {
  InvoiceScannerService._();
  static final InvoiceScannerService _instance = InvoiceScannerService._();
  static InvoiceScannerService get instance => _instance;

  CameraController? _cameraController;
  List<CameraDescription>? _cameras;
  bool _isInitialized = false;

  /// Reused across calls to avoid the cost of opening/closing the
  /// recognizer for every scan. Closed in [dispose].
  final TextRecognizer _textRecognizer = TextRecognizer();

  Future<bool> initialize() async {
    if (_isInitialized) return true;
    try {
      _cameras = await availableCameras();
      if (_cameras == null || _cameras!.isEmpty) return false;
      _cameraController = CameraController(
        _cameras!.first,
        ResolutionPreset.high, // high gives the OCR more pixels to chew on
        enableAudio: false,
      );
      await _cameraController!.initialize();
      _isInitialized = true;
      return true;
    } catch (e) {
      debugPrint('InvoiceScannerService.initialize failed: $e');
      return false;
    }
  }

  CameraController? get cameraController => _cameraController;

  Future<void> dispose() async {
    await _cameraController?.dispose();
    _cameraController = null;
    _isInitialized = false;
    await _textRecognizer.close();
  }

  /// Run OCR on a file and return a [ScannedInvoice] with everything the UI
  /// needs. Throws [ScanException] on failure so callers can show a specific
  /// message instead of a generic catch-all.
  Future<ScannedInvoice> scan(File imageFile) async {
    if (!await imageFile.exists()) {
      throw const ScanException(ScanFailure.imageUnreadable, 'file does not exist');
    }
    try {
      final inputImage = InputImage.fromFile(imageFile);
      final recognized = await _textRecognizer.processImage(inputImage);
      final text = recognized.text;
      if (text.trim().isEmpty) {
        throw const ScanException(ScanFailure.noTextFound);
      }
      final result = _extractTotals(text);
      if (result == null) {
        throw const ScanException(ScanFailure.noTotalFound);
      }
      return result;
    } on ScanException {
      rethrow;
    } catch (e) {
      debugPrint('InvoiceScannerService.scan error: $e');
      throw ScanException(ScanFailure.unknown, e.toString());
    }
  }

  Future<ScannedInvoice?> scanFromPath(String imagePath) =>
      scan(File(imagePath)).then((v) => v).catchError((Object e) {
        if (e is ScanException) throw e;
        throw ScanException(ScanFailure.unknown, e.toString());
      });

  // ── Parsing ───────────────────────────────────────────────────────────────

  /// What we hand back to the UI. We return the *best-guess* total plus the
  /// runner-up so the user can pick if the first one looks wrong.
  ScannedInvoice? _extractTotals(String text) {
    // Two passes:
    //   1. Lines that mention "total"/"amount"/"balance" near a number
    //      are weighted higher — these are the "explicit" candidates.
    //   2. Any currency-shaped number in the document is a fallback
    //      candidate. We later bias toward the line that says "total".
    final explicit = <double>[];
    final fallback = <double>[];

    // Regex 1: "total: 12.50" / "TOTAL $1,250.00" etc.
    final labeled = RegExp(
      r'(?:total|importe|monto|amount|grand\s*total|suma|total\s*a\s*pagar|net\s*amount|due|balance)'
      r'[:\s]*\$?\s*([\d.,]+)',
      caseSensitive: false,
    );
    for (final m in labeled.allMatches(text)) {
      final v = _parseAmount(m.group(1)!);
      if (v != null && v > 0) explicit.add(v);
    }

    // Regex 2: any number with exactly two decimals, prefixed with $ or
    // followed by a currency marker — typical for receipt line totals.
    final currencyShape = RegExp(r'\$\s*([\d.,]+)|\b([\d]{1,3}(?:[.,]\d{3})*[.,]\d{2})\b');
    for (final m in currencyShape.allMatches(text)) {
      final raw = m.group(1) ?? m.group(2);
      if (raw == null) continue;
      final v = _parseAmount(raw);
      if (v != null && v > 0) fallback.add(v);
    }

    if (explicit.isEmpty && fallback.isEmpty) return null;

    // The "best" total is the LARGEST explicit candidate. We prefer the
    // largest because the grand total of an invoice is always >= any line
    // item. If no explicit, fall back to the largest fallback.
    final primary = (explicit.isNotEmpty ? explicit : fallback).reduce(_max);

    // Runner-up = next largest different number, useful when OCR mis-reads
    // a 0 as 6 or something and the user wants the second option.
    final all = [...explicit, ...fallback]..sort((a, b) => b.compareTo(a));
    final differentRunners = all.where((v) => (v - primary).abs() > 0.01).toList();
    final runnerUp = differentRunners.isNotEmpty ? differentRunners.first : null;

    // Also pull the merchant guess: the first non-empty line of the OCR.
    // Not great, but enough for the "description" field.
    final firstLine = text
        .split(RegExp(r'[\r\n]+'))
        .map((l) => l.trim())
        .firstWhere((l) => l.isNotEmpty, orElse: () => '');

    return ScannedInvoice(
      total: primary,
      runnerUp: runnerUp,
      merchant: firstLine,
      rawText: text,
    );
  }

  /// Parse a string like "1.234,56" / "1,234.56" / "12.50" into a double.
  /// Uses a simple heuristic: if the LAST separator is a comma and there
  /// are exactly 2 digits after it, treat it as the decimal mark; else
  /// assume the period is the decimal mark and commas are thousands.
  double? _parseAmount(String raw) {
    var s = raw.trim();
    if (s.isEmpty) return null;
    // Drop thousands separators: keep only digits and the *last* separator.
    final lastComma = s.lastIndexOf(',');
    final lastDot = s.lastIndexOf('.');
    if (lastComma > lastDot) {
      // Comma is the decimal mark. Drop all dots (thousands) and replace
      // the decimal comma with a dot.
      s = s.replaceAll('.', '').replaceFirst(',', '.');
    } else {
      // Dot is the decimal mark. Just drop commas.
      s = s.replaceAll(',', '');
    }
    return double.tryParse(s);
  }

  double _max(double a, double b) => a > b ? a : b;
}

class ScannedInvoice {
  const ScannedInvoice({
    required this.total,
    required this.merchant,
    required this.rawText,
    this.runnerUp,
  });

  final double total;
  final double? runnerUp;
  final String merchant;
  final String rawText;
}
