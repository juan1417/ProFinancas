package com.profinanzas.features.facturas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.profinanzas.core.data.model.InvoiceCreateRequest
import com.profinanzas.core.ui.formatDate
import com.profinanzas.core.ui.components.AppCard
import com.profinanzas.core.ui.components.StatusPill
import com.profinanzas.core.ui.theme.*

@Composable
fun FacturasScreen(viewModel: FacturasViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredInvoices = viewModel.getFilteredInvoices()
    val filters = listOf("Todas", "Pendientes", "Pagadas", "Vencidas")
    var showEditDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // Left: Table
        Column(modifier = Modifier.weight(2f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Facturas",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "${filteredInvoices.size} facturas",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                filters.forEach { filter ->
                    val isSelected = uiState.selectedFilter == filter
                    Surface(
                        onClick = { viewModel.setFilter(filter) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) Fg else Surface,
                    ) {
                        Text(
                            text = filter,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = if (isSelected) Bg else Muted,
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Buscar...", color = Muted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Muted) },
                    modifier = Modifier.width(250.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = Border,
                        focusedContainerColor = Surface,
                        unfocusedContainerColor = Surface,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            AppCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                ) {
                    Text("ID", modifier = Modifier.width(80.dp), style = MaterialTheme.typography.labelSmall, color = Muted)
                    Text("Proveedor", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = Muted)
                    Text("Monto", modifier = Modifier.width(120.dp), style = MaterialTheme.typography.labelSmall, color = Muted)
                    Text("Vencimiento", modifier = Modifier.width(120.dp), style = MaterialTheme.typography.labelSmall, color = Muted)
                    Text("Estado", modifier = Modifier.width(100.dp), style = MaterialTheme.typography.labelSmall, color = Muted)
                }

                HorizontalDivider(color = Border)

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = Accent)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 500.dp),
                    ) {
                        items(filteredInvoices) { invoice ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectInvoice(invoice) }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    invoice.invoiceNumber,
                                    modifier = Modifier.width(80.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Muted,
                                )
                                Text(
                                    invoice.provider,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                )
                                Text(
                                    "$ ${String.format("%,.0f", invoice.amount)}",
                                    modifier = Modifier.width(120.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    formatDate(invoice.dueDate),
                                    modifier = Modifier.width(120.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Muted,
                                )
                                Box(modifier = Modifier.width(100.dp)) {
                                    when (invoice.status) {
                                        "PAID" -> StatusPill("Pagada", Accent)
                                        "PENDING" -> StatusPill("Pendiente", Warning)
                                        "OVERDUE" -> StatusPill("Vencida", Danger)
                                    }
                                }
                            }
                            HorizontalDivider(color = Border, thickness = 0.5.dp)
                        }
                    }
                }
            }
        }

        // Right: Detail Panel
        uiState.selectedInvoice?.let { invoice ->
            Column(
                modifier = Modifier
                    .width(340.dp)
                    .fillMaxHeight(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = invoice.invoiceNumber,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    IconButton(onClick = { viewModel.selectInvoice(null) }) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Muted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                AppCard {
                    DetailRow("Proveedor", invoice.provider)
                    DetailRow("Monto", "$ ${String.format("%,.0f", invoice.amount)}")
                    DetailRow("Vencimiento", formatDate(invoice.dueDate))
                    DetailRow("Estado", invoice.status.replaceFirstChar { it.uppercase() })
                    DetailRow("Categoría", invoice.category)
                    DetailRow("Registrada", formatDate(invoice.registeredDate))
                    invoice.paidDate?.let { DetailRow("Pagada", formatDate(it)) }
                    if (invoice.notes.isNotEmpty()) {
                        DetailRow("Notas", invoice.notes)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showEditDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Accent),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Editar", color = Bg)
                    }
                    OutlinedButton(
                        onClick = { viewModel.deleteInvoice(invoice.id) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Danger),
                    ) {
                        Text("Eliminar")
                    }
                }
            }
        }

        uiState.selectedInvoice?.let { invoice ->
            if (showEditDialog) {
                EditInvoiceDialog(
                    invoice = invoice,
                    onDismiss = { showEditDialog = false },
                    onSave = { provider, amount, status, category, dueDate, notes ->
                        viewModel.updateInvoice(
                            invoice.id,
                            InvoiceCreateRequest(
                                provider = provider,
                                amount = amount,
                                status = status,
                                category = category,
                                dueDate = dueDate,
                                registeredDate = invoice.registeredDate,
                                paidDate = invoice.paidDate,
                                notes = notes,
                            )
                        )
                        showEditDialog = false
                    }
                )
            }
        }
    }
}

@Composable
private fun EditInvoiceDialog(
    invoice: com.profinanzas.core.domain.model.Invoice,
    onDismiss: () -> Unit,
    onSave: (provider: String, amount: Double, status: String, category: String, dueDate: String, notes: String) -> Unit,
) {
    var provider by remember { mutableStateOf(invoice.provider) }
    var amount by remember { mutableStateOf(invoice.amount.toString()) }
    var status by remember { mutableStateOf(invoice.status) }
    var category by remember { mutableStateOf(invoice.category) }
    var dueDate by remember { mutableStateOf(invoice.dueDate) }
    var notes by remember { mutableStateOf(invoice.notes) }

    val statuses = listOf("PENDING", "PAID", "OVERDUE")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Factura", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = provider,
                    onValueChange = { provider = it },
                    label = { Text("Proveedor") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Monto") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Vencimiento (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text("Estado", style = MaterialTheme.typography.labelMedium, color = Muted)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    statuses.forEach { s ->
                        FilterChip(
                            selected = status == s,
                            onClick = { status = s },
                            label = { Text(s.replaceFirstChar { it.uppercase() }) },
                        )
                    }
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedAmount = amount.toDoubleOrNull() ?: invoice.amount
                    onSave(provider, parsedAmount, status, category, dueDate, notes)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Bg),
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Muted)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
