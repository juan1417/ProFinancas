package com.profinanzas.features.facturas.presentation.sheet

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.profinanzas.core.domain.model.Invoice
import com.profinanzas.core.ui.components.StatusPill
import com.profinanzas.core.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleFacturaSheet(
    invoice: Invoice,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onMarkPaid: () -> Unit,
) {
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
            Text("${invoice.invoiceNumber} — ${invoice.provider}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)

            DetailRow("Proveedor", invoice.provider)
            DetailRow("Monto", "$ ${String.format("%,.0f", invoice.amount)}", valueFontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Estado", style = MaterialTheme.typography.bodySmall, color = Muted)
                when (invoice.status) {
                    "PAID" -> StatusPill("Pagada", Accent)
                    "PENDING" -> StatusPill("Pendiente", Warning)
                    "OVERDUE" -> StatusPill("Vencida", Danger)
                }
            }
            DetailRow("Vencimiento", invoice.dueDate)
            DetailRow("Categoría", invoice.category)

            HorizontalDivider(color = Border)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Danger),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                ) {
                    Text("Eliminar")
                }
                Button(
                    onClick = onMarkPaid,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Bg),
                    enabled = invoice.status != "PAID",
                ) {
                    Text("Marcar pagada")
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueFontWeight: FontWeight = FontWeight.Normal,
    valueFontSize: TextUnit = TextUnit.Unspecified,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Muted)
        Text(
            value,
            style = if (valueFontSize != TextUnit.Unspecified) {
                MaterialTheme.typography.bodyMedium.copy(fontSize = valueFontSize)
            } else {
                MaterialTheme.typography.bodyMedium
            },
            fontWeight = valueFontWeight,
        )
    }
}
