package com.profinanzas.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.profinanzas.core.ui.theme.*

data class SidebarItem(
    val label: String,
    val icon: ImageVector,
    val route: String,
)

data class SidebarUser(
    val initials: String,
    val name: String,
    val email: String,
)

@Composable
fun Sidebar(
    items: List<SidebarItem>,
    selectedRoute: String,
    onItemSelected: (String) -> Unit,
    user: SidebarUser?,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(260.dp)
            .fillMaxHeight()
            .background(Surface)
            .padding(16.dp),
    ) {
        // Brand
        Text(
            text = "profinanzas",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Accent,
            modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 24.dp),
        )

        // Nav links
        items.forEach { item ->
            val isSelected = item.route == selectedRoute
            val bgColor = if (isSelected) Accent.copy(alpha = 0.14f) else MaterialTheme.colorScheme.background
            val textColor = if (isSelected) Accent else Muted

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor)
                    .clickable { onItemSelected(item.route) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    item.icon,
                    contentDescription = item.label,
                    tint = textColor,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        // User section
        user?.let { u ->
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(start = 12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Neutral300),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = u.initials,
                            color = Fg,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Column {
                        Text(
                            text = u.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = u.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onLogout() }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        Icons.Default.Logout,
                        contentDescription = "Cerrar sesión",
                        tint = Muted,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "Cerrar sesión",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                    )
                }
            }
        }
    }
}
