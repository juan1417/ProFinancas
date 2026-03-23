import 'package:intl/intl.dart';

class DateFormatter {
  DateFormatter._();

  static final DateFormat _short = DateFormat('dd/MM/yyyy');
  static final DateFormat _long = DateFormat("dd 'de' MMMM 'de' yyyy", 'es');
  static final DateFormat _withTime = DateFormat('dd/MM/yyyy HH:mm');

  static String short(DateTime date) => _short.format(date);
  static String long(DateTime date) => _long.format(date);
  static String withTime(DateTime date) => _withTime.format(date);

  static String relative(DateTime date) {
    final now = DateTime.now();
    final diff = now.difference(date);
    if (diff.inDays == 0) return 'Hoy';
    if (diff.inDays == 1) return 'Ayer';
    if (diff.inDays < 7) return 'Hace ${diff.inDays} días';
    return short(date);
  }
}
