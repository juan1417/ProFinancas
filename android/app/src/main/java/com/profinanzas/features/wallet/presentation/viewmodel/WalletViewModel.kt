package com.profinanzas.features.wallet.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profinanzas.core.domain.model.Budget
import com.profinanzas.core.domain.model.StoredCard
import com.profinanzas.core.domain.repository.CardRepository
import com.profinanzas.core.domain.usecase.budget.GetBudgetsUseCase
import com.profinanzas.core.domain.usecase.card.CreateCardUseCase
import com.profinanzas.core.domain.usecase.card.DeleteCardUseCase
import com.profinanzas.core.domain.usecase.card.GetCardsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WalletUiState(
    val cards: List<StoredCard> = emptyList(),
    val budgets: List<Budget> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class WalletViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getCardsUseCase: GetCardsUseCase,
    private val createCardUseCase: CreateCardUseCase,
    private val deleteCardUseCase: DeleteCardUseCase,
    private val getBudgetsUseCase: GetBudgetsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    init {
        loadCards()
        loadBudgets()
    }

    private fun loadCards() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            getCardsUseCase()
                .onSuccess { cards ->
                    _uiState.value = _uiState.value.copy(cards = cards, isLoading = false)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
        }
    }

    private fun loadBudgets() {
        viewModelScope.launch {
            getBudgetsUseCase()
                .onSuccess { budgets ->
                    _uiState.value = _uiState.value.copy(budgets = budgets)
                }
                .onFailure { /* silencioso: presupuestos opcionales */ }
        }
    }

    fun addCard(card: StoredCard) {
        viewModelScope.launch {
            createCardUseCase(card)
                .onSuccess { created ->
                    _uiState.value = _uiState.value.copy(
                        cards = _uiState.value.cards + created,
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
        }
    }

    fun removeCard(cardId: String) {
        viewModelScope.launch {
            deleteCardUseCase(cardId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        cards = _uiState.value.cards.filter { it.id != cardId },
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun reload() {
        loadCards()
        loadBudgets()
    }
}
