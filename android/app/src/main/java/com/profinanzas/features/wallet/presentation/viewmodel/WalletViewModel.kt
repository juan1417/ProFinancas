package com.profinanzas.features.wallet.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profinanzas.core.domain.model.Budget
import com.profinanzas.core.domain.usecase.budget.GetBudgetsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@Serializable
data class StoredCard(
    val id: String,
    val brand: String,
    val bank: String,
    val last4: String,
    val expiry: String,
    val holderName: String,
    val colorHex: String,
    val createdAt: Long,
)

data class WalletUiState(
    val cards: List<StoredCard> = emptyList(),
    val budgets: List<Budget> = emptyList(),
    val isLoading: Boolean = false,
)

@HiltViewModel
class WalletViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getBudgetsUseCase: GetBudgetsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    init {
        loadCards()
        loadBudgets()
    }

    private fun loadCards() {
        val prefs = context.getSharedPreferences("cards", Context.MODE_PRIVATE)
        val json = prefs.getString("cards", "[]") ?: "[]"
        val cards = Json.decodeFromString<List<StoredCard>>(json)
        _uiState.value = _uiState.value.copy(cards = cards)
    }

    private fun loadBudgets() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            getBudgetsUseCase()
                .onSuccess { budgets ->
                    _uiState.value = _uiState.value.copy(budgets = budgets, isLoading = false)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
        }
    }

    fun addCard(card: StoredCard) {
        val updated = _uiState.value.cards + card
        _uiState.value = _uiState.value.copy(cards = updated)
        persistCards(updated)
    }

    fun removeCard(cardId: String) {
        val updated = _uiState.value.cards.filter { it.id != cardId }
        _uiState.value = _uiState.value.copy(cards = updated)
        persistCards(updated)
    }

    private fun persistCards(cards: List<StoredCard>) {
        val prefs = context.getSharedPreferences("cards", Context.MODE_PRIVATE)
        prefs.edit().putString("cards", Json.encodeToString(cards)).apply()
    }
}
