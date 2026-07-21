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
    onSave: () -> Unit,
) {
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

            // Proveedor
            OutlinedTextField(
                value = "",
                onValueChange = {},
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

            // Monto
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
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
                    value = "$ USD",
                    onValueChange = {},
                    modifier = Modifier.width(120.dp),
                    label = { Text("Moneda") },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Border,
                        focusedBorderColor = Accent,
                        cursorColor = Accent,
                        unfocusedContainerColor = Surface,
                        focusedContainerColor = Surface,
                    ),
                    singleLine = true,
                )
            }

            // Fecha
            OutlinedTextField(
                value = "",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Fecha de vencimiento") },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Border,
                    focusedBorderColor = Accent,
                    cursorColor = Accent,
                    unfocusedContainerColor = Surface,
                    focusedContainerColor = Surface,
                ),
                singleLine = true,
            )

            // Categoría
            OutlinedTextField(
                value = "",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Categoría") },
                placeholder = { Text("Seleccionar") },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Border,
                    focusedBorderColor = Accent,
                    cursorColor = Accent,
                    unfocusedContainerColor = Surface,
                    focusedContainerColor = Surface,
                ),
                singleLine = true,
            )

            // Notas
            OutlinedTextField(
                value = "",
                onValueChange = {},
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

            // Buttons
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
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Bg),
                ) {
                    Text("Guardar")
                }
            }
        }
    }
}
