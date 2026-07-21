package com.profinanzas.features.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.profinanzas.core.ui.components.AppCard
import com.profinanzas.core.ui.components.StatusPill
import com.profinanzas.core.ui.theme.*

private val categoryColors = listOf(CatFood, CatTransport, CatServices, CatShopping, CatHealth)
private val categoryNames = listOf("Alimentación", "Transporte", "Servicios", "Compras", "Salud")
private val categoryPercentages = listOf(35, 25, 20, 12, 8)

@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val summary = uiState.summary
    val income = summary?.totalIncome ?: 0.0
    val expenses = summary?.totalExpenses ?: 0.0
    val balance = summary?.balance ?: 0.0

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
                StatCard("Ingresos", "+$ ${String.format("%,.0f", income)}", Accent, "+12.5% vs mes anterior", Modifier.weight(1f))
                StatCard("Gastos", "-$ ${String.format("%,.0f", expenses)}", Danger, "+8.3% vs mes anterior", Modifier.weight(1f))
                StatCard("Balance", "$ ${String.format("%,.0f", balance)}", Warning, "Saludable", Modifier.weight(1f))
                StatCard("Facturas pendientes", "4", Warning, "\$ 284.67 por pagar", Modifier.weight(1f))
            }
        }

        // Charts Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Bar Chart
                Box(modifier = Modifier.weight(2f)) {
                    WeeklyBarChart()
                }
                // Donut
                Box(modifier = Modifier.weight(1f)) {
                    DonutChart()
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
                        RecentTransactionItem("Ramen Ichiran", "Hoy, 12:30", -2850.0, CatFood, Icons.Default.Restaurant)
                        RecentTransactionItem("Metro Tokyo", "Hoy, 08:15", -320.0, CatTransport, Icons.Default.Train)
                        RecentTransactionItem("Depósito freelance", "Ayer, 16:45", 12000.0, Accent, Icons.Default.AccountBalance)
                        RecentTransactionItem("Electricidad TEPCO", "Ayer, 09:00", -6420.0, CatServices, Icons.Default.Bolt)
                        RecentTransactionItem("Don Quijote", "Lun, 19:20", -3150.0, CatShopping, Icons.Default.ShoppingBag)
                    }
                }

                // Budget Progress
                Box(modifier = Modifier.weight(1f)) {
                    AppCard {
                        Text("Presupuestos", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(16.dp))
                        BudgetItem("Alimentación", 450000.0, 320000.0)
                        BudgetItem("Transporte", 180000.0, 145000.0)
                        BudgetItem("Servicios", 250000.0, 248000.0)
                        BudgetItem("Entretenimiento", 120000.0, 89000.0)
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
private fun WeeklyBarChart() {
    val days = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
    val values = listOf(2100.0, 3800.0, 1500.0, 4700.0, 2900.0, 5200.0, 1800.0)
    val maxValue = values.max()

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
                        text = "$${String.format("%.1f", values[index] / 1000)}k",
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((values[index] / maxValue * 140).dp)
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
private fun DonutChart() {
    AppCard {
        Text("Por categoría", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

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
                categoryPercentages.forEach { pct ->
                    val sweep = pct * 3.6f
                    drawArc(
                        color = categoryColors[categoryPercentages.indexOf(pct)],
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
                    text = "$${String.format("%.1f", 32.1)}k",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text("Total", style = MaterialTheme.typography.labelSmall, color = Muted)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        categoryNames.forEachIndexed { index, name ->
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
                            .background(categoryColors[index]),
                    )
                    Text(name, style = MaterialTheme.typography.bodySmall, color = Muted)
                }
                Text(
                    "${categoryPercentages[index]}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                )
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
