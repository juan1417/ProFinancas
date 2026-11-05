package com.profinanzas.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.profinanzas.core.ui.theme.*

/**
 * Muestra un error de red/validación de forma consistente.
 *
 * Antes cada pantalla guardaba `error` en su UiState pero nadie lo pintaba, así
 * que un fallo del backend se veía como una pantalla vacía sin explicación.
 */
@Composable
fun ErrorBanner(
    message: String?,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    if (message.isNullOrBlank()) return

    AppCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = message,
                color = Danger,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f),
            )
            if (onDismiss != null) {
                TextButton(onClick = onDismiss) {
                    Text("Cerrar", color = Muted)
                }
            }
        }
    }
}
