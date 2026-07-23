package com.profinanzas.features.transactions

import com.profinanzas.core.domain.model.Category
import com.profinanzas.core.domain.model.Transaction
import com.profinanzas.core.domain.usecase.category.ListCategoriesUseCase
import com.profinanzas.core.domain.usecase.transaction.CreateTransactionUseCase
import com.profinanzas.core.domain.usecase.transaction.DeleteTransactionUseCase
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
)

class TransactionsViewModel(
    private val getTransactions: GetTransactionsUseCase = AppModule.getTransactionsUseCase,
    private val createTransactionUseCase: CreateTransactionUseCase = AppModule.createTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase = AppModule.deleteTransactionUseCase,
    private val listCategories: ListCategoriesUseCase = AppModule.listCategoriesUseCase,
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

    fun createTransaction(categoryId: Int, type: String, amount: String, description: String) {
        scope.launch {
            val now = ZonedDateTime.now(ZoneId.of("America/Bogota"))
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            createTransactionUseCase(categoryId, type, amount, description, null, now)
                .onSuccess { loadTransactions(_uiState.value.selectedType) }
                .onFailure { e -> _uiState.value = _uiState.value.copy(error = e.message) }
        }
    }

    fun deleteTransaction(id: Int) {
        scope.launch {
            deleteTransactionUseCase(id)
                .onSuccess { loadTransactions(_uiState.value.selectedType) }
                .onFailure { e -> _uiState.value = _uiState.value.copy(error = e.message) }
        }
    }
}
