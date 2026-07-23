package com.profinanzas.features.facturas

import com.profinanzas.core.data.model.InvoiceCreateRequest
import com.profinanzas.core.domain.model.Invoice
import com.profinanzas.di.AppModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FacturasUiState(
    val invoices: List<Invoice> = emptyList(),
    val selectedFilter: String = "Todas",
    val searchQuery: String = "",
    val selectedInvoice: Invoice? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

class FacturasViewModel {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _uiState = MutableStateFlow(FacturasUiState())
    val uiState: StateFlow<FacturasUiState> = _uiState.asStateFlow()

    init {
        loadInvoices()
    }

    fun loadInvoices() {
        scope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            AppModule.listInvoicesUseCase()
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

    fun selectInvoice(invoice: Invoice?) {
        _uiState.value = _uiState.value.copy(selectedInvoice = invoice)
    }

    fun createInvoice(request: InvoiceCreateRequest) {
        scope.launch {
            AppModule.createInvoiceUseCase(request)
                .onSuccess { loadInvoices() }
                .onFailure { e -> _uiState.value = _uiState.value.copy(error = e.message) }
        }
    }

    fun updateInvoice(id: String, request: InvoiceCreateRequest) {
        scope.launch {
            AppModule.updateInvoiceUseCase(id, request)
                .onSuccess { loadInvoices() }
                .onFailure { e -> _uiState.value = _uiState.value.copy(error = e.message) }
        }
    }

    fun deleteInvoice(id: String) {
        scope.launch {
            AppModule.deleteInvoiceUseCase(id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(selectedInvoice = null)
                    loadInvoices()
                }
                .onFailure { e -> _uiState.value = _uiState.value.copy(error = e.message) }
        }
    }

    fun getFilteredInvoices(): List<Invoice> {
        val state = _uiState.value
        return state.invoices.filter { invoice ->
            val matchesFilter = state.selectedFilter == "Todas" ||
                    (state.selectedFilter == "Pendientes" && invoice.isPending) ||
                    (state.selectedFilter == "Pagadas" && invoice.isPaid) ||
                    (state.selectedFilter == "Vencidas" && invoice.isOverdue)
            val matchesSearch = state.searchQuery.isEmpty() ||
                    invoice.provider.contains(state.searchQuery, ignoreCase = true) ||
                    invoice.invoiceNumber.contains(state.searchQuery, ignoreCase = true)
            matchesFilter && matchesSearch
        }
    }
}
