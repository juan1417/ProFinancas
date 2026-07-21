package com.profinanzas.features.facturas.presentation.screens

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
import androidx.hilt.navigation.compose.hiltViewModel
import com.profinanzas.core.ui.components.AppCard
import com.profinanzas.core.ui.components.SegmentedTabs
import com.profinanzas.core.ui.components.StatusPill
import com.profinanzas.core.ui.theme.*
import com.profinanzas.features.facturas.presentation.viewmodel.FacturasViewModel
import com.profinanzas.features.facturas.presentation.viewmodel.Invoice

@Composable
fun FacturasScreen(
    viewModel: FacturasViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Registro", "Historial", "Actualizar")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .padding(horizontal = 20.dp),
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("FACTURAS", style = MaterialTheme.typography.labelSmall, color = Accent)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Gestión de facturas", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { /* TODO: open registrar modal */ },
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Nueva", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Sub-tabs
        SegmentedTabs(
            options = tabs,
            selectedIndex = selectedTab,
            onSelected = { selectedTab = it },
        )

        Spacer(modifier = Modifier.height(20.dp))

        when (selectedTab) {
            0 -> RegistroTab(viewModel)
            1 -> HistorialTab(viewModel)
            2 -> ActualizarTab(viewModel)
        }
    }
}

@Composable
private fun RegistroTab(viewModel: FacturasViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val invoices = viewModel.getFilteredInvoices()
    val filters = listOf("Todas", "Pendientes", "Pagadas", "Vencidas")

    // Search bar
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Surface,
        border = ButtonDefaults.outlinedButtonBorder().let { it.copy(brush = androidx.compose.ui.graphics.SolidColor(Border)) },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = Muted, modifier = Modifier.size(18.dp))
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Buscar factura...", color = Muted) },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                    focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                    cursorColor = Accent,
                ),
                singleLine = true,
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Filter pills
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        filters.forEach { filter ->
            val isActive = uiState.selectedFilter == filter
            Surface(
                onClick = { viewModel.setFilter(filter) },
                shape = RoundedCornerShape(999.dp),
                color = if (isActive) Fg else androidx.compose.ui.graphics.Color.Transparent,
                border = if (!isActive) ButtonDefaults.outlinedButtonBorder().let {
                    it.copy(brush = androidx.compose.ui.graphics.SolidColor(Border))
                } else null,
            ) {
                Text(
                    text = filter,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isActive) Bg else Muted,
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Invoice table
    LazyColumn(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 10.dp),
            ) {
                Text("Factura", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = Muted)
                Text("Proveedor", modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.labelSmall, color = Muted)
                Text("Monto", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = Muted, textAlign = androidx.compose.ui.text.style.TextAlign.End)
                Text("Estado", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = Muted)
            }
            HorizontalDivider(color = Border)
        }

        items(invoices) { invoice ->
            InvoiceRow(invoice)
        }
    }
}

@Composable
private fun InvoiceRow(invoice: Invoice) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: open detalle modal */ }
            .padding(vertical = 14.dp, horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(invoice.id, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
        Text(invoice.provider, modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.bodySmall)
        Text(
            "$ ${String.format("%,.0f", invoice.amount)}",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
        )
        Box(modifier = Modifier.weight(1f)) {
            when (invoice.status) {
                "paid" -> StatusPill("Pagada", Accent)
                "pending" -> StatusPill("Pendiente", Warning)
                "overdue" -> StatusPill("Vencida", Danger)
            }
        }
    }
    HorizontalDivider(color = Border)
}

@Composable
private fun HistorialTab(viewModel: FacturasViewModel) {
    val invoices = viewModel.uiState.value.invoices.filter { it.paidDate != null }

    LazyColumn {
        item {
            Text("Facturas registradas", style = MaterialTheme.typography.labelSmall, color = Muted)
            Spacer(modifier = Modifier.height(12.dp))
        }
        items(invoices) { invoice ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CatServices.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, tint = CatServices, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("${invoice.id} — ${invoice.provider}", fontWeight = FontWeight.Medium)
                    Text("Reg: ${invoice.registeredDate} · Pag: ${invoice.paidDate}", style = MaterialTheme.typography.bodySmall, color = Muted)
                }
                Text(
                    "$ ${String.format("%,.0f", invoice.amount)}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            HorizontalDivider(color = Border)
        }
    }
}

@Composable
private fun ActualizarTab(viewModel: FacturasViewModel) {
    val invoices = viewModel.uiState.value.invoices.filter { it.status == "overdue" || it.notes.isNotEmpty() }

    LazyColumn {
        item {
            Text("Selecciona una factura para actualizar", style = MaterialTheme.typography.labelSmall, color = Muted)
            Spacer(modifier = Modifier.height(12.dp))
        }
        items(invoices) { invoice ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* TODO: open actualizar modal */ }
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Warning.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Warning, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("${invoice.id} — ${invoice.provider}", fontWeight = FontWeight.Medium)
                    Text(invoice.notes.ifEmpty { invoice.status }, style = MaterialTheme.typography.bodySmall, color = Muted)
                }
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Muted, modifier = Modifier.size(14.dp))
            }
            HorizontalDivider(color = Border)
        }
    }
}
