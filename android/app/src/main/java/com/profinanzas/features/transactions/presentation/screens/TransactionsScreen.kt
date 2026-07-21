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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
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

        // FAB
        item {
            Spacer(modifier = Modifier.height(64.dp))
        }
    }

    if (showAddSheet) {
        AddTransactionSheet(
            onDismiss = { showAddSheet = false },
            onCreate = { categoryId, type, amount, description ->
                viewModel.createTransaction(categoryId, type, amount, description)
                showAddSheet = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheet(
    onDismiss: () -> Unit,
    onCreate: (categoryId: Int, type: String, amount: String, description: String) -> Unit,
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

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = type == "EXPENSE",
                    onClick = { type = "EXPENSE" },
                    label = { Text("Gasto") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ExpenseRed.copy(alpha = 0.1f),
                    ),
                )
                FilterChip(
                    selected = type == "INCOME",
                    onClick = { type = "INCOME" },
                    label = { Text("Ingreso") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IncomeGreen.copy(alpha = 0.1f),
                    ),
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
                prefix = { Text("$ ") },
                singleLine = true,
            )

            Button(
                onClick = { onCreate(1, type, amount, description) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                enabled = description.isNotBlank() && amount.toDoubleOrNull() != null,
            ) {
                Text("Crear Transacción")
            }
        }
    }
}
