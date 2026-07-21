package com.profinanzas.core.ui.theme

import androidx.compose.ui.graphics.Color

// ─── Design tokens (dark theme from prototype) ─────────────────────────────
val Bg = Color(0xFF0A0A0F)
val Surface = Color(0xFF141419)
val Fg = Color(0xFFE8E8EC)
val Muted = Color(0xFF6B6B76)
val Border = Color(0xFF1E1E26)
val Accent = Color(0xFF22C55E)
val Danger = Color(0xFFEF4444)
val Warning = Color(0xFFF59E0B)

// Derived
val AccentSoft = Color(0x2422C55E) // accent 14%
val FgSoft = Color(0x0FE8E8EC)     // fg 6%
val SurfaceHover = FgSoft

// Category colors
val CatFood = Warning
val CatTransport = Color(0xFF2563EB)
val CatServices = Color(0xFF7C3AED)
val CatShopping = Color(0xFFDB2777)
val CatHealth = Color(0xFF14B8A6)

// Semantic aliases
val IncomeGreen = Accent
val ExpenseRed = Danger
val Neutral50 = Color(0xFF0A0A0F)
val Neutral100 = Color(0xFF141419)
val Neutral200 = Border
val Neutral300 = Color(0xFF2A2A35)
val Neutral400 = Muted
val Neutral500 = Muted
val Neutral600 = Muted
val Neutral700 = Color(0xFF8B8B96)
val Neutral800 = Fg
val Neutral900 = Fg

// Chart colors
val ChartNavy = Accent
val ChartRed = Danger
val ChartGreen = Accent
val ChartSlate = CatTransport
val ChartBrown = CatServices

// Legacy aliases (used by existing screens)
val NavyBlue = Accent
val NavyBlueLight = Accent
val SurfaceLight = Fg
