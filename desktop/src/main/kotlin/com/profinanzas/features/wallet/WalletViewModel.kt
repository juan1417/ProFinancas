package com.profinanzas.features.wallet

import com.profinanzas.core.domain.model.Budget
import com.profinanzas.core.domain.usecase.budget.GetBudgetsUseCase
import com.profinanzas.di.AppModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WalletCard(
    val id: String,
    val name: String,
    val lastFour: String,
    val cardType: String,
    val gradientStart: Long,
    val gradientEnd: Long,
)

data class WalletUiState(
    val cards: List<WalletCard> = emptyList(),
    val budgets: List<Budget> = emptyList(),
    val selectedCardId: String? = null,
)

class WalletViewModel(
    private val getBudgets: GetBudgetsUseCase = AppModule.getBudgetsUseCase,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    init {
        loadBudgets()
    }

    private fun loadBudgets() {
        scope.launch {
            getBudgets()
                .onSuccess { budgets ->
                    _uiState.value = _uiState.value.copy(budgets = budgets)
                }
        }
    }

    fun selectCard(id: String?) {
        _uiState.value = _uiState.value.copy(selectedCardId = id)
    }
}
