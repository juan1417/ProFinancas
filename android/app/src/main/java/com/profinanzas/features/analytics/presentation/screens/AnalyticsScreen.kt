package com.profinanzas.features.analytics.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.profinanzas.core.ui.components.AppCard
import com.profinanzas.core.ui.components.ErrorBanner
import com.profinanzas.core.ui.components.PercentageBadge
import com.profinanzas.core.ui.components.SegmentedTabs
import com.profinanzas.core.ui.theme.*
import com.profinanzas.features.analytics.presentation.viewmodel.AnalyticsViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedPeriod by remember { mutableIntStateOf(1) }
    val periods = listOf("Semanal", "Mensual", "Anual")
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    val maxBarAmount = uiState.monthlyData.maxOfOrNull { maxOf(it.income, it.expense) } ?: 1.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SegmentedTabs(
            options = periods,
            selectedIndex = selectedPeriod,
            onSelected = { selectedPeriod = it },
        )

        ErrorBanner(message = uiState.error)

        // KPI Row
        val summary = uiState.summary
        val totalTransactions = summary?.transactionCount ?: 0
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppCard(modifier = Modifier.weight(1f)) {
                Text("Ingresos", style = MaterialTheme.typography.labelSmall, color = Neutral600)
                Text(
                    currencyFormat.format(summary?.totalIncome ?: 0.0),
                    color = IncomeGreen,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                )
                if (totalTransactions > 0) {
                    Text(
                        "${summary?.byCategory?.filter { it.categoryType == "INCOME" }?.size ?: 0} categorías",
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral600,
                    )
                }
            }
            AppCard(modifier = Modifier.weight(1f)) {
                Text("Gastos", style = MaterialTheme.typography.labelSmall, color = Neutral600)
                Text(
                    currencyFormat.format(summary?.totalExpenses ?: 0.0),
                    color = ExpenseRed,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                )
                if (totalTransactions > 0) {
                    Text(
                        "${summary?.byCategory?.filter { it.categoryType == "EXPENSE" }?.size ?: 0} categorías",
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral600,
                    )
                }
            }
        }

        // Net Balance
        AppCard {
            Text("Balance Neto", style = MaterialTheme.typography.labelSmall, color = Neutral600)
            Text(
                currencyFormat.format(summary?.balance ?: 0.0),
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.headlineMedium,
            )
            if (totalTransactions > 0) {
                Text(
                    "$totalTransactions transacciones",
                    style = MaterialTheme.typography.labelSmall,
                    color = Neutral600,
                )
            }
        }

        // Bar Chart
        AppCard {
            Text("Ingresos vs Gastos", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom,
            ) {
                uiState.monthlyData.forEach { data ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.height(140.dp),
                        ) {
                            // Income bar
                            Box(
                                modifier = Modifier
                                    .width(12.dp)
                                    .fillMaxHeight((data.income / maxBarAmount).toFloat().coerceIn(0f, 1f))
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(NavyBlue)
                            )
                            // Expense bar
                            Box(
                                modifier = Modifier
                                    .width(12.dp)
                                    .fillMaxHeight((data.expense / maxBarAmount).toFloat().coerceIn(0f, 1f))
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(ExpenseRed)
                            )
                        }
                        Text(data.month, style = MaterialTheme.typography.labelSmall, color = Neutral600)
                    }
                }
            }
        }

        // Category Breakdown
        if (summary?.byCategory?.isNotEmpty() == true) {
            AppCard {
                Text("Desglose por Categoría", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(12.dp))
                val expenseCategories = summary.byCategory.filter { it.categoryType == "EXPENSE" }
                val maxAmount = expenseCategories.maxOfOrNull { it.totalAmount } ?: 1.0
                expenseCategories.forEach { item ->
                    val widthFraction = (item.totalAmount / maxAmount.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = item.categoryName ?: "Sin categoría",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            text = currencyFormat.format(item.totalAmount),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    LinearProgressIndicator(
                        progress = { widthFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = NavyBlue,
                        trackColor = Neutral200,
                    )
                }
            }
        }
    }
}
