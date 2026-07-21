package com.profinanzas.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.profinanzas.core.ui.theme.*

@Composable
fun SegmentedTabs(
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Surface,
        border = ButtonDefaults.outlinedButtonBorder(enabled = true).let {
            it.copy(brush = androidx.compose.ui.graphics.SolidColor(Border))
        },
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            options.forEachIndexed { index, label ->
                val isActive = selectedIndex == index
                Surface(
                    onClick = { onSelected(index) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = if (isActive) Fg else androidx.compose.ui.graphics.Color.Transparent,
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = if (isActive) Bg else Muted,
                    )
                }
            }
        }
    }
}
