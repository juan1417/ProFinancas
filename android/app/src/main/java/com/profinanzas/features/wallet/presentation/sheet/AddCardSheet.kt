package com.profinanzas.features.wallet.presentation.sheet

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
fun AddCardSheet(
    onDismiss: () -> Unit,
    onSave: (brand: String, bank: String, last4: String, expiry: String, holderName: String, colorHex: String) -> Unit,
) {
    var brand by remember { mutableStateOf("") }
    var bank by remember { mutableStateOf("") }
    var last4 by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }
    var holderName by remember { mutableStateOf("") }

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
            Text("Agregar tarjeta", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)

            // Brand
            OutlinedTextField(
                value = brand,
                onValueChange = { brand = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Marca") },
                placeholder = { Text("Visa, Mastercard, Amex...") },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Border,
                    focusedBorderColor = Accent,
                    cursorColor = Accent,
                    unfocusedContainerColor = Surface,
                    focusedContainerColor = Surface,
                ),
                singleLine = true,
            )

            // Bank
            OutlinedTextField(
                value = bank,
                onValueChange = { bank = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Banco") },
                placeholder = { Text("Nombre del banco") },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Border,
                    focusedBorderColor = Accent,
                    cursorColor = Accent,
                    unfocusedContainerColor = Surface,
                    focusedContainerColor = Surface,
                ),
                singleLine = true,
            )

            // Last 4 digits
            OutlinedTextField(
                value = last4,
                onValueChange = { if (it.length <= 4) last4 = it.filter { c -> c.isDigit() } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Últimos 4 dígitos") },
                placeholder = { Text("0000") },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Border,
                    focusedBorderColor = Accent,
                    cursorColor = Accent,
                    unfocusedContainerColor = Surface,
                    focusedContainerColor = Surface,
                ),
                singleLine = true,
            )

            // Expiry
            OutlinedTextField(
                value = expiry,
                onValueChange = { expiry = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Vencimiento") },
                placeholder = { Text("MM/AA") },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Border,
                    focusedBorderColor = Accent,
                    cursorColor = Accent,
                    unfocusedContainerColor = Surface,
                    focusedContainerColor = Surface,
                ),
                singleLine = true,
            )

            // Holder name
            OutlinedTextField(
                value = holderName,
                onValueChange = { holderName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nombre del titular") },
                placeholder = { Text("Como aparece en la tarjeta") },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Border,
                    focusedBorderColor = Accent,
                    cursorColor = Accent,
                    unfocusedContainerColor = Surface,
                    focusedContainerColor = Surface,
                ),
                singleLine = true,
            )

            // Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Fg),
                ) {
                    Text("Cancelar")
                }
                Button(
                    onClick = {
                        val colorHex = when {
                            brand.contains("Visa", ignoreCase = true) -> "#1A1A2E"
                            brand.contains("Master", ignoreCase = true) -> "#1A1A1A"
                            brand.contains("Amex", ignoreCase = true) -> "#0D2137"
                            else -> "#1A1A2E"
                        }
                        onSave(brand, bank, last4, expiry, holderName, colorHex)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(),
                    enabled = brand.isNotBlank() && last4.length == 4,
                ) {
                    Text("Guardar")
                }
            }
        }
    }
}
