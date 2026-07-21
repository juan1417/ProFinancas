package com.profinanzas.features.facturas

import com.profinanzas.di.AppModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Invoice(
    val id: String,
    val provider: String,
    val amount: Double,
    val status: String,
    val dueDate: String = "",
    val category: String = "",
    val notes: String = "",
    val registeredDate: String = "",
    val paidDate: String? = null,
)

data class FacturasUiState(
    val invoices: List<Invoice> = listOf(
        Invoice("FAC-001", "TEPCO Electricidad", 6420.0, "paid", "15 Jul 2026", "Servicios", registeredDate = "01 Jul 2026", paidDate = "14 Jul 2026"),
        Invoice("FAC-002", "NTT DoCoMo", 4500.0, "pending", "20 Jul 2026", "Servicios", registeredDate = "05 Jul 2026"),
        Invoice("FAC-003", "Tokyo Gas", 3280.0, "paid", "10 Jul 2026", "Servicios", registeredDate = "01 Jul 2026", paidDate = "09 Jul 2026"),
        Invoice("FAC-004", "Seguro Residencial", 8900.0, "overdue", "05 Jul 2026", "Salud", notes = "Se presentó reclamo el 12/07.", registeredDate = "01 Jun 2026"),
        Invoice("FAC-005", "Internet Yozumi", 5200.0, "pending", "25 Jul 2026", "Servicios", registeredDate = "10 Jul 2026"),
        Invoice("FAC-006", "Agua Municipal", 2100.0, "paid", "08 Jul 2026", "Servicios", registeredDate = "01 Jul 2026", paidDate = "07 Jul 2026"),
    ),
    val selectedFilter: String = "Todas",
    val searchQuery: String = "",
    val selectedInvoice: Invoice? = null,
)

class FacturasViewModel {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _uiState = MutableStateFlow(FacturasUiState())
    val uiState: StateFlow<FacturasUiState> = _uiState.asStateFlow()

    fun setFilter(filter: String) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun selectInvoice(invoice: Invoice?) {
        _uiState.value = _uiState.value.copy(selectedInvoice = invoice)
    }

    fun getFilteredInvoices(): List<Invoice> {
        val state = _uiState.value
        return state.invoices.filter { invoice ->
            val matchesFilter = state.selectedFilter == "Todas" ||
                    (state.selectedFilter == "Pendientes" && invoice.status == "pending") ||
                    (state.selectedFilter == "Pagadas" && invoice.status == "paid") ||
                    (state.selectedFilter == "Vencidas" && invoice.status == "overdue")
            val matchesSearch = state.searchQuery.isEmpty() ||
                    invoice.provider.contains(state.searchQuery, ignoreCase = true) ||
                    invoice.id.contains(state.searchQuery, ignoreCase = true)
            matchesFilter && matchesSearch
        }
    }
}
