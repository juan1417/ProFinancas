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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.profinanzas.core.ui.components.AppCard
import com.profinanzas.core.ui.components.ErrorBanner
import com.profinanzas.core.ui.theme.*
import com.profinanzas.features.wallet.presentation.components.BudgetProgress
import com.profinanzas.features.wallet.presentation.components.BudgetProgressCard
import com.profinanzas.features.wallet.presentation.components.FinancialCard
import com.profinanzas.features.wallet.presentation.sheet.AddCardSheet
import com.profinanzas.core.domain.model.StoredCard
import com.profinanzas.features.wallet.presentation.viewmodel.WalletViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    viewModel: WalletViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ErrorBanner(message = uiState.error, onDismiss = viewModel::clearError)
        }
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
                    "Presupuestos",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        if (uiState.budgets.isEmpty()) {
            item {
                AppCard {
                    Text(
                        "No hay presupuestos activos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Muted,
                    )
                }
            }
        } else {
            items(uiState.budgets) { budget ->
                BudgetProgressCard(
                    budget = BudgetProgress(
                        id = budget.id.toString(),
                        name = budget.categoryName ?: "General",
                        limit = budget.amount,
                        spent = budget.spent,
                    ),
                    onEdit = { /* TODO: edit budget */ },
                )
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
                viewModel.addCard(card)
                showAddSheet = false
            },
        )
    }
}
