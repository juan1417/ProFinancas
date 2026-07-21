package com.profinanzas

import androidx.compose.runtime.Composable
import com.profinanzas.core.ui.theme.ProFinanzasTheme
import com.profinanzas.ui.navigation.DesktopNavGraph

@Composable
fun App() {
    ProFinanzasTheme {
        DesktopNavGraph()
    }
}
