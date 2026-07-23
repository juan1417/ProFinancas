package com.profinanzas.core.domain.usecase.invoice

import com.profinanzas.core.data.model.InvoiceCreateRequest
import com.profinanzas.core.domain.repository.InvoiceRepository

class ListInvoicesUseCase(private val repository: InvoiceRepository) {
    suspend operator fun invoke() = repository.list()
}

class GetInvoiceUseCase(private val repository: InvoiceRepository) {
    suspend operator fun invoke(id: String) = repository.get(id)
}

class CreateInvoiceUseCase(private val repository: InvoiceRepository) {
    suspend operator fun invoke(data: InvoiceCreateRequest) = repository.create(data)
}

class UpdateInvoiceUseCase(private val repository: InvoiceRepository) {
    suspend operator fun invoke(id: String, data: InvoiceCreateRequest) = repository.update(id, data)
}

class DeleteInvoiceUseCase(private val repository: InvoiceRepository) {
    suspend operator fun invoke(id: String) = repository.delete(id)
}
