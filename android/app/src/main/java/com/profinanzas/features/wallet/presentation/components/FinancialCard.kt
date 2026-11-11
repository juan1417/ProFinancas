package com.profinanzas.features.wallet.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.profinanzas.core.domain.model.StoredCard

@Composable
fun FinancialCard(
    card: StoredCard,
    modifier: Modifier = Modifier,
) {
    val gradient = when {
        card.brand.contains("Visa", ignoreCase = true) -> Brush.linearGradient(
            listOf(Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFF0F3460))
        )
        card.brand.contains("Master", ignoreCase = true) -> Brush.linearGradient(
            listOf(Color(0xFF1A1A1A), Color(0xFF2D1B1B), Color(0xFF3D2020))
        )
        card.brand.contains("Amex", ignoreCase = true) -> Brush.linearGradient(
            listOf(Color(0xFF0D2137), Color(0xFF143254), Color(0xFF1A4570))
        )
        else -> Brush.linearGradient(
            listOf(Color(0xFF1A1A2E), Color(0xFF16213E))
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(gradient)
            .padding(24.dp),
    ) {
        // Decorative circles (depth effect)
        Box(
            modifier = Modifier
                .size(140.dp)
                .offset(x = 200.dp, y = (-40).dp)
                .clip(RoundedCornerShape(70.dp))
                .background(Color.White.copy(alpha = 0.06f))
        )
        Box(
            modifier = Modifier
                .size(100.dp)
                .offset(x = (-30).dp, y = 150.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(Color.White.copy(alpha = 0.04f))
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Top: chip + network
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                // Chip
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(28.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFC5B358), Color(0xFFC0A030))
                            )
                        )
                )
                Text(
                    text = card.brand.uppercase(),
                    color = Color.White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    letterSpacing = 1.sp,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Card number
            Text(
                text = "•••• •••• •••• ${card.last4}",
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    letterSpacing = 2.sp,
                ),
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom: holder + expiry
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column {
                    Text(
                        "TITULAR",
                        color = Color.White.copy(alpha = 0.4f),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    )
                    Text(
                        card.holderName.ifBlank { "USUARIO" },
                        color = Color.White.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodySmall,
                        letterSpacing = 0.5.sp,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "VENCE",
                        color = Color.White.copy(alpha = 0.4f),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    )
                    Text(
                        card.expiry,
                        color = Color.White.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        ),
                    )
                }
            }
        }
    }
}
