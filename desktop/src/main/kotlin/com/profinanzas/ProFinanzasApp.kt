package com.profinanzas

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.profinanzas.core.ui.theme.ProFinanzasTheme
import com.profinanzas.core.update.AppVersionInfo
import com.profinanzas.core.update.UpdateChecker
import com.profinanzas.ui.navigation.DesktopNavGraph
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun App() {
    ProFinanzasTheme {
        var updateInfo by remember { mutableStateOf<AppVersionInfo?>(null) }
        val scope = rememberCoroutineScope()
        LaunchedEffect(Unit) {
            scope.launch {
                withContext(Dispatchers.IO) {
                    updateInfo = UpdateChecker.check()
                }
            }
        }
        if (updateInfo != null && updateInfo!!.forceUpdate) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Actualización disponible") },
                text = { Text(updateInfo!!.changelog.ifBlank { "Hay una nueva versión disponible." }) },
                confirmButton = {
                    TextButton(onClick = {
                        java.awt.Desktop.getDesktop().browse(java.net.URI(updateInfo!!.downloadUrl))
                        updateInfo = null
                    }) { Text("Descargar") }
                },
                dismissButton = {
                    TextButton(onClick = { updateInfo = null }) { Text("Más tarde") }
                },
            )
        }
        DesktopNavGraph()
    }
}
