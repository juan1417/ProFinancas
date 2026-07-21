package com.profinanzas.core.ui.theme

import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
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

@Composable
fun ProFinanzasTheme(content: @Composable () -> Unit) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Bg.toArgb()
            window.navigationBarColor = Bg.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
