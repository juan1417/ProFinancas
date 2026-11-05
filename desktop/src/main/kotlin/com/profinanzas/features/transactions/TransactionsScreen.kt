package com.profinanzas.features.transactions

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
import com.profinanzas.core.ui.formatRelativeDate
import com.profinanzas.core.ui.components.AppCard
import com.profinanzas.core.ui.components.StatusPill
import com.profinanzas.core.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TransactionsScreen(viewModel: TransactionsViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Todos", "Ingresos", "Gastos")
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("TRANSACCIONES", style = MaterialTheme.typography.labelSmall, color = Accent)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Gestión de transacciones", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Nueva", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            }
        }

        // Error visible: antes se perdía y la pantalla quedaba vacía.
        uiState.error?.let { message ->
            AppCard {
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
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("Cerrar", color = Muted)
                    }
                }
            }
        }

        // Tabs
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            tabs.forEachIndexed { index, label ->
                FilterChip(
                    selected = selectedTab == index,
                    onClick = {
                        selectedTab = index
                        val type = when (index) {
                            1 -> "INCOME"
                            2 -> "EXPENSE"
                            else -> null
                        }
                        viewModel.loadTransactions(type)
                    },
                    label = { Text(label) },
                )
            }
        }

        // Transaction list
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Accent)
            }
        } else if (uiState.transactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay transacciones", color = Muted)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.transactions) { transaction ->
                    AppCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                if (transaction.isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = if (transaction.isIncome) Accent else Danger,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(transaction.description, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(transaction.categoryName ?: "", style = MaterialTheme.typography.bodySmall, color = Muted)
                                    Text("·", color = Muted)
                                    Text(formatRelativeDate(transaction.transactionDate), style = MaterialTheme.typography.bodySmall, color = Muted)
                                }
                            }
                            Text(
                                text = "${if (transaction.isIncome) "+" else "-"}${currencyFormat.format(transaction.amount)}",
                                color = if (transaction.isIncome) Accent else Danger,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { viewModel.deleteTransaction(transaction.id) },
                                modifier = Modifier.size(32.dp),
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Danger, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Create Transaction Dialog
    if (showCreateDialog) {
        CreateTransactionDialog(
            categories = uiState.categories,
            onDismiss = { showCreateDialog = false },
            onCreate = { categoryId, type, amount, description, date ->
                viewModel.createTransaction(categoryId, type, amount, description, date)
                showCreateDialog = false
            },
            onTypeChange = { viewModel.loadCategories(it) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateTransactionDialog(
    categories: List<com.profinanzas.core.domain.model.Category>,
    onDismiss: () -> Unit,
    onCreate: (categoryId: Int, type: String, amount: String, description: String, date: String) -> Unit,
    onTypeChange: (String) -> Unit,
) {
    var type by remember { mutableStateOf("EXPENSE") }
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<com.profinanzas.core.domain.model.Category?>(null) }
    var date by remember { mutableStateOf(java.time.LocalDate.now().toString()) }
    val datePattern = remember { Regex("""\d{4}-\d{2}-\d{2}""") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Transacción") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == "EXPENSE",
                        onClick = {
                            type = "EXPENSE"
                            selectedCategory = null
                            onTypeChange("EXPENSE")
                        },
                        label = { Text("Gasto") },
                    )
                    FilterChip(
                        selected = type == "INCOME",
                        onClick = {
                            type = "INCOME"
                            selectedCategory = null
                            onTypeChange("INCOME")
                        },
                        label = { Text("Ingreso") },
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Monto") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                // Category dropdown
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                },
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Fecha (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = !datePattern.matches(date),
                    supportingText = {
                        if (!datePattern.matches(date)) {
                            Text("Usa el formato YYYY-MM-DD", color = Danger)
                        }
                    },
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedCategory?.let { cat ->
                        onCreate(cat.id, type, amount, description, date)
                    }
                },
                enabled = selectedCategory != null && description.isNotBlank() &&
                    amount.toDoubleOrNull() != null && datePattern.matches(date),
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
    )
}
