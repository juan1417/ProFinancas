package com.profinanzas.features.facturas.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profinanzas.core.domain.model.Invoice
import com.profinanzas.core.domain.usecase.invoice.CreateInvoiceUseCase
import com.profinanzas.core.domain.usecase.invoice.DeleteInvoiceUseCase
import com.profinanzas.core.domain.usecase.invoice.ListInvoicesUseCase
import com.profinanzas.core.domain.usecase.invoice.UpdateInvoiceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FacturasUiState(
    val invoices: List<Invoice> = emptyList(),
    val selectedFilter: String = "Todas",
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class FacturasViewModel @Inject constructor(
    private val listInvoicesUseCase: ListInvoicesUseCase,
    private val createInvoiceUseCase: CreateInvoiceUseCase,
    private val updateInvoiceUseCase: UpdateInvoiceUseCase,
    private val deleteInvoiceUseCase: DeleteInvoiceUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FacturasUiState())
    val uiState: StateFlow<FacturasUiState> = _uiState.asStateFlow()

    init {
        loadInvoices()
    }

    fun loadInvoices() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            listInvoicesUseCase()
                .onSuccess { invoices ->
                    _uiState.value = _uiState.value.copy(invoices = invoices, isLoading = false)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
                }
        }
    }

    fun setFilter(filter: String) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun getFilteredInvoices(): List<Invoice> {
        val state = _uiState.value
        return state.invoices.filter { invoice ->
            val matchesFilter = state.selectedFilter == "Todas" ||
                    (state.selectedFilter == "Pendientes" && invoice.status == "PENDING") ||
                    (state.selectedFilter == "Pagadas" && invoice.status == "PAID") ||
                    (state.selectedFilter == "Vencidas" && invoice.status == "OVERDUE")
            val matchesSearch = state.searchQuery.isEmpty() ||
                    invoice.provider.contains(state.searchQuery, ignoreCase = true) ||
                    invoice.invoiceNumber.contains(state.searchQuery, ignoreCase = true)
            matchesFilter && matchesSearch
        }
    }

    fun createInvoice(
        provider: String,
        amount: Double,
        status: String,
        category: String,
        dueDate: String,
        registeredDate: String,
        paidDate: String?,
        notes: String,
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            createInvoiceUseCase(provider, amount, status, category, dueDate, registeredDate, paidDate, notes)
                .onSuccess { loadInvoices() }
                .onFailure { e -> _uiState.value = _uiState.value.copy(error = e.message, isLoading = false) }
        }
    }

    fun updateInvoice(
        id: String,
        provider: String,
        amount: Double,
        status: String,
        category: String,
        dueDate: String,
        registeredDate: String,
        paidDate: String?,
        notes: String,
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            updateInvoiceUseCase(id, provider, amount, status, category, dueDate, registeredDate, paidDate, notes)
                .onSuccess { loadInvoices() }
                .onFailure { e -> _uiState.value = _uiState.value.copy(error = e.message, isLoading = false) }
        }
    }

    fun deleteInvoice(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            deleteInvoiceUseCase(id)
                .onSuccess { loadInvoices() }
                .onFailure { e -> _uiState.value = _uiState.value.copy(error = e.message, isLoading = false) }
        }
    }
}
