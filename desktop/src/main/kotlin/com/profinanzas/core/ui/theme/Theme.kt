package com.profinanzas.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

@Composable
fun ProFinanzasTheme(
    darkTheme: Boolean? = null,
    content: @Composable () -> Unit,
) {
    val dark = isDarkTheme(darkTheme)
    val palette = if (dark) DarkPalette else LightPalette

    val colorScheme = if (dark) {
        darkColorScheme(
            primary = Accent, onPrimary = Color(0xFF0A0A0F),
            primaryContainer = Accent, onPrimaryContainer = Color(0xFF0A0A0F),
            secondary = CatTransport, onSecondary = palette.fg,
            tertiary = CatServices, onTertiary = palette.fg,
            background = palette.bg, onBackground = palette.fg,
            surface = palette.surface, onSurface = palette.fg,
            surfaceVariant = palette.neutral300, onSurfaceVariant = palette.muted,
            error = Danger, onError = Color(0xFFFFFFFF),
            outline = palette.border,
        )
    } else {
        lightColorScheme(
            primary = Accent, onPrimary = Color(0xFF0A0A0F),
            primaryContainer = Accent, onPrimaryContainer = Color(0xFF0A0A0F),
            secondary = CatTransport, onSecondary = Color(0xFFFFFFFF),
            tertiary = CatServices, onTertiary = Color(0xFFFFFFFF),
            background = palette.bg, onBackground = palette.fg,
            surface = palette.surface, onSurface = palette.fg,
            surfaceVariant = palette.neutral300, onSurfaceVariant = palette.muted,
            error = Danger, onError = Color(0xFFFFFFFF),
            outline = palette.border,
        )
    }

    CompositionLocalProvider(LocalAppPalette provides palette) {
        MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
    }
}
