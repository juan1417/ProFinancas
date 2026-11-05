package com.profinanzas.features.analytics.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profinanzas.core.domain.model.Summary
import com.profinanzas.core.domain.model.Transaction
import com.profinanzas.core.domain.usecase.transaction.GetSummaryUseCase
import com.profinanzas.core.domain.usecase.transaction.GetTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class MonthlyData(
    val month: String,
    val year: Int,
    val income: Double,
    val expense: Double,
)

data class AnalyticsUiState(
    val summary: Summary? = null,
    val transactions: List<Transaction> = emptyList(),
    val monthlyData: List<MonthlyData> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val getSummary: GetSummaryUseCase,
    private val getTransactions: GetTransactionsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            getSummary("year")
                .onSuccess { summary ->
                    getTransactions()
                        .onSuccess { transactions ->
                            _uiState.value = _uiState.value.copy(
                                summary = summary,
                                transactions = transactions,
                                monthlyData = computeMonthlyTotals(transactions),
                                isLoading = false,
                            )
                        }
                        .onFailure { e ->
                            // Antes este fallo se perdía y el spinner no paraba.
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = e.message,
                            )
                        }
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

    private fun computeMonthlyTotals(transactions: List<Transaction>): List<MonthlyData> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val now = LocalDate.now()
        val months = (0L..5L).map { now.minusMonths(it) }.reversed()

        return months.map { date ->
            val monthTransactions = transactions.filter { tx ->
                try {
                    val txDate = LocalDate.parse(tx.transactionDate.substring(0, 10), formatter)
                    txDate.year == date.year && txDate.month == date.month
                } catch (_: Exception) { false }
            }
            MonthlyData(
                month = date.format(DateTimeFormatter.ofPattern("MMM")),
                year = date.year,
                income = monthTransactions.filter { it.isIncome }.sumOf { it.amount },
                expense = monthTransactions.filter { it.isExpense }.sumOf { it.amount },
            )
        }
    }
}
