import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'app_colors.dart';

class AppTextStyles {
  AppTextStyles._();

  static final TextStyle headlineLarge = GoogleFonts.inter(
    fontSize: 32, fontWeight: FontWeight.w800, color: AppColors.black);

  static final TextStyle headline = GoogleFonts.inter(
    fontSize: 26, fontWeight: FontWeight.w700, color: AppColors.black);

  static final TextStyle headlineSmall = GoogleFonts.inter(
    fontSize: 20, fontWeight: FontWeight.w700, color: AppColors.black);

  static final TextStyle body = GoogleFonts.inter(
    fontSize: 15, fontWeight: FontWeight.w400, color: AppColors.black);

  static final TextStyle bodyMedium = GoogleFonts.inter(
    fontSize: 15, fontWeight: FontWeight.w500, color: AppColors.black);

  static final TextStyle bodySecondary = GoogleFonts.inter(
    fontSize: 13, fontWeight: FontWeight.w400, color: AppColors.neutral700);

  static final TextStyle label = GoogleFonts.inter(
    fontSize: 11, fontWeight: FontWeight.w500,
    color: AppColors.neutral700, letterSpacing: 1.2);

  static final TextStyle amount = GoogleFonts.inter(
    fontSize: 34, fontWeight: FontWeight.w800, color: AppColors.black);

  static final TextStyle amountSmall = GoogleFonts.inter(
    fontSize: 16, fontWeight: FontWeight.w700, color: AppColors.black);

  static final TextStyle button = GoogleFonts.inter(
    fontSize: 15, fontWeight: FontWeight.w600, letterSpacing: 0.3);
}
