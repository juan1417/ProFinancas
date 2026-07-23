package com.profinanzas.features.transactions.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profinanzas.core.domain.model.Category
import com.profinanzas.core.domain.model.Summary
import com.profinanzas.core.domain.model.Transaction
import com.profinanzas.core.domain.usecase.category.ListCategoriesUseCase
import com.profinanzas.core.domain.usecase.transaction.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionUiState(
    val transactions: List<Transaction> = emptyList(),
    val summary: Summary? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val getTransactions: GetTransactionsUseCase,
    private val createTransaction: CreateTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val getSummary: GetSummaryUseCase,
    private val listCategories: ListCategoriesUseCase,
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
    ) {
        viewModelScope.launch {
            val now = java.time.ZonedDateTime.now(java.time.ZoneId.of("America/Bogota"))
                .format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            createTransaction(categoryId, type, amount, description, notes, now)
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

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
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
}
