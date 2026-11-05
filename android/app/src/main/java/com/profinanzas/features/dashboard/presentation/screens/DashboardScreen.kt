package com.profinanzas.features.dashboard.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.profinanzas.core.ui.formatDate
import com.profinanzas.core.ui.formatMonthYear
import com.profinanzas.core.ui.formatRelativeDate
import com.profinanzas.core.ui.components.AppCard
import com.profinanzas.core.ui.components.StatusPill
import com.profinanzas.core.ui.theme.*
import com.profinanzas.features.dashboard.presentation.viewmodel.DashboardViewModel
import java.text.NumberFormat
import java.util.Locale

private val categoryColors = listOf(
    CatFood, CatTransport, CatServices, CatShopping, CatHealth
)

private val categoryNames = listOf(
    "Alimentación", "Transporte", "Servicios", "Compras", "Salud"
)

@Composable
fun DashboardScreen(
    onNavigateToProfile: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val summary = uiState.summary
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    val income = summary?.totalIncome ?: 0.0
    val expenses = summary?.totalExpenses ?: 0.0
    val balance = summary?.balance ?: 0.0
    val recentTransactions = uiState.recentTransactions
    val budgets = uiState.budgets
    val monthYear = formatMonthYear()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(vertical = 24.dp),
    ) {
        // ─── Eyebrow + Title ───────────────────────────────────────────
        item {
            Column {
                Text(
                    text = "RESUMEN · ${monthYear.uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Accent,
                    letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tu mes en números",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        // ─── Income / Expenses Grid ────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Ingresos
                AppCard(modifier = Modifier.weight(1f)) {
                    Text("Ingresos", style = MaterialTheme.typography.bodySmall, color = Muted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "+$ ${String.format("%,.0f", income)}",
                        style = MaterialTheme.typography.displayLarge,
                        color = Accent,
                        fontWeight = FontWeight.Bold,
                    )
                }
                // Gastos
                AppCard(modifier = Modifier.weight(1f)) {
                    Text("Gastos", style = MaterialTheme.typography.bodySmall, color = Muted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "-$ ${String.format("%,.0f", expenses)}",
                        style = MaterialTheme.typography.displayLarge,
                        color = Danger,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        // ─── Balance Card ──────────────────────────────────────────────
        item {
            AppCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Balance disponible", style = MaterialTheme.typography.bodySmall, color = Muted)
                    StatusPill(text = "Saludable", color = Accent)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$ ${String.format("%,.0f", balance)}",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        // ─── Weekly Bar Chart ──────────────────────────────────────────
        item {
            WeeklyBarChart(uiState.recentTransactions)
        }

        // ─── Recent Transactions + Budget Progress ─────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Recent transactions
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TRANSACCIONES RECIENTES",
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (recentTransactions.isEmpty()) {
                        Text(
                            text = "Sin transacciones recientes",
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted,
                        )
                    } else {
                        recentTransactions.forEach { tx ->
                            val icon = when {
                                tx.isIncome -> Icons.Default.AccountBalance
                                tx.categoryName?.contains("Alimentación", true) == true -> Icons.Default.Restaurant
                                tx.categoryName?.contains("Transporte", true) == true -> Icons.Default.Train
                                tx.categoryName?.contains("Servicio", true) == true -> Icons.Default.Bolt
                                else -> Icons.Default.ShoppingBag
                            }
                            val color = when {
                                tx.isIncome -> Accent
                                tx.categoryName?.contains("Alimentación", true) == true -> CatFood
                                tx.categoryName?.contains("Transporte", true) == true -> CatTransport
                                tx.categoryName?.contains("Servicio", true) == true -> CatServices
                                else -> CatShopping
                            }
                            RecentTransactionItem(
                                title = tx.description,
                                subtitle = formatRelativeDate(tx.transactionDate),
                                amount = tx.signedAmount,
                                categoryColor = color,
                                icon = icon,
                            )
                        }
                    }
                }
                // Budget progress
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "PRESUPUESTOS",
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (budgets.isEmpty()) {
                        Text(
                            text = "Sin presupuestos activos",
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted,
                        )
                    } else {
                        budgets.take(4).forEach { budget ->
                            BudgetItem(
                                name = budget.categoryName ?: "General",
                                total = budget.amount,
                                spent = budget.spent,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeeklyBarChart(transactions: List<com.profinanzas.core.domain.model.Transaction> = emptyList()) {
    val today = java.time.LocalDate.now(java.time.ZoneId.of("America/Bogota"))
    val days = (6 downTo 0).map { offset ->
        today.minusDays(offset.toLong()).dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale("es", "CO"))
    }

    val dailyExpenses = mutableMapOf<Int, Double>()
    transactions.filter { it.isExpense }.forEach { tx ->
        try {
            val txDate = java.time.LocalDate.parse(tx.transactionDate.substring(0, 10))
            val daysAgo = java.time.temporal.ChronoUnit.DAYS.between(txDate, today).toInt()
            if (daysAgo in 0..6) {
                val idx = 6 - daysAgo
                dailyExpenses[idx] = (dailyExpenses[idx] ?: 0.0) + tx.amount
            }
        } catch (_: Exception) { }
    }

    val values = (0..6).map { dailyExpenses[it] ?: 0.0 }
    val maxValue = values.max().coerceAtLeast(1.0)

    AppCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Gastos semanales", style = MaterialTheme.typography.headlineSmall)
            Text("Últimos 7 días", style = MaterialTheme.typography.bodySmall, color = Muted)
        }
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            days.forEachIndexed { index, day ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    Text(
                        text = if (values[index] > 0) "$${String.format("%.1f", values[index] / 1000)}k" else "$0",
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((values[index] / maxValue * 120).dp)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 2.dp, bottomEnd = 2.dp))
                            .background(AccentSoft)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentTransactionItem(
    title: String,
    subtitle: String,
    amount: Double,
    categoryColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    Row(
        modifier = Modifier.padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(categoryColor.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = categoryColor, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Muted)
        }
        Text(
            text = "${if (amount > 0) "+" else "-"}$ ${String.format("%,.0f", kotlin.math.abs(amount))}",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (amount > 0) Accent else Danger,
        )
    }
}

@Composable
private fun BudgetItem(name: String, total: Double, spent: Double) {
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
