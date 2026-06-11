class CardDetectorService {
  CardDetectorService._();

  static final CardDetectorService _instance = CardDetectorService._();
  static CardDetectorService get instance => _instance;

  /// Detect card type and bank from card number using BIN ranges
  ({String type, String bank})? detectCard(String cardNumber) {
    final cleaned = cardNumber.replaceAll(RegExp(r'\D'), '');
    if (cleaned.length < 4) return null;

    final firstDigit = cleaned[0];
    final first2 = cleaned.length >= 2 ? cleaned.substring(0, 2) : '';
    final first4 = cleaned.length >= 4 ? cleaned.substring(0, 4) : '';
    final first6 = cleaned.length >= 6 ? cleaned.substring(0, 6) : '';
    final first3 = cleaned.length >= 3 ? cleaned.substring(0, 3) : '';

    // Visa
    if (firstDigit == '4') {
      return (type: 'Visa', bank: _getVisaBank(first4));
    }

    // Mastercard (51-55, 2221-2720)
    if (first2.length >= 2) {
      final m2 = int.tryParse(first2);
      if (m2 != null && m2 >= 51 && m2 <= 55) {
        return (type: 'Mastercard', bank: _getMastercardBank(first6));
      }
      if (first4.length >= 4) {
        final m4 = int.tryParse(first4);
        if (m4 != null && m4 >= 2221 && m4 <= 2720) {
          return (type: 'Mastercard', bank: _getMastercardBank(first6));
        }
      }
    }

    // Amex (34, 37)
    if (first2 == '34' || first2 == '37') {
      return (type: 'Amex', bank: 'American Express');
    }

    // Discover (6011, 65, 644-649)
    if (first4 == '6011' || first2 == '65' ||
        (first3.length >= 3 && int.tryParse(first3) != null &&
         int.parse(first3) >= 644 && int.parse(first3) <= 649)) {
      return (type: 'Discover', bank: 'Discover');
    }

    // UnionPay (62)
    if (first2.startsWith('62')) {
      return (type: 'UnionPay', bank: 'UnionPay');
    }

    return null;
  }

  String _getVisaBank(String bin) {
    // Common Visa BIN ranges by bank (simplified)
    return 'Visa';
  }

  String _getMastercardBank(String bin) {
    // Common Mastercard issuers (simplified)
    if (bin.startsWith('5210')) return 'Mastercard Standard';
    if (bin.startsWith('5300')) return 'Mastercard Standard';
    return 'Mastercard';
  }
}
