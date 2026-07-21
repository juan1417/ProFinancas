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
import com.profinanzas.core.ui.components.AppCard
import com.profinanzas.core.ui.components.StatusPill
import com.profinanzas.core.ui.theme.*

@Composable
fun FacturasScreen(viewModel: FacturasViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredInvoices = viewModel.getFilteredInvoices()
    val filters = listOf("Todas", "Pendientes", "Pagadas", "Vencidas")

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // Left: Table
        Column(modifier = Modifier.weight(2f)) {
            // Header
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

            // Filters + Search
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

            // Table
            AppCard {
                // Table header
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

                // Table rows
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
                                invoice.id,
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
                                invoice.dueDate,
                                modifier = Modifier.width(120.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = Muted,
                            )
                            Box(modifier = Modifier.width(100.dp)) {
                                when (invoice.status) {
                                    "paid" -> StatusPill("Pagada", Accent)
                                    "pending" -> StatusPill("Pendiente", Warning)
                                    "overdue" -> StatusPill("Vencida", Danger)
                                }
                            }
                        }
                        HorizontalDivider(color = Border, thickness = 0.5.dp)
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
                        text = invoice.id,
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
                    DetailRow("Vencimiento", invoice.dueDate)
                    DetailRow("Estado", invoice.status.replaceFirstChar { it.uppercase() })
                    DetailRow("Categoría", invoice.category)
                    DetailRow("Registrada", invoice.registeredDate)
                    invoice.paidDate?.let { DetailRow("Pagada", it) }
                    if (invoice.notes.isNotEmpty()) {
                        DetailRow("Notas", invoice.notes)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = Accent),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Editar", color = Bg)
                    }
                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Danger),
                    ) {
                        Text("Eliminar")
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Muted)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
