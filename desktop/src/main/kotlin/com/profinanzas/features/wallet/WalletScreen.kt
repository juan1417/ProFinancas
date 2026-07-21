package com.profinanzas.features.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.profinanzas.core.ui.components.AppCard
import com.profinanzas.core.ui.theme.*

@Composable
fun WalletScreen(viewModel: WalletViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Text(
                text = "Mis Tarjetas",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                uiState.cards.forEach { card ->
                    FinancialCard(
                        card = card,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            AppCard {
                Text("Presupuestos", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                BudgetProgressBar("Alimentación", 320000.0, 450000.0)
                BudgetProgressBar("Transporte", 145000.0, 180000.0)
                BudgetProgressBar("Servicios", 248000.0, 250000.0)
                BudgetProgressBar("Entretenimiento", 89000.0, 120000.0)
            }
        }
    }
}

@Composable
private fun FinancialCard(
    card: WalletCard,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .height(200.dp)
            .clickable {},
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(card.gradientStart),
                            Color(card.gradientEnd),
                        )
                    )
                )
                .padding(24.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = card.cardType.uppercase(),
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Text(
                        text = "•••• •••• •••• ${card.lastFour}",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }

                Column {
                    Text(
                        text = card.name,
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetProgressBar(name: String, spent: Double, total: Double) {
    val pct = if (total > 0) (spent / total) else 0.0
    val barColor = when {
        pct < 0.7 -> Accent
        pct < 0.9 -> Warning
        else -> Danger
    }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(
                "${String.format("%,.0f", spent)} / ${String.format("%,.0f", total)}",
                style = MaterialTheme.typography.labelMedium,
                color = Muted,
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Border)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(pct.toFloat().coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(barColor)
            )
        }
    }
}
