package com.profinanzas.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Accent,
    onPrimary = Bg,
    primaryContainer = Accent,
    onPrimaryContainer = Bg,
    secondary = CatTransport,
    onSecondary = Fg,
    tertiary = CatServices,
    onTertiary = Fg,
    background = Bg,
    onBackground = Fg,
    surface = Surface,
    onSurface = Fg,
    surfaceVariant = Neutral300,
    onSurfaceVariant = Muted,
    error = Danger,
    onError = Fg,
    outline = Border,
)

@Composable
fun ProFinanzasTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content,
    )
}
