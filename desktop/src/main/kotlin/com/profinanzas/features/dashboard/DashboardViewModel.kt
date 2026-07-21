package com.profinanzas.features.dashboard

import com.profinanzas.core.domain.model.Summary
import com.profinanzas.core.domain.usecase.transaction.GetSummaryUseCase
import com.profinanzas.core.domain.usecase.budget.GetBudgetsUseCase
import com.profinanzas.core.domain.model.Budget
import com.profinanzas.di.AppModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val summary: Summary? = null,
    val budgets: List<Budget> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class DashboardViewModel(
    private val getSummary: GetSummaryUseCase = AppModule.getSummaryUseCase,
    private val getBudgets: GetBudgetsUseCase = AppModule.getBudgetsUseCase,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadSummary()
        loadBudgets()
    }

    fun loadSummary(period: String = "month") {
        scope.launch {
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

    private fun loadBudgets() {
        scope.launch {
            getBudgets()
                .onSuccess { budgets ->
                    _uiState.value = _uiState.value.copy(budgets = budgets)
                }
        }
    }
}
