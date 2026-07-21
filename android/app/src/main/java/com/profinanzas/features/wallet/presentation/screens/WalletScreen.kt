package com.profinanzas.features.wallet.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.profinanzas.features.wallet.presentation.components.BudgetProgress
import com.profinanzas.features.wallet.presentation.components.BudgetProgressCard
import com.profinanzas.features.wallet.presentation.components.FinancialCard
import com.profinanzas.features.wallet.presentation.sheet.AddCardSheet
import com.profinanzas.features.wallet.presentation.viewmodel.StoredCard
import com.profinanzas.features.wallet.presentation.viewmodel.WalletViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    viewModel: WalletViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showAddSheet by remember { mutableStateOf(false) }
    var selectedCardTab by remember { mutableIntStateOf(0) }

    val cardTabs = uiState.cards.map { "${it.brand} •${it.last4}" }

    val sampleExpenses = remember(selectedCardTab) {
        when (selectedCardTab) {
            0 -> listOf(
                Triple("Ramen Ichiran", "Hoy", "-$ 2,850"),
                Triple("Metro Tokyo", "Hoy", "-$ 320"),
                Triple("Don Quijote", "Lun", "-$ 3,150"),
                Triple("Toho Cinemas", "Dom", "-$ 2,400"),
                Triple("Farmacia Matsumoto", "Sáb", "-$ 1,890"),
            )
            1 -> listOf(
                Triple("Electricidad TEPCO", "Ayer", "-$ 6,420"),
                Triple("NTT DoCoMo", "Lun", "-$ 4,500"),
                Triple("Agua Municipal", "Viernes", "-$ 2,100"),
            )
            else -> emptyList()
        }
    }

    // Sample budgets (matching prototype)
    val budgets = remember {
        listOf(
            BudgetProgress("1", "Alimentación", 12000.0, 8160.0),
            BudgetProgress("2", "Transporte", 5000.0, 4100.0),
            BudgetProgress("3", "Entretenimiento", 8000.0, 7600.0),
            BudgetProgress("4", "Servicios", 10000.0, 4500.0),
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Tarjetas", style = MaterialTheme.typography.labelMedium, color = Muted)
                    Text("Mis tarjetas", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                }
                TextButton(onClick = { showAddSheet = true }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Agregar")
                }
            }
        }

        // Financial cards
        if (uiState.cards.isEmpty()) {
            item {
                AppCard {
                    Text(
                        "No tienes tarjetas guardadas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Muted,
                    )
                }
            }
        } else {
            items(uiState.cards) { card ->
                FinancialCard(card = card)
            }
        }

        // Add card button
        item {
            TextButton(
                onClick = { showAddSheet = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agregar nueva tarjeta")
            }
        }

        // Budget section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Presupuesto semanal",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                TextButton(onClick = { /* TODO: add budget */ }) {
                    Text("+ Nuevo")
                }
            }
        }

        items(budgets) { budget ->
            BudgetProgressCard(
                budget = budget,
                onEdit = { /* TODO: edit budget */ },
            )
        }

        // Expense history per card
        if (uiState.cards.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Historial de gastos por tarjeta",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                if (cardTabs.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    SegmentedTabs(
                        options = cardTabs,
                        selectedIndex = selectedCardTab,
                        onSelected = { selectedCardTab = it },
                    )
                }
            }

            items(sampleExpenses) { (name, time, amount) ->
                AppCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(name, fontWeight = FontWeight.SemiBold)
                            Text(time, style = MaterialTheme.typography.bodySmall, color = Muted)
                        }
                        Text(
                            amount,
                            style = MaterialTheme.typography.bodyMedium,
                            color = ExpenseRed,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }

        // Bottom spacing
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    // Add card sheet
    if (showAddSheet) {
        AddCardSheet(
            onDismiss = { showAddSheet = false },
            onSave = { brand, bank, last4, expiry, holderName, colorHex ->
                val card = StoredCard(
                    id = System.currentTimeMillis().toString(),
                    brand = brand,
                    bank = bank,
                    last4 = last4,
                    expiry = expiry,
                    holderName = holderName,
                    colorHex = colorHex,
                    createdAt = System.currentTimeMillis(),
                )
                viewModel.addCard(
                    context = context,
                    card = card,
                )
                showAddSheet = false
            },
        )
    }
}
