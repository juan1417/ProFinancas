package com.profinanzas.core.ui.theme

import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

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

// Esquema claro: mismo vocabulario de tokens, invertido.
private val LightColorScheme = lightColorScheme(
    primary = Accent,
    onPrimary = Color(0xFF0A0A0F),
    primaryContainer = Accent,
    onPrimaryContainer = Color(0xFF0A0A0F),
    secondary = CatTransport,
    onSecondary = Color(0xFF0A0A0F),
    tertiary = CatServices,
    onTertiary = Color(0xFF0A0A0F),
    background = Color(0xFFF7F7FA),
    onBackground = Color(0xFF0A0A0F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0A0A0F),
    surfaceVariant = Color(0xFFE8E8EC),
    onSurfaceVariant = Color(0xFF6B6B76),
    error = Danger,
    onError = Color(0xFFFFFFFF),
    outline = Border,
)

@Composable
fun ProFinanzasTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val bg = if (darkTheme) Bg else Color(0xFFF7F7FA)
            window.statusBarColor = bg.toArgb()
            window.navigationBarColor = bg.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
