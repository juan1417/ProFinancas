import 'package:intl/intl.dart';

class CurrencyFormatter {
  CurrencyFormatter._();

  static final NumberFormat _formatter = NumberFormat.currency(
    locale: 'es_CO',
    symbol: '\$',
    decimalDigits: 2,
  );

  static String format(double amount) => _formatter.format(amount);

  static String formatPositive(double amount) =>
      '+ ${_formatter.format(amount)}';

  static String formatNegative(double amount) =>
      '- ${_formatter.format(amount.abs())}';

  static String formatSigned(double amount) =>
      amount >= 0 ? formatPositive(amount) : formatNegative(amount);
}
