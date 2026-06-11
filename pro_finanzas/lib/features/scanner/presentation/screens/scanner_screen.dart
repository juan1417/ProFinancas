import 'dart:io';
import 'package:flutter/material.dart';
import 'package:camera/camera.dart';
import 'package:image_picker/image_picker.dart';
import 'package:provider/provider.dart';

import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_text_styles.dart';
import '../../../../core/utils/currency_formatter.dart';
import '../../../../core/widgets/app_card.dart';
import '../../../../core/widgets/pro_app_bar.dart';
import '../../../../core/services/invoice_scanner_service.dart';
import '../../../categories/presentation/providers/category_provider.dart';
import '../../../transactions/domain/entities/category.dart';
import '../../../transactions/presentation/providers/transaction_provider.dart';

class ScannerScreen extends StatefulWidget {
  const ScannerScreen({super.key});

  @override
  State<ScannerScreen> createState() => _ScannerScreenState();
}

class _ScannerScreenState extends State<ScannerScreen> {
  CameraController? _cameraController;
  bool _isInitialized = false;
  bool _isScanning = false;
  bool _scanned = false;
  ScannedInvoice? _invoice;
  File? _imageFile;
  String? _scanError;
  final ImagePicker _picker = ImagePicker();

  @override
  void initState() {
    super.initState();
    _initCamera();
  }

  Future<void> _initCamera() async {
    try {
      final service = InvoiceScannerService.instance;
      final ok = await service.initialize();
      if (ok && mounted) {
        setState(() {
          _cameraController = service.cameraController;
          _isInitialized = true;
        });
      }
    } catch (e) {
      // No camera available — keep the "Use gallery instead" hint visible.
    }
  }

  @override
  void dispose() {
    _cameraController?.dispose();
    super.dispose();
  }

  Future<void> _captureAndScan() async {
    if (_cameraController == null || _isScanning) return;
    await _scanImage(fromCamera: true);
  }

  Future<void> _pickFromGallery() async {
    if (_isScanning) return;
    await _scanImage(fromCamera: false);
  }

  Future<void> _scanImage({required bool fromCamera}) async {
    setState(() {
      _isScanning = true;
      _scanError = null;
      _scanned = false;
    });

    XFile? image;
    try {
      image = fromCamera
          ? await _cameraController!.takePicture()
          : await _picker.pickImage(source: ImageSource.gallery);
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _isScanning = false;
        _scanError = e.toString();
      });
      return;
    }

    if (image == null) {
      setState(() => _isScanning = false);
      return; // user cancelled the picker
    }

    try {
      final invoice =
          await InvoiceScannerService.instance.scanFromPath(image.path);
      if (!mounted) return;
      setState(() {
        _imageFile = File(image!.path);
        _invoice = invoice;
        _scanned = true;
        _isScanning = false;
        _scanError = null;
      });
    } on ScanException catch (e) {
      if (!mounted) return;
      setState(() {
        _isScanning = false;
        _scanned = true; // we still want to show the result card with the error
        _imageFile = File(image!.path);
        _invoice = null;
        _scanError = e.userMessage;
      });
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _isScanning = false;
        _scanned = true;
        _imageFile = File(image!.path);
        _scanError = 'Unexpected error: $e';
      });
    }
  }

  /// Ask the user to pick an expense category, then save the scanned total
  /// as a transaction. Categories come from [CategoryProvider] (not
  /// from [TransactionProvider], which doesn't own them).
  Future<void> _saveAsTransaction() async {
    if (_invoice == null) return;
    final catProvider = context.read<CategoryProvider>();
    if (catProvider.all.isEmpty) {
      await catProvider.loadAll(isActive: true);
    }
    if (!mounted) return;

    final expenseCategories = catProvider.expenseCategories;

    if (expenseCategories.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text(
            'No expense categories found. Create one from the Wallet screen first.',
          ),
        ),
      );
      return;
    }

    final selected = await showModalBottomSheet<Category>(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (_) => _CategoryPickerSheet(categories: expenseCategories),
    );
    if (selected == null || !mounted) return;

    final description = _invoice!.merchant.isNotEmpty
        ? _invoice!.merchant
        : 'Scanned invoice';

    final txProvider = context.read<TransactionProvider>();
    final ok = await txProvider.addTransaction(
      categoryId: selected.id,
      type: 'EXPENSE',
      amount: _invoice!.total,
      description: description,
      transactionDate: DateTime.now(),
    );
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(ok
            ? 'Saved ${CurrencyFormatter.format(_invoice!.total)} as ${selected.name}'
            : (txProvider.error ?? 'Could not save the transaction.')),
        backgroundColor: ok ? AppColors.income : AppColors.expense,
      ),
    );
    if (ok) setState(() => _scanned = false);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: const ProAppBar(title: 'Invoice Scanner'),
      body: ListView(
        padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
        children: [
          // Camera / gallery viewfinder
          Container(
            height: 260,
            decoration: BoxDecoration(
              color: AppColors.black,
              borderRadius: BorderRadius.circular(20),
            ),
            child: ClipRRect(
              borderRadius: BorderRadius.circular(20),
              child: _isInitialized && _cameraController != null
                  ? CameraPreview(_cameraController!)
                  : const Center(
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Icon(Icons.camera_alt,
                              color: Colors.white54, size: 56),
                          SizedBox(height: 12),
                          Text('Camera not available',
                              style: TextStyle(color: Colors.white70)),
                          SizedBox(height: 4),
                          Text('Use the gallery button below',
                              style: TextStyle(
                                  color: Colors.white38, fontSize: 12)),
                        ],
                      ),
                    ),
            ),
          ),
          const SizedBox(height: 16),

          // Action row
          Row(
            children: [
              Expanded(
                child: FilledButton.icon(
                  style: FilledButton.styleFrom(
                    backgroundColor: AppColors.primary,
                    minimumSize: const Size(0, 52),
                    shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(12)),
                  ),
                  onPressed:
                      _isInitialized && !_isScanning ? _captureAndScan : null,
                  icon: _isScanning
                      ? const SizedBox(
                          width: 18,
                          height: 18,
                          child: CircularProgressIndicator(
                              strokeWidth: 2, color: Colors.white),
                        )
                      : const Icon(Icons.camera_alt, size: 18),
                  label: Text(_isScanning ? 'Scanning...' : 'Scan Invoice'),
                ),
              ),
              const SizedBox(width: 12),
              OutlinedButton(
                style: OutlinedButton.styleFrom(
                  minimumSize: const Size(52, 52),
                  padding: EdgeInsets.zero,
                  side: const BorderSide(color: AppColors.neutral400),
                  shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(12)),
                ),
                onPressed: _isScanning ? null : _pickFromGallery,
                child: const Icon(Icons.photo_library_outlined,
                    color: AppColors.black, size: 22),
              ),
            ],
          ),
          const SizedBox(height: 8),
          Text(
            'Tap "Scan Invoice" to use the camera, or pick one from the gallery.',
            style: AppTextStyles.bodySecondary,
            textAlign: TextAlign.center,
          ),

          if (_scanned) ...[
            const SizedBox(height: 24),
            Text('EXTRACTED DATA', style: AppTextStyles.label),
            const SizedBox(height: 8),
            _ResultCard(
              invoice: _invoice,
              imageFile: _imageFile,
              errorMessage: _scanError,
              onSave: _invoice == null ? null : _saveAsTransaction,
              onDismiss: () => setState(() {
                _scanned = false;
                _invoice = null;
                _imageFile = null;
                _scanError = null;
              }),
            ),
          ],
          const SizedBox(height: 24),
        ],
      ),
    );
  }
}

// ── Result card ──────────────────────────────────────────────────────────────
class _ResultCard extends StatelessWidget {
  const _ResultCard({
    required this.invoice,
    required this.imageFile,
    required this.errorMessage,
    required this.onSave,
    required this.onDismiss,
  });

  final ScannedInvoice? invoice;
  final File? imageFile;
  final String? errorMessage;
  final VoidCallback? onSave;
  final VoidCallback onDismiss;

  @override
  Widget build(BuildContext context) {
    final hasResult = invoice != null;
    return AppCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          if (imageFile != null) ...[
            ClipRRect(
              borderRadius: BorderRadius.circular(12),
              child: Image.file(
                imageFile!,
                height: 140,
                fit: BoxFit.cover,
                errorBuilder: (_, __, ___) => Container(
                  height: 140,
                  color: AppColors.neutral100,
                  alignment: Alignment.center,
                  child: const Icon(Icons.broken_image_outlined,
                      color: AppColors.neutral500),
                ),
              ),
            ),
            const SizedBox(height: 16),
          ],
          if (errorMessage != null) ...[
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: AppColors.error.withValues(alpha: 0.10),
                borderRadius: BorderRadius.circular(8),
                border: Border.all(
                    color: AppColors.error.withValues(alpha: 0.30)),
              ),
              child: Row(
                children: [
                  const Icon(Icons.error_outline,
                      color: AppColors.error, size: 20),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      errorMessage!,
                      style: AppTextStyles.bodyMedium
                          .copyWith(color: AppColors.error),
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 12),
          ],
          if (hasResult) ...[
            _DataRow('Merchant', invoice!.merchant.isEmpty ? '—' : invoice!.merchant),
            const Divider(height: 24),
            _DataRow('Total detected',
                CurrencyFormatter.format(invoice!.total)),
            if (invoice!.runnerUp != null) ...[
              const Divider(height: 24),
              _DataRow('Alternative',
                  CurrencyFormatter.format(invoice!.runnerUp!)),
            ],
            const SizedBox(height: 20),
            Row(
              children: [
                Expanded(
                  child: FilledButton(
                    style: FilledButton.styleFrom(
                      backgroundColor: AppColors.income,
                      minimumSize: const Size(0, 48),
                      shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(10)),
                    ),
                    onPressed: onSave,
                    child: const Text('Save as expense'),
                  ),
                ),
                const SizedBox(width: 12),
                OutlinedButton(
                  style: OutlinedButton.styleFrom(
                    minimumSize: const Size(48, 48),
                    padding: EdgeInsets.zero,
                    side: const BorderSide(color: AppColors.neutral400),
                    shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(10)),
                  ),
                  onPressed: onDismiss,
                  child: const Icon(Icons.close, color: AppColors.neutral700),
                ),
              ],
            ),
          ] else
            // No invoice but we still want the user to be able to dismiss.
            TextButton(onPressed: onDismiss, child: const Text('Dismiss')),
        ],
      ),
    );
  }
}

class _DataRow extends StatelessWidget {
  const _DataRow(this.label, this.value);
  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text(label, style: AppTextStyles.bodySecondary),
        Flexible(
          child: Text(
            value,
            textAlign: TextAlign.end,
            style: AppTextStyles.bodyMedium,
            maxLines: 2,
            overflow: TextOverflow.ellipsis,
          ),
        ),
      ],
    );
  }
}

// ── Category picker (used by "Save as expense") ──────────────────────────────
class _CategoryPickerSheet extends StatelessWidget {
  const _CategoryPickerSheet({required this.categories});
  final List<Category> categories;

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.all(16),
      padding: EdgeInsets.only(
        left: 24,
        right: 24,
        top: 24,
        bottom: MediaQuery.of(context).viewInsets.bottom + 24,
      ),
      decoration: BoxDecoration(
        color: AppColors.white,
        borderRadius: BorderRadius.circular(20),
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text('Pick a category', style: AppTextStyles.headlineSmall),
              IconButton(
                onPressed: () => Navigator.pop(context),
                icon: const Icon(Icons.close),
              ),
            ],
          ),
          const SizedBox(height: 8),
          Text(
            'Choose the expense category for this invoice.',
            style: AppTextStyles.bodySecondary,
          ),
          const SizedBox(height: 16),
          Flexible(
            child: ListView.separated(
              shrinkWrap: true,
              itemCount: categories.length,
              separatorBuilder: (_, __) => const Divider(height: 1),
              itemBuilder: (_, i) {
                final c = categories[i];
                return ListTile(
                  contentPadding: EdgeInsets.zero,
                  title: Text(c.name, style: AppTextStyles.bodyMedium),
                  trailing: const Icon(Icons.chevron_right,
                      color: AppColors.neutral500),
                  onTap: () => Navigator.pop(context, c),
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}
