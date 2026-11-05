package com.profinanzas.features.dashboard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profinanzas.core.domain.model.Budget
import com.profinanzas.core.domain.model.Summary
import com.profinanzas.core.domain.model.Transaction
import com.profinanzas.core.domain.usecase.budget.GetBudgetsUseCase
import com.profinanzas.core.domain.usecase.transaction.GetSummaryUseCase
import com.profinanzas.core.domain.usecase.transaction.GetTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val summary: Summary? = null,
    val recentTransactions: List<Transaction> = emptyList(),
    val budgets: List<Budget> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getSummary: GetSummaryUseCase,
    private val getTransactions: GetTransactionsUseCase,
    private val getBudgets: GetBudgetsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadSummary()
        loadRecentTransactions()
        loadBudgets()
    }

    fun loadSummary(period: String = "month") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            getSummary(period)
                .onSuccess { summary ->
                    _uiState.value = _uiState.value.copy(
                        summary = summary,
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

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun loadRecentTransactions() {        viewModelScope.launch {
            getTransactions()
                .onSuccess { transactions ->
                    _uiState.value = _uiState.value.copy(
                        recentTransactions = transactions.take(5),
                    )
                }
        }
    }

    private fun loadBudgets() {
        viewModelScope.launch {
            getBudgets()
                .onSuccess { budgets ->
                    _uiState.value = _uiState.value.copy(budgets = budgets)
                }
        }
    }
}
