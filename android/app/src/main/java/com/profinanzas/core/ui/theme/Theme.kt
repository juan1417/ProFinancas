package com.profinanzas.core.ui.theme

import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun ProFinanzasTheme(
    // null => sigue AppTheme.mode (system/light/dark). Forzar un valor puntualmente.
    darkTheme: Boolean? = null,
    content: @Composable () -> Unit,
) {
    val dark = isDarkTheme(darkTheme)
    val palette = if (dark) DarkPalette else LightPalette

    val colorScheme = if (dark) {
        darkColorScheme(
            primary = Accent, onPrimary = palette.bg,
            primaryContainer = Accent, onPrimaryContainer = palette.bg,
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
            primary = Accent, onPrimary = palette.bg,
            primaryContainer = Accent, onPrimaryContainer = palette.bg,
            secondary = CatTransport, onSecondary = Color(0xFFFFFFFF),
            tertiary = CatServices, onTertiary = Color(0xFFFFFFFF),
            background = palette.bg, onBackground = palette.fg,
            surface = palette.surface, onSurface = palette.fg,
            surfaceVariant = palette.neutral300, onSurfaceVariant = palette.muted,
            error = Danger, onError = Color(0xFFFFFFFF),
            outline = palette.border,
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val bg = palette.bg.toArgb()
            window.statusBarColor = bg
            window.navigationBarColor = bg
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }

    CompositionLocalProvider(LocalAppPalette provides palette) {
        MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
    }
}
