package com.profinanzas.features.scanner.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.profinanzas.core.domain.model.Category
import com.profinanzas.core.domain.usecase.category.ListCategoriesUseCase
import com.profinanzas.core.domain.usecase.transaction.CreateTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class ScannedInvoice(
    val total: Double,
    val merchant: String? = null,
    val rawText: String,
)

data class ScannerUiState(
    val scannedInvoice: ScannedInvoice? = null,
    val expenseCategories: List<Category> = emptyList(),
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val listCategories: ListCategoriesUseCase,
    private val createTransaction: CreateTransactionUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            listCategories(type = "EXPENSE", isActive = true)
                .onSuccess { categories ->
                    _uiState.value = _uiState.value.copy(expenseCategories = categories)
                }
        }
    }

    fun scanImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val image = InputImage.fromFilePath(context, uri)
                val result = recognizer.process(image).await()
                val text = result.text
                val invoice = parseInvoice(text)
                _uiState.value = _uiState.value.copy(scannedInvoice = invoice)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Error al escanear: ${e.message}")
            }
        }
    }

    fun saveAsExpense(categoryId: Int, amount: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            val now = java.time.ZonedDateTime.now(java.time.ZoneId.of("America/Bogota"))
                .format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            createTransaction(
                categoryId = categoryId,
                type = "EXPENSE",
                amount = amount.toString(),
                description = _uiState.value.scannedInvoice?.merchant ?: "Factura escaneada",
                notes = _uiState.value.scannedInvoice?.rawText?.take(500),
                transactionDate = now,
            )
            .onSuccess {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    savedSuccessfully = true,
                    scannedInvoice = null,
                )
                loadCategories()
            }
            .onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message,
                )
            }
        }
    }

    private fun parseInvoice(text: String): ScannedInvoice {
        val totalPatterns = listOf(
            Regex("(?i)(total|importe|monto|suma|total a pagar)[\\s:]*\\$?\\s*([\\d,.]+)"),
            Regex("\\$\\s*([\\d,.]+)"),
        )

        var total = 0.0
        for (pattern in totalPatterns) {
            val match = pattern.findAll(text).firstOrNull()
            if (match != null) {
                val valueStr = match.groupValues.last().replace(",", "").replace(".", "")
                val value = valueStr.toDoubleOrNull()
                if (value != null && value > total) {
                    total = value / 100.0
                }
            }
        }

        val merchant = text.lines().firstOrNull { it.length in 3..50 && !it.contains(Regex("\\d{4}")) }

        return ScannedInvoice(total = total, merchant = merchant, rawText = text)
    }

    fun clearResult() {
        _uiState.value = _uiState.value.copy(scannedInvoice = null, savedSuccessfully = false, error = null)
    }
}
