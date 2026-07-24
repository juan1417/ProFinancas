package com.profinanzas.features.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.profinanzas.core.domain.model.Category
import com.profinanzas.core.ui.components.AppCard
import com.profinanzas.core.ui.theme.*

@Composable
fun CategoriesScreen(viewModel: CategoriesViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Todos", "Ingresos", "Gastos")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("CATEGORÍAS", style = MaterialTheme.typography.labelSmall, color = Accent)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Gestión de categorías", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Nueva", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            }
        }

        // Tabs
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            tabs.forEachIndexed { index, label ->
                FilterChip(
                    selected = selectedTab == index,
                    onClick = {
                        selectedTab = index
                        val type = when (index) {
                            1 -> "INCOME"
                            2 -> "EXPENSE"
                            else -> null
                        }
                        viewModel.loadCategories(type)
                    },
                    label = { Text(label) },
                )
            }
        }

        // Category list
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Accent)
            }
        } else if (uiState.categories.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay categorías", color = Muted)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.categories) { category ->
                    AppCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                if (category.isIncome) Icons.Default.AddCircle else Icons.Default.RemoveCircle,
                                contentDescription = null,
                                tint = if (category.isIncome) Accent else Danger,
                                modifier = Modifier.size(24.dp),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(category.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                if (category.description != null) {
                                    Text(category.description, style = MaterialTheme.typography.bodySmall, color = Muted)
                                } else {
                                    Text("${category.transactionsCount} transacciones", style = MaterialTheme.typography.bodySmall, color = Muted)
                                }
                            }
                            IconButton(
                                onClick = { editingCategory = category },
                                modifier = Modifier.size(32.dp),
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Muted, modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = { viewModel.deleteCategory(category.id) },
                                modifier = Modifier.size(32.dp),
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Danger, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Create Category Dialog
    if (showCreateDialog) {
        CreateCategoryDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, type, description ->
                viewModel.createCategory(name, type, description)
                showCreateDialog = false
            },
        )
    }

    editingCategory?.let { category ->
        EditCategoryDialog(
            category = category,
            onDismiss = { editingCategory = null },
            onSave = { name, type, description ->
                viewModel.updateCategory(category.id, name, type, description)
                editingCategory = null
            },
        )
    }
}

@Composable
private fun CreateCategoryDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, type: String, description: String?) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("EXPENSE") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Categoría") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == "EXPENSE",
                        onClick = { type = "EXPENSE" },
                        label = { Text("Gasto") },
                    )
                    FilterChip(
                        selected = type == "INCOME",
                        onClick = { type = "INCOME" },
                        label = { Text("Ingreso") },
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre (mín. 3 caracteres)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(name, type, description.ifBlank { null }) },
                enabled = name.length >= 3,
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
    )
}

@Composable
private fun EditCategoryDialog(
    category: Category,
    onDismiss: () -> Unit,
    onSave: (name: String, type: String, description: String?) -> Unit,
) {
    var name by remember { mutableStateOf(category.name) }
    var type by remember { mutableStateOf(category.type) }
    var description by remember { mutableStateOf(category.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Categoría") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == "EXPENSE",
                        onClick = { type = "EXPENSE" },
                        label = { Text("Gasto") },
                    )
                    FilterChip(
                        selected = type == "INCOME",
                        onClick = { type = "INCOME" },
                        label = { Text("Ingreso") },
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre (mín. 3 caracteres)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, type, description.ifBlank { null }) },
                enabled = name.length >= 3,
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
    )
}
