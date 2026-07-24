package com.profinanzas.core.domain.usecase.invoice

import com.profinanzas.core.domain.repository.InvoiceRepository
import javax.inject.Inject

class ListInvoicesUseCase @Inject constructor(
    private val repository: InvoiceRepository,
) {
    suspend operator fun invoke() = repository.list()
}

class GetInvoiceUseCase @Inject constructor(
    private val repository: InvoiceRepository,
) {
    suspend operator fun invoke(id: String) = repository.get(id)
}

class CreateInvoiceUseCase @Inject constructor(
    private val repository: InvoiceRepository,
) {
    suspend operator fun invoke(
        provider: String,
        amount: String,
        status: String,
        category: String,
        dueDate: String,
        registeredDate: String,
        paidDate: String?,
        notes: String,
    ) = repository.create(provider, amount, status, category, dueDate, registeredDate, paidDate, notes)
}

class UpdateInvoiceUseCase @Inject constructor(
    private val repository: InvoiceRepository,
) {
    suspend operator fun invoke(
        id: String,
        provider: String,
        amount: String,
        status: String,
        category: String,
        dueDate: String,
        registeredDate: String,
        paidDate: String?,
        notes: String,
    ) = repository.update(id, provider, amount, status, category, dueDate, registeredDate, paidDate, notes)
}

class DeleteInvoiceUseCase @Inject constructor(
    private val repository: InvoiceRepository,
) {
    suspend operator fun invoke(id: String) = repository.delete(id)
}
