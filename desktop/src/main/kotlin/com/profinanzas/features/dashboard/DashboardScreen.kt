package com.profinanzas.features.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.profinanzas.core.domain.model.CategoryBreakdown
import com.profinanzas.core.domain.model.Transaction
import com.profinanzas.core.ui.formatRelativeDate
import com.profinanzas.core.ui.formatMonthYear
import com.profinanzas.core.ui.components.AppCard
import com.profinanzas.core.ui.components.StatusPill
import com.profinanzas.core.ui.theme.*
import java.text.NumberFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

private val categoryColors = listOf(CatFood, CatTransport, CatServices, CatShopping, CatHealth, Accent, Warning, Danger)

@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val summary = uiState.summary
    val income = summary?.totalIncome ?: 0.0
    val expenses = summary?.totalExpenses ?: 0.0
    val balance = summary?.balance ?: 0.0
    val recentTransactions = uiState.recentTransactions
    val budgets = uiState.budgets
    val monthYear = formatMonthYear()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(vertical = 24.dp),
    ) {
        // Stats Grid - 4 columns
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                StatCard("Ingresos", "+$ ${String.format("%,.0f", income)}", Accent, monthYear, Modifier.weight(1f))
                StatCard("Gastos", "-$ ${String.format("%,.0f", expenses)}", Danger, monthYear, Modifier.weight(1f))
                StatCard("Balance", "$ ${String.format("%,.0f", balance)}", if (balance >= 0) Accent else Danger, "Disponible", Modifier.weight(1f))
                StatCard("Transacciones", "${summary?.transactionCount ?: 0}", Warning, monthYear, Modifier.weight(1f))
            }
        }

        // Charts Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Bar Chart - uses real transactions
                Box(modifier = Modifier.weight(2f)) {
                    WeeklyBarChart(recentTransactions)
                }
                // Donut - uses real byCategory
                Box(modifier = Modifier.weight(1f)) {
                    DonutChart(summary?.byCategory ?: emptyList(), expenses)
                }
            }
        }

        // Recent Transactions + Budget Progress
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Recent Transactions
                Box(modifier = Modifier.weight(1f)) {
                    AppCard {
                        Text("TRANSACCIONES RECIENTES", style = MaterialTheme.typography.labelSmall, color = Muted)
                        Spacer(modifier = Modifier.height(16.dp))
                        if (recentTransactions.isEmpty()) {
                            Text("Sin transacciones recientes", style = MaterialTheme.typography.bodySmall, color = Muted)
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
                                    tx.description,
                                    formatRelativeDate(tx.transactionDate),
                                    tx.signedAmount,
                                    color,
                                    icon,
                                )
                            }
                        }
                    }
                }

                // Budget Progress
                Box(modifier = Modifier.weight(1f)) {
                    AppCard {
                        Text("Presupuestos", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(16.dp))
                        if (budgets.isEmpty()) {
                            Text("Sin presupuestos activos", style = MaterialTheme.typography.bodySmall, color = Muted)
                        } else {
                            budgets.take(4).forEach { budget ->
                                BudgetItem(
                                    budget.categoryName ?: "General",
                                    budget.amount,
                                    budget.spent,
                                )
                            }
                        }
                    }
                }
            }
        }

        // Balance Summary
        item {
            AppCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Balance disponible", style = MaterialTheme.typography.bodySmall, color = Muted)
                    val balanceColor = when {
                        balance > 0 -> Accent
                        balance < 0 -> Danger
                        else -> Warning
                    }
                    StatusPill(
                        text = if (balance > 0) "Saludable" else if (balance < 0) "Déficit" else "Neutro",
                        color = balanceColor,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$ ${String.format("%,.0f", balance)}",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    valueColor: Color,
    subtitle: String,
    modifier: Modifier = Modifier,
    valueStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.displayLarge,
) {
    AppCard(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Muted)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = valueStyle,
            color = valueColor,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Muted)
    }
}

@Composable
private fun WeeklyBarChart(transactions: List<Transaction>) {
    val today = LocalDate.now(ZoneId.of("America/Bogota"))
    val dayOfWeek = today.dayOfWeek.value // 1=Monday..7=Sunday

    // Build last 7 days labels
    val days = (1..7).map { offset ->
        val date = today.minusDays((7 - offset).toLong())
        date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("es", "CO"))
    }

    // Compute expenses per day from transactions
    val dailyExpenses = mutableMapOf<Int, Double>()
    transactions.filter { it.isExpense }.forEach { tx ->
        try {
            val txDate = LocalDate.parse(tx.transactionDate.substring(0, 10))
            val daysAgo = java.time.temporal.ChronoUnit.DAYS.between(txDate, today)
            if (daysAgo in 0..6) {
                val idx = (daysAgo.toInt())
                dailyExpenses[6 - idx] = (dailyExpenses[6 - idx] ?: 0.0) + tx.amount
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
                .height(180.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            days.forEachIndexed { index, day ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    Text(
                        text = if (values[index] > 0) "$${String.format("%.0f", values[index] / 1000)}k" else "$0",
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(((if (maxValue > 0) values[index] / maxValue else 0.0) * 140).coerceAtLeast(2.0).dp)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 2.dp, bottomEnd = 2.dp))
                            .background(AccentSoft)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(day, style = MaterialTheme.typography.labelSmall, color = Muted)
                }
            }
        }
    }
}

@Composable
private fun DonutChart(byCategory: List<CategoryBreakdown>, totalExpenses: Double) {
    val expenseCategories = byCategory.filter { it.categoryType == "EXPENSE" && it.totalAmount > 0 }
    val total = expenseCategories.sumOf { it.totalAmount }.coerceAtLeast(1.0)

    AppCard {
        Text("Por categoría", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        if (expenseCategories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text("Sin datos", color = Muted)
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                ) {
                    val strokeWidth = 24.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val center = Offset(size.width / 2f, size.height / 2f)

                    drawCircle(
                        color = Border,
                        radius = radius,
                        center = center,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                    )

                    var startAngle = -90f
                    expenseCategories.forEachIndexed { index, cat ->
                        val pct = cat.totalAmount / total
                        val sweep = (pct * 360).toFloat()
                        val color = categoryColors.getOrElse(index) { Accent }
                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2),
                        )
                        startAngle += sweep
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$${String.format("%.1f", total / 1000)}k",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text("Total", style = MaterialTheme.typography.labelSmall, color = Muted)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            expenseCategories.forEachIndexed { index, cat ->
                val pct = (cat.totalAmount / total * 100)
                val color = categoryColors.getOrElse(index) { Accent }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(color),
                        )
                        Text(cat.categoryName ?: "Otros", style = MaterialTheme.typography.bodySmall, color = Muted)
                    }
                    Text(
                        "${String.format("%.1f", pct)}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
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
