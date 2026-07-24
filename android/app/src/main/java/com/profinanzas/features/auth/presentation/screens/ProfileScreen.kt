package com.profinanzas.features.auth.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.profinanzas.core.ui.components.AppCard
import com.profinanzas.core.ui.theme.*
import com.profinanzas.features.auth.presentation.viewmodel.AuthUiState
import com.profinanzas.features.auth.presentation.sheet.EditProfileSheet
import com.profinanzas.features.auth.presentation.sheet.PreferencesSheet

@Composable
fun ProfileScreen(
    uiState: AuthUiState,
    onLogout: () -> Unit,
    onNavigateBack: () -> Unit,
    onUpdateProfile: (firstName: String, lastName: String, username: String) -> Unit = { _, _, _ -> },
) {
    var showEditSheet by remember { mutableStateOf(false) }
    var showPreferencesSheet by remember { mutableStateOf(false) }

    val user = uiState.user
    val initials = remember(user) {
        val first = user?.firstName?.firstOrNull()?.uppercase() ?: ""
        val last = user?.lastName?.firstOrNull()?.uppercase() ?: ""
        if (first.isNotEmpty() || last.isNotEmpty()) {
            "$first$last"
        } else {
            user?.username?.firstOrNull()?.uppercase() ?: "?"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Header with back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
            }
            Text("Perfil", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        }

        // Avatar + info
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Accent),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = initials,
                    color = Bg,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = user?.fullName ?: "Usuario",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = user?.email ?: "email@ejemplo.com",
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
            )
            Text(
                text = "Miembro desde Ene 2025",
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
            )
        }

        // Menu
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Configuración", style = MaterialTheme.typography.bodySmall, color = Muted)

            AppCard(modifier = Modifier.clickable { showEditSheet = true }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Fg, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Editar perfil", modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Muted)
                }
            }

            AppCard(modifier = Modifier.clickable { showPreferencesSheet = true }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = Fg, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Preferencias", modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Muted)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            AppCard(modifier = Modifier.clickable { onLogout() }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, tint = Danger, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Cerrar sesión", color = Danger, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Danger)
                }
            }
        }
    }

    // Sheets
    if (showEditSheet) {
        EditProfileSheet(
            currentFirstName = user?.firstName ?: "",
            currentLastName = user?.lastName ?: "",
            currentUsername = user?.username ?: "",
            onDismiss = { showEditSheet = false },
            onSave = { firstName, lastName, username ->
                onUpdateProfile(firstName, lastName, username)
                showEditSheet = false
            },
        )
    }

    if (showPreferencesSheet) {
        PreferencesSheet(
            onDismiss = { showPreferencesSheet = false },
        )
    }
}
