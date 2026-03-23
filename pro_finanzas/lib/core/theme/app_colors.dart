import 'package:flutter/material.dart';

class AppColors {
  AppColors._();

  // Primary — deep navy blue
  static const Color primary = Color(0xFF1A237E);
  static const Color primary700 = Color(0xFF283593);
  static const Color primary500 = Color(0xFF3949AB);
  static const Color primary300 = Color(0xFF7986CB);
  static const Color primary100 = Color(0xFFC5CAE9);
  static const Color primary50 = Color(0xFFE8EAF6);

  // Secondary — deep green (income)
  static const Color secondary = Color(0xFF2E7D32);
  static const Color secondary500 = Color(0xFF43A047);
  static const Color secondary100 = Color(0xFFC8E6C9);

  // Tertiary — deep red (expense / error)
  static const Color tertiary = Color(0xFFC62828);
  static const Color tertiary500 = Color(0xFFE53935);
  static const Color tertiary100 = Color(0xFFFFCDD2);

  // Neutral
  static const Color neutral700 = Color(0xFF616161);
  static const Color neutral500 = Color(0xFF9E9E9E);
  static const Color neutral400 = Color(0xFFBDBDBD);
  static const Color neutral200 = Color(0xFFEEEEEE);
  static const Color neutral100 = Color(0xFFF5F5F5);
  static const Color black = Color(0xFF212121);
  static const Color white = Color(0xFFFFFFFF);

  // Semantic aliases
  static const Color income = secondary;
  static const Color expense = tertiary;
  static const Color surface = white;
  static const Color background = neutral100;
  static const Color divider = neutral200;
}
