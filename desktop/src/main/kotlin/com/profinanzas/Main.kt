package com.profinanzas

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "ProFinanzas",
        state = WindowState(
            position = WindowPosition(Alignment.Center),
            size = DpSize(1400.dp, 900.dp),
        ),
    ) {
        App()
    }
}
