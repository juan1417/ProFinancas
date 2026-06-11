import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';

/// Persists the user's saved cards (display data only — no PAN, no CVV) in
/// SharedPreferences. The actual card number is stored as the last 4
/// digits + the BIN; that's enough to render a "Visa **** 4821" tile and
/// is consistent with how the rest of the app displays cards.
///
/// In a real banking product you'd store the full PAN behind a token
/// from a PCI-compliant vault (Stripe Issuing, etc.) and never see the
/// raw number on the device. That's out of scope here.
class CardStorageService {
  CardStorageService._();
  static final CardStorageService _instance = CardStorageService._();
  static CardStorageService get instance => _instance;

  static const _key = 'profinancas.cards.v1';

  Future<List<StoredCard>> readAll() async {
    final prefs = await SharedPreferences.getInstance();
    final raw = prefs.getString(_key);
    if (raw == null || raw.isEmpty) return const [];
    try {
      final list = (jsonDecode(raw) as List).cast<Map<String, dynamic>>();
      return list.map(StoredCard.fromJson).toList();
    } catch (_) {
      // Corrupted payload — start fresh rather than crash the wallet.
      return const [];
    }
  }

  Future<void> writeAll(List<StoredCard> cards) async {
    final prefs = await SharedPreferences.getInstance();
    final raw = jsonEncode(cards.map((c) => c.toJson()).toList());
    await prefs.setString(_key, raw);
  }
}

class StoredCard {
  const StoredCard({
    required this.id,
    required this.brand,
    required this.bank,
    required this.last4,
    required this.expiry,
    required this.cardholderName,
    required this.color,
  });

  final String id;
  final String brand;
  final String bank;
  final String last4;
  final String expiry;
  final String cardholderName;
  final int color;

  factory StoredCard.fromJson(Map<String, dynamic> json) => StoredCard(
        id: json['id'] as String,
        brand: json['brand'] as String,
        bank: json['bank'] as String,
        last4: json['last4'] as String,
        expiry: json['expiry'] as String,
        cardholderName: json['cardholderName'] as String? ?? '',
        color: json['color'] as int,
      );

  Map<String, dynamic> toJson() => {
        'id': id,
        'brand': brand,
        'bank': bank,
        'last4': last4,
        'expiry': expiry,
        'cardholderName': cardholderName,
        'color': color,
      };
}
