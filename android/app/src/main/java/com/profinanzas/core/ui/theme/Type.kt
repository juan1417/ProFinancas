package com.profinanzas.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Display: serif (Georgia/Charter equivalent on Android)
val DisplayFont = FontFamily.Serif

// Body: system sans
val BodyFont = FontFamily.Default

// Mono: monospace for numbers, captions, eyebrows
val MonoFont = FontFamily.Monospace

val Typography = Typography(
    // H1 — display serif, 32px, 700, ls -0.02em
    headlineLarge = TextStyle(
        fontFamily = DisplayFont,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 35.sp, // 1.1
        letterSpacing = (-0.02).sp,
    ),
    // H2 — display serif, 26px, 700, ls -0.015em
    headlineMedium = TextStyle(
        fontFamily = DisplayFont,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 30.sp, // 1.15
        letterSpacing = (-0.015).sp,
    ),
    // H3 — body, 18px, 600
    headlineSmall = TextStyle(
        fontFamily = BodyFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 23.sp, // 1.3
    ),
    // Lead — body, 16px, 400
    bodyLarge = TextStyle(
        fontFamily = BodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 25.sp, // 1.55
    ),
    // Body — body, 15px, 400
    bodyMedium = TextStyle(
        fontFamily = BodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 23.sp,
    ),
    // Meta — mono, 12px
    bodySmall = TextStyle(
        fontFamily = MonoFont,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        color = Muted,
    ),
    // Eyebrow — mono, 11px, ls 0.1em, uppercase
    labelSmall = TextStyle(
        fontFamily = MonoFont,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.1.sp,
    ),
    // Stat values — mono, 36px, 700, ls -0.03em, tabular-nums
    displayLarge = TextStyle(
        fontFamily = MonoFont,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.03).sp,
    ),
    // Budget amounts — mono, 13px, tabular-nums
    labelMedium = TextStyle(
        fontFamily = MonoFont,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    ),
)
