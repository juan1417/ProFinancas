package com.profinanzas.features.transactions

import com.profinanzas.core.domain.model.Category
import com.profinanzas.core.domain.model.Transaction
import com.profinanzas.core.domain.usecase.category.ListCategoriesUseCase
import com.profinanzas.core.domain.usecase.transaction.CreateTransactionUseCase
import com.profinanzas.core.domain.usecase.transaction.DeleteTransactionUseCase
import com.profinanzas.core.domain.usecase.transaction.ExportTransactionsUseCase
import com.profinanzas.core.domain.usecase.transaction.GetTransactionsUseCase
import com.profinanzas.di.AppModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class TransactionsUiState(
    val transactions: List<Transaction> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedType: String? = null,
    val exportMessage: String? = null,
    val isExporting: Boolean = false,
)

class TransactionsViewModel(
    private val getTransactions: GetTransactionsUseCase = AppModule.getTransactionsUseCase,
    private val createTransactionUseCase: CreateTransactionUseCase = AppModule.createTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase = AppModule.deleteTransactionUseCase,
    private val listCategories: ListCategoriesUseCase = AppModule.listCategoriesUseCase,
    private val exportTransactionsUseCase: ExportTransactionsUseCase = AppModule.exportTransactionsUseCase,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _uiState = MutableStateFlow(TransactionsUiState())
    val uiState: StateFlow<TransactionsUiState> = _uiState.asStateFlow()

    init {
        loadTransactions()
        loadCategories("EXPENSE")
    }

    fun loadTransactions(type: String? = null) {
        _uiState.value = _uiState.value.copy(selectedType = type, isLoading = true)
        scope.launch {
            getTransactions(type = type)
                .onSuccess { transactions ->
                    _uiState.value = _uiState.value.copy(transactions = transactions, isLoading = false)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
                }
        }
    }

    fun loadCategories(type: String) {
        scope.launch {
            listCategories(type = type)
                .onSuccess { _uiState.value = _uiState.value.copy(categories = it) }
        }
    }

    fun createTransaction(
        categoryId: Int,
        type: String,
        amount: String,
        description: String,
        date: String? = null,
    ) {
        scope.launch {
            val zone = ZoneId.of("America/Bogota")
            val now = ZonedDateTime.now(zone)
            // `date` llega como YYYY-MM-DD. Hoy usa la hora actual (el backend
            // rechaza fechas futuras); otro día usa mediodía para no cruzar el
            // límite por zona horaria.
            val whenValue = date
                ?.takeIf { it.isNotBlank() }
                ?.let { raw -> runCatching { java.time.LocalDate.parse(raw) }.getOrNull() }
                ?.let { day ->
                    if (day == now.toLocalDate()) now
                    else day.atTime(12, 0).atZone(zone)
                }
                ?: now
            val transactionDate = whenValue
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            createTransactionUseCase(categoryId, type, amount, description, null, transactionDate)
                .onSuccess { loadTransactions(_uiState.value.selectedType) }
                .onFailure { e -> _uiState.value = _uiState.value.copy(error = e.message) }
        }
    }

    fun exportTransactions(format: String = "csv") {
        scope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, exportMessage = null)
            exportTransactionsUseCase(format = format)
                .onSuccess { bytes ->
                    runCatching {
                        val dir = java.io.File(System.getProperty("user.home"), "Downloads").apply { mkdirs() }
                        val file = java.io.File(dir, "transacciones.$format")
                        file.writeBytes(bytes)
                        file.absolutePath
                    }.onSuccess { path ->
                        _uiState.value = _uiState.value.copy(isExporting = false, exportMessage = "Exportado: $path")
                    }.onFailure { e ->
                        _uiState.value = _uiState.value.copy(isExporting = false, error = "No se pudo guardar: ${e.message}")
                    }
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isExporting = false, error = e.message ?: "Error al exportar")
                }
        }
    }

    fun clearExportMessage() {
        _uiState.value = _uiState.value.copy(exportMessage = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun deleteTransaction(id: Int) {
        scope.launch {
            deleteTransactionUseCase(id)
                .onSuccess { loadTransactions(_uiState.value.selectedType) }
                .onFailure { e -> _uiState.value = _uiState.value.copy(error = e.message) }
        }
    }
}
