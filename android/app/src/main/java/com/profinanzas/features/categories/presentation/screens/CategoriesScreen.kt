package com.profinanzas.features.categories.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.profinanzas.core.domain.model.Category
import com.profinanzas.core.ui.components.AppCard
import com.profinanzas.core.ui.components.ErrorBanner
import com.profinanzas.core.ui.components.SegmentedTabs
import com.profinanzas.core.ui.theme.*
import com.profinanzas.features.categories.presentation.viewmodel.CategoryViewModel

@Composable
fun CategoriesScreen(
    viewModel: CategoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showCreateSheet by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    val tabs = listOf("Todos", "Ingresos", "Gastos")

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateSheet = true },
                containerColor = NavyBlue,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear categoría")
            }
        },
    ) { padding ->
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            ErrorBanner(message = uiState.error, onDismiss = viewModel::clearError)
        }

        item {
            SegmentedTabs(
                options = tabs,
                selectedIndex = selectedTab,
                onSelected = { selectedTab = it },
            )
        }

        val filteredCategories = when (selectedTab) {
            1 -> uiState.categories.filter { it.isIncome }
            2 -> uiState.categories.filter { it.isExpense }
            else -> uiState.categories
        }

        items(filteredCategories) { category ->
            CategoryTile(
                category = category,
                onEdit = { editingCategory = category },
                onDelete = { viewModel.delete(category.id) },
            )
        }

        item {
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
    }

    if (showCreateSheet) {
        CreateCategorySheet(
            onDismiss = { showCreateSheet = false },
            onCreate = { name, type, description ->
                viewModel.create(name, type, description)
                showCreateSheet = false
            },
        )
    }

    editingCategory?.let { category ->
        EditCategorySheet(
            category = category,
            onDismiss = { editingCategory = null },
            onSave = { name, type, description ->
                viewModel.update(category.id, name, type, description)
                editingCategory = null
            },
        )
    }
}

@Composable
fun CategoryTile(
    category: Category,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    AppCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                if (category.isIncome) Icons.Default.AddCircle else Icons.Default.RemoveCircle,
                contentDescription = null,
                tint = if (category.isIncome) IncomeGreen else ExpenseRed,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(category.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                if (category.description != null) {
                    Text(category.description, style = MaterialTheme.typography.bodySmall, color = Neutral600)
                } else {
                    Text("${category.transactionsCount} transacciones", style = MaterialTheme.typography.bodySmall, color = Neutral600)
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Neutral500)
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = ExpenseRed)
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar categoría") },
            text = { Text("¿Estás seguro de eliminar \"${category.name}\"?") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                    Text("Eliminar", color = ExpenseRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCategorySheet(
    onDismiss: () -> Unit,
    onCreate: (name: String, type: String, description: String?) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Nueva Categoría", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

            var name by remember { mutableStateOf("") }
            var type by remember { mutableStateOf("EXPENSE") }
            var description by remember { mutableStateOf("") }

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

            Button(
                onClick = { onCreate(name, type, description.ifBlank { null }) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                enabled = name.length >= 3,
            ) {
                Text("Crear Categoría")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCategorySheet(
    category: Category,
    onDismiss: () -> Unit,
    onSave: (name: String, type: String, description: String?) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Editar Categoría", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

            var name by remember { mutableStateOf(category.name) }
            var type by remember { mutableStateOf(category.type) }
            var description by remember { mutableStateOf(category.description ?: "") }

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

            Button(
                onClick = { onSave(name, type, description.ifBlank { null }) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                enabled = name.length >= 3,
            ) {
                Text("Guardar Cambios")
            }
        }
    }
}
