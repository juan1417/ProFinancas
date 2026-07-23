package com.profinanzas.features.facturas.presentation.sheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.profinanzas.core.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarFacturaSheet(
    onDismiss: () -> Unit,
    onScan: () -> Unit,
    onSave: (provider: String, amount: Double, dueDate: String, category: String, notes: String) -> Unit,
) {
    var provider by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .background(Border, RoundedCornerShape(2.dp))
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
            Text("Registrar factura", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)

            OutlinedTextField(
                value = provider,
                onValueChange = { provider = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Proveedor") },
                placeholder = { Text("Nombre del proveedor") },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Border,
                    focusedBorderColor = Accent,
                    cursorColor = Accent,
                    unfocusedContainerColor = Surface,
                    focusedContainerColor = Surface,
                ),
                singleLine = true,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Monto") },
                    placeholder = { Text("0.00") },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Border,
                        focusedBorderColor = Accent,
                        cursorColor = Accent,
                        unfocusedContainerColor = Surface,
                        focusedContainerColor = Surface,
                    ),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = "$ COP",
                    onValueChange = {},
                    modifier = Modifier.width(120.dp),
                    label = { Text("Moneda") },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Border,
                        focusedBorderColor = Accent,
                        cursorColor = Accent,
                        unfocusedContainerColor = Surface,
                        focusedContainerColor = Surface,
                        disabledTextColor = Fg,
                        disabledBorderColor = Border,
                        disabledLabelColor = Muted,
                    ),
                    singleLine = true,
                )
            }

            OutlinedTextField(
                value = dueDate,
                onValueChange = { dueDate = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Fecha de vencimiento") },
                placeholder = { Text("YYYY-MM-DD") },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Border,
                    focusedBorderColor = Accent,
                    cursorColor = Accent,
                    unfocusedContainerColor = Surface,
                    focusedContainerColor = Surface,
                ),
                singleLine = true,
            )

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Categoría") },
                placeholder = { Text("Ej: Servicios, Alimentación") },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Border,
                    focusedBorderColor = Accent,
                    cursorColor = Accent,
                    unfocusedContainerColor = Surface,
                    focusedContainerColor = Surface,
                ),
                singleLine = true,
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notas") },
                placeholder = { Text("Detalles adicionales...") },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Border,
                    focusedBorderColor = Accent,
                    cursorColor = Accent,
                    unfocusedContainerColor = Surface,
                    focusedContainerColor = Surface,
                ),
                maxLines = 2,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onScan,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Fg),
                    border = ButtonDefaults.outlinedButtonBorder(),
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Escanear")
                }
                Button(
                    onClick = {
                        val parsedAmount = amount.toDoubleOrNull() ?: 0.0
                        onSave(provider, parsedAmount, dueDate, category, notes)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Bg),
                    enabled = provider.isNotBlank() && amount.toDoubleOrNull() != null,
                ) {
                    Text("Guardar")
                }
            }
        }
    }
}
