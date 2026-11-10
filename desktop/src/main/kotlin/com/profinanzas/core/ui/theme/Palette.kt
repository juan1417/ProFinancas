package com.profinanzas.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import java.util.prefs.Preferences

enum class ThemeMode { SYSTEM, LIGHT, DARK }

// Estado global del modo de tema, persistido (java.util.prefs -> archivo/registro por SO).
object AppTheme {
    private val prefs = Preferences.userRoot().node("/com/profinanzas/ui")
    private const val KEY = "mode"

    val mode: MutableState<ThemeMode> = mutableStateOf(read())

    private fun read(): ThemeMode =
        runCatching { ThemeMode.valueOf(prefs.get(KEY, ThemeMode.SYSTEM.name)) }
            .getOrDefault(ThemeMode.SYSTEM)

    fun set(value: ThemeMode) {
        mode.value = value
        prefs.put(KEY, value.name)
    }
}

data class AppPalette(
    val bg: Color,
    val surface: Color,
    val fg: Color,
    val muted: Color,
    val border: Color,
    val accentSoft: Color,
    val fgSoft: Color,
    val neutral50: Color,
    val neutral100: Color,
    val neutral300: Color,
    val neutral700: Color,
)

internal val DarkPalette = AppPalette(
    bg = Color(0xFF0A0A0F),
    surface = Color(0xFF141419),
    fg = Color(0xFFE8E8EC),
    muted = Color(0xFF6B6B76),
    border = Color(0xFF1E1E26),
    accentSoft = Color(0x2422C55E),
    fgSoft = Color(0x0FE8E8EC),
    neutral50 = Color(0xFF0A0A0F),
    neutral100 = Color(0xFF141419),
    neutral300 = Color(0xFF2A2A35),
    neutral700 = Color(0xFF8B8B96),
)

internal val LightPalette = AppPalette(
    bg = Color(0xFFF7F7FA),
    surface = Color(0xFFFFFFFF),
    fg = Color(0xFF0A0A0F),
    muted = Color(0xFF5A5A66),
    border = Color(0xFFE3E4EA),
    accentSoft = Color(0x3322C55E),
    fgSoft = Color(0x0F0A0A0F),
    neutral50 = Color(0xFFFFFFFF),
    neutral100 = Color(0xFFF2F3F6),
    neutral300 = Color(0xFFE3E4EA),
    neutral700 = Color(0xFF4A4A55),
)

val LocalAppPalette = compositionLocalOf { DarkPalette }

// Compose Desktop no expone de forma fiable el modo oscuro del SO, así que
// SYSTEM se resuelve a oscuro (comportamiento histórico del escritorio).
@Composable
fun isDarkTheme(darkTheme: Boolean? = null): Boolean {
    if (darkTheme != null) return darkTheme
    return when (AppTheme.mode.value) {
        ThemeMode.SYSTEM -> true
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
}

// ─── Tokens neutros adaptativos (mismos nombres que antes) ───────────────────
val Bg: Color @Composable get() = LocalAppPalette.current.bg
val Surface: Color @Composable get() = LocalAppPalette.current.surface
val Fg: Color @Composable get() = LocalAppPalette.current.fg
val Muted: Color @Composable get() = LocalAppPalette.current.muted
val Border: Color @Composable get() = LocalAppPalette.current.border
val AccentSoft: Color @Composable get() = LocalAppPalette.current.accentSoft
val FgSoft: Color @Composable get() = LocalAppPalette.current.fgSoft
val SurfaceHover: Color @Composable get() = LocalAppPalette.current.fgSoft
val SurfaceLight: Color @Composable get() = LocalAppPalette.current.fg

val Neutral50: Color @Composable get() = LocalAppPalette.current.neutral50
val Neutral100: Color @Composable get() = LocalAppPalette.current.neutral100
val Neutral200: Color @Composable get() = LocalAppPalette.current.border
val Neutral300: Color @Composable get() = LocalAppPalette.current.neutral300
val Neutral400: Color @Composable get() = LocalAppPalette.current.muted
val Neutral500: Color @Composable get() = LocalAppPalette.current.muted
val Neutral600: Color @Composable get() = LocalAppPalette.current.muted
val Neutral700: Color @Composable get() = LocalAppPalette.current.neutral700
val Neutral800: Color @Composable get() = LocalAppPalette.current.fg
val Neutral900: Color @Composable get() = LocalAppPalette.current.fg
