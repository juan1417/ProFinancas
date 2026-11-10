package com.profinanzas.features.scanner.presentation.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.profinanzas.core.ui.components.AppCard
import com.profinanzas.core.ui.theme.*
import com.profinanzas.features.scanner.presentation.viewmodel.ScannerViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.scanImage(context, it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Camera viewfinder placeholder
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            colors = CardDefaults.cardColors(containerColor = Neutral200),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Neutral500,
                    )
                    Text("Cámara no disponible", color = Neutral600)
                }
            }
        }

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(),
            ) {
                Icon(Icons.Default.Photo, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Galería")
            }
        }

        // Result
        uiState.scannedInvoice?.let { invoice ->
            AppCard {
                Text("Resultado del Escaneo", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

                if (invoice.merchant != null) {
                    Text("Comercio: ${invoice.merchant}", style = MaterialTheme.typography.bodySmall, color = Neutral600)
                }
                Text(
                    "Total detectado: ${currencyFormat.format(invoice.total)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = NavyBlue,
                )

                Spacer(modifier = Modifier.height(12.dp))

                var selectedCategoryId by remember { mutableIntStateOf(0) }

                Text("Categoría:", style = MaterialTheme.typography.bodySmall, color = Neutral600)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp),
                ) {
                    uiState.expenseCategories.take(4).forEach { cat ->
                        FilterChip(
                            selected = selectedCategoryId == cat.id,
                            onClick = { selectedCategoryId = cat.id },
                            label = { Text(cat.name, style = MaterialTheme.typography.labelSmall) },
                        )
                    }
                }

                Button(
                    onClick = {
                        viewModel.saveAsExpense(selectedCategoryId, invoice.total)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen),
                    enabled = !uiState.isSaving && selectedCategoryId > 0 && invoice.total > 0,
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = SurfaceLight, strokeWidth = 2.dp)
                    } else {
                        Text("Guardar como gasto")
                    }
                }
            }
        }

        uiState.savedSuccessfully?.let {
            if (it) {
                Card(colors = CardDefaults.cardColors(containerColor = IncomeGreen.copy(alpha = 0.1f))) {
                    Text(
                        "Gasto guardado exitosamente",
                        modifier = Modifier.padding(16.dp),
                        color = IncomeGreen,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        uiState.error?.let { error ->
            Card(colors = CardDefaults.cardColors(containerColor = ExpenseRed.copy(alpha = 0.1f))) {
                Text(
                    error,
                    modifier = Modifier.padding(16.dp),
                    color = ExpenseRed,
                )
            }
        }
    }
}
