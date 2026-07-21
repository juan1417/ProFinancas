package com.profinanzas.features.wallet

import com.profinanzas.di.AppModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class WalletCard(
    val id: String,
    val name: String,
    val lastFour: String,
    val cardType: String,
    val gradientStart: Long,
    val gradientEnd: Long,
)

data class WalletUiState(
    val cards: List<WalletCard> = listOf(
        WalletCard("1", "Visa Platinum", "4521", "credit", 0xFF22C55E, 0xFF16A34A),
        WalletCard("2", "Mastercard Gold", "8903", "debit", 0xFF2563EB, 0xFF1D4ED8),
    ),
    val selectedCardId: String? = null,
)

class WalletViewModel {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    fun selectCard(id: String?) {
        _uiState.value = _uiState.value.copy(selectedCardId = id)
    }
}
