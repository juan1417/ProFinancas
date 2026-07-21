package com.profinanzas.features.wallet.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
)

@HiltViewModel
class WalletViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    fun load(context: Context) {
        val prefs = context.getSharedPreferences("cards", Context.MODE_PRIVATE)
        val json = prefs.getString("cards", "[]") ?: "[]"
        val cards = Json.decodeFromString<List<StoredCard>>(json)
        _uiState.value = WalletUiState(cards = cards)
    }

    fun addCard(context: Context, card: StoredCard) {
        val updated = _uiState.value.cards + card
        _uiState.value = WalletUiState(cards = updated)
        persist(context, updated)
    }

    fun removeCard(context: Context, cardId: String) {
        val updated = _uiState.value.cards.filter { it.id != cardId }
        _uiState.value = WalletUiState(cards = updated)
        persist(context, updated)
    }

    private fun persist(context: Context, cards: List<StoredCard>) {
        val prefs = context.getSharedPreferences("cards", Context.MODE_PRIVATE)
        prefs.edit().putString("cards", Json.encodeToString(cards)).apply()
    }
}
