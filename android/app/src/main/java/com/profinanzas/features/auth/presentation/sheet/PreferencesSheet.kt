package com.profinanzas.features.auth.presentation.sheet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.profinanzas.core.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesSheet(
    onDismiss: () -> Unit,
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var currency by remember { mutableStateOf("COP") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Preferencias", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)

            // Notifications toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Notificaciones", fontWeight = FontWeight.SemiBold)
                    Text("Alertas de vencimiento y presupuesto", style = MaterialTheme.typography.bodySmall, color = Muted)
                }
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = Accent,
                        uncheckedTrackColor = Border,
                    ),
                )
            }

            HorizontalDivider(color = Border)

            // Dark mode toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Modo oscuro", fontWeight = FontWeight.SemiBold)
                    Text("Tema de la aplicación", style = MaterialTheme.typography.bodySmall, color = Muted)
                }
                val ctx = androidx.compose.ui.platform.LocalContext.current
                Switch(
                    checked = isDarkTheme(),
                    onCheckedChange = { isDark ->
                        AppTheme.set(ctx, if (isDark) ThemeMode.DARK else ThemeMode.LIGHT)
                    },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = Accent,
                        uncheckedTrackColor = Border,
                    ),
                )
            }

            HorizontalDivider(color = Border)

            // Currency
            Column {
                Text("Moneda", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("USD", "COP", "EUR").forEach { curr ->
                        FilterChip(
                            selected = currency == curr,
                            onClick = { currency = curr },
                            label = { Text(curr) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Accent,
                                selectedLabelColor = Bg,
                            ),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(),
            ) {
                Text("Cerrar")
            }
        }
    }
}
