package com.profinanzas.features.dashboard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profinanzas.core.domain.model.Summary
import com.profinanzas.core.domain.usecase.transaction.GetSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val summary: Summary? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getSummary: GetSummaryUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadSummary()
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
}
