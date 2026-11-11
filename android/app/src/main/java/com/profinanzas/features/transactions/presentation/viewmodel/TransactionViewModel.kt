package com.profinanzas.features.transactions.presentation.viewmodel

import android.content.Context
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profinanzas.core.domain.model.Category
import com.profinanzas.core.domain.model.Summary
import com.profinanzas.core.domain.model.Transaction
import com.profinanzas.core.domain.usecase.category.ListCategoriesUseCase
import com.profinanzas.core.domain.usecase.transaction.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class TransactionUiState(
    val transactions: List<Transaction> = emptyList(),
    val summary: Summary? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val exportMessage: String? = null,
    val isExporting: Boolean = false,
)

@HiltViewModel
class TransactionViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getTransactions: GetTransactionsUseCase,
    private val createTransactionUseCase: CreateTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val getSummary: GetSummaryUseCase,
    private val listCategories: ListCategoriesUseCase,
    private val exportTransactionsUseCase: ExportTransactionsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    init {
        loadTransactions()
        loadSummary()
        loadCategories("EXPENSE")
    }

    fun loadTransactions(type: String? = null, search: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            getTransactions(type = type, search = search)
                .onSuccess { transactions ->
                    _uiState.value = _uiState.value.copy(
                        transactions = transactions,
                        isLoading = false,
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message,
                    )
                }
        }
    }

    fun loadSummary(period: String = "month") {
        viewModelScope.launch {
            getSummary(period)
                .onSuccess { summary ->
                    _uiState.value = _uiState.value.copy(summary = summary)
                }
                .onFailure { /* silently fail */ }
        }
    }

    fun createTransaction(
        categoryId: Int,
        type: String,
        amount: String,
        description: String,
        notes: String? = null,
        date: String? = null,
    ) {
        viewModelScope.launch {
            val zone = java.time.ZoneId.of("America/Bogota")
            val now = java.time.ZonedDateTime.now(zone)
            val whenValue = date
                ?.takeIf { it.isNotBlank() }
                ?.let { raw ->
                    runCatching { java.time.LocalDate.parse(raw) }.getOrNull()
                }
                ?.let { day ->
                    if (day == now.toLocalDate()) now
                    else day.atTime(12, 0).atZone(zone)
                }
                ?: now
            val transactionDate = whenValue
                .format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            createTransactionUseCase(categoryId, type, amount, description, notes, transactionDate)
                .onSuccess { transaction ->
                    _uiState.value = _uiState.value.copy(
                        transactions = listOf(transaction) + _uiState.value.transactions,
                    )
                    loadSummary()
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
        }
    }

    fun deleteTransaction(id: Int) {
        viewModelScope.launch {
            deleteTransactionUseCase(id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        transactions = _uiState.value.transactions.filter { it.id != id },
                    )
                    loadSummary()
                }
                .onFailure { e -> _uiState.value = _uiState.value.copy(error = e.message) }
        }
    }

    fun loadCategories(type: String) {
        viewModelScope.launch {
            listCategories(type = type)
                .onSuccess { _categories.value = it }
        }
    }

    fun exportTransactions(format: String = "csv") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, exportMessage = null)
            exportTransactionsUseCase(format = format)
                .onSuccess { bytes ->
                    val dir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
                        ?: context.cacheDir
                    val file = File(dir, "transacciones.$format")
                    try {
                        file.writeBytes(bytes)
                        _uiState.value = _uiState.value.copy(
                            isExporting = false,
                            exportMessage = "Exportado: ${file.absolutePath}",
                        )
                    } catch (e: Exception) {
                        _uiState.value = _uiState.value.copy(
                            isExporting = false,
                            error = "No se pudo guardar el archivo: ${e.message}",
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        error = e.message ?: "Error al exportar",
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearExportMessage() {
        _uiState.value = _uiState.value.copy(exportMessage = null)
    }
}
