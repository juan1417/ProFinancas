package com.profinanzas.features.facturas.presentation.sheet

import androidx.compose.foundation.background
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
fun ActualizarFacturaSheet(
    invoiceId: String,
    provider: String,
    amount: String,
    notes: String,
    onDismiss: () -> Unit,
    onSave: (provider: String, amount: String, notes: String) -> Unit,
) {
    var editedProvider by remember { mutableStateOf(provider) }
    var editedAmount by remember { mutableStateOf("") }
    var editedNotes by remember { mutableStateOf(notes) }

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
            Text("Actualizar $invoiceId", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)

            OutlinedTextField(
                value = editedProvider,
                onValueChange = { editedProvider = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Proveedor") },
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
                value = amount,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Monto original") },
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

            OutlinedTextField(
                value = editedAmount,
                onValueChange = { editedAmount = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Monto actualizado") },
                placeholder = { Text("Nuevo monto si aplica") },
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
                value = editedNotes,
                onValueChange = { editedNotes = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notas") },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Border,
                    focusedBorderColor = Accent,
                    cursorColor = Accent,
                    unfocusedContainerColor = Surface,
                    focusedContainerColor = Surface,
                ),
                maxLines = 3,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Fg),
                ) {
                    Text("Cancelar")
                }
                Button(
                    onClick = { onSave(editedProvider, editedAmount.ifBlank { amount }, editedNotes) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Bg),
                    enabled = editedProvider.isNotBlank(),
                ) {
                    Text("Guardar")
                }
            }
        }
    }
}
