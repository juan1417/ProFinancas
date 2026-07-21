package com.profinanzas.features.wallet.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.profinanzas.core.ui.theme.*

data class BudgetProgress(
    val id: String,
    val name: String,
    val limit: Double,
    val spent: Double,
) {
    val remaining: Double get() = limit - spent
    val percentage: Float get() = (spent / limit).coerceIn(0.0, 1.0).toFloat()

    val status: BudgetStatus
        get() = when {
            percentage >= 0.9f -> BudgetStatus.DANGER
            percentage >= 0.7f -> BudgetStatus.WARNING
            else -> BudgetStatus.SAFE
        }
}

enum class BudgetStatus { SAFE, WARNING, DANGER }

@Composable
fun BudgetProgressCard(
    budget: BudgetProgress,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val barColor = when (budget.status) {
        BudgetStatus.SAFE -> Accent
        BudgetStatus.WARNING -> Warning
        BudgetStatus.DANGER -> Danger
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = CardDefaults.outlinedCardBorder(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Header: name + limit + edit button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = budget.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Límite semanal",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                    )
                }
                Text(
                    text = "$ ${String.format("%,.0f", budget.limit)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted,
                )
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar presupuesto",
                        modifier = Modifier.size(14.dp),
                        tint = Muted,
                    )
                }
            }

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Border),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = budget.percentage)
                        .clip(RoundedCornerShape(4.dp))
                        .background(barColor)
                )
            }

            // Amounts: spent + remaining
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Gastado: ",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
                Text(
                    text = "$ ${String.format("%,.0f", budget.spent)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Restante: ",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
                Text(
                    text = "$ ${String.format("%,.0f", budget.remaining)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = barColor,
                )
            }
        }
    }
}
