package com.profinanzas.features.transactions.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.profinanzas.core.ui.components.AppCard
import com.profinanzas.core.ui.components.SegmentedTabs
import com.profinanzas.core.ui.theme.*
import com.profinanzas.features.transactions.presentation.viewmodel.TransactionViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TransactionsScreen(
    viewModel: TransactionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddSheet by remember { mutableStateOf(false) }
    val tabs = listOf("Todos", "Ingresos", "Gastos")
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    LaunchedEffect(selectedTab) {
        val type = when (selectedTab) {
            1 -> "INCOME"
            2 -> "EXPENSE"
            else -> null
        }
        viewModel.loadTransactions(type = type)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Error visible: antes se perdía y la pantalla quedaba vacía.
            uiState.error?.let { message ->
                item {
                    AppCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = message,
                                color = ExpenseRed,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f),
                            )
                            TextButton(onClick = { viewModel.clearError() }) {
                                Text("Cerrar")
                            }
                        }
                    }
                }
            }

            // Summary strip
            item {
                val summary = uiState.summary
                AppCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text("Ingresos", style = MaterialTheme.typography.labelSmall, color = Neutral600)
                            Text(currencyFormat.format(summary?.totalIncome ?: 0.0), color = IncomeGreen, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Transacciones", style = MaterialTheme.typography.labelSmall, color = Neutral600)
                            Text("${summary?.transactionCount ?: 0}", fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Gastos", style = MaterialTheme.typography.labelSmall, color = Neutral600)
                            Text(currencyFormat.format(summary?.totalExpenses ?: 0.0), color = ExpenseRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Exportar (Hoja de Cálculo)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = { viewModel.exportTransactions("csv") },
                        enabled = !uiState.isExporting,
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (uiState.isExporting) "Exportando..." else "Exportar CSV")
                    }
                }
                uiState.exportMessage?.let { msg ->
                    LaunchedEffect(msg) {
                        android.widget.Toast.makeText(
                            context, msg, android.widget.Toast.LENGTH_LONG
                        ).show()
                        viewModel.clearExportMessage()
                    }
                }
            }

            // Tabs
            item {
                SegmentedTabs(
                    options = tabs,
                    selectedIndex = selectedTab,
                    onSelected = { selectedTab = it },
                )
            }

            // Transaction list
            items(uiState.transactions) { transaction ->
                AppCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            if (transaction.isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = if (transaction.isIncome) IncomeGreen else ExpenseRed,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(transaction.description, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(transaction.categoryName ?: "", style = MaterialTheme.typography.bodySmall, color = Neutral600)
                        }
                        Text(
                            text = "${if (transaction.isIncome) "+" else "-"}${currencyFormat.format(transaction.amount)}",
                            color = if (transaction.isIncome) IncomeGreen else ExpenseRed,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            // Espacio para que el FAB no tape la última tarjeta.
            item {
                Spacer(modifier = Modifier.height(88.dp))
            }
        }

        FloatingActionButton(
            onClick = {
                viewModel.loadCategories("EXPENSE")
                showAddSheet = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Icon(Icons.Default.Add, contentDescription = "Nueva transacción")
        }
    }

    if (showAddSheet) {
        val categories by viewModel.categories.collectAsState()
        AddTransactionSheet(
            categories = categories,
            onDismiss = { showAddSheet = false },
            onTypeChange = { viewModel.loadCategories(it) },
            onCreate = { categoryId, type, amount, description, date ->
                viewModel.createTransaction(categoryId, type, amount, description, date = date)
                showAddSheet = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheet(
    categories: List<com.profinanzas.core.domain.model.Category>,
    onDismiss: () -> Unit,
    onTypeChange: (String) -> Unit,
    onCreate: (categoryId: Int, type: String, amount: String, description: String, date: String) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Nueva Transacción", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

            var type by remember { mutableStateOf("EXPENSE") }
            var description by remember { mutableStateOf("") }
            var amount by remember { mutableStateOf("") }
            var selectedCategory by remember { mutableStateOf<com.profinanzas.core.domain.model.Category?>(null) }
            var categoryExpanded by remember { mutableStateOf(false) }
            var date by remember { mutableStateOf(java.time.LocalDate.now().toString()) }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = type == "EXPENSE",
                    onClick = {
                        type = "EXPENSE"
                        selectedCategory = null
                        onTypeChange("EXPENSE")
                    },
                    label = { Text("Gasto") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ExpenseRed.copy(alpha = 0.1f),
                    ),
                )
                FilterChip(
                    selected = type == "INCOME",
                    onClick = {
                        type = "INCOME"
                        selectedCategory = null
                        onTypeChange("INCOME")
                    },
                    label = { Text("Ingreso") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IncomeGreen.copy(alpha = 0.1f),
                    ),
                )
            }

            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it },
            ) {
                OutlinedTextField(
                    value = selectedCategory?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false },
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = {
                                selectedCategory = category
                                categoryExpanded = false
                            },
                        )
                    }
                }
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
                prefix = { Text("$ ") },
                singleLine = true,
            )

            val datePattern = remember { Regex("""\d{4}-\d{2}-\d{2}""") }
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Fecha (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = !datePattern.matches(date),
                supportingText = {
                    if (!datePattern.matches(date)) {
                        Text("Usa el formato YYYY-MM-DD", color = ExpenseRed)
                    }
                },
            )

            Button(
                onClick = {
                    selectedCategory?.let { cat ->
                        onCreate(cat.id, type, amount, description, date)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(),
                enabled = selectedCategory != null && description.isNotBlank() &&
                    amount.toDoubleOrNull() != null && datePattern.matches(date),
            ) {
                Text("Crear Transacción")
            }
        }
    }
}
