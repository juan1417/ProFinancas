package com.profinanzas.core.data.repository

import com.profinanzas.core.api.ApiService
import com.profinanzas.core.data.model.InvoiceCreateRequest
import com.profinanzas.core.domain.model.Invoice
import com.profinanzas.core.domain.repository.InvoiceRepository

class InvoiceRepositoryImpl(
    private val api: ApiService,
) : InvoiceRepository {

    private fun com.profinanzas.core.data.model.InvoiceDto.toDomain() = Invoice(
        id = id,
        invoiceNumber = invoiceNumber,
        provider = provider,
        amount = amount,
        status = status,
        category = category,
        dueDate = dueDate,
        registeredDate = registeredDate,
        paidDate = paidDate,
        notes = notes,
    )

    override suspend fun list(): Result<List<Invoice>> = runCatching {
        val response = api.getInvoices()
        if (response.isSuccessful) response.body()!!.map { it.toDomain() }
        else throw Exception("Error al cargar facturas")
    }

    override suspend fun get(id: String): Result<Invoice> = runCatching {
        val response = api.getInvoice(id)
        if (response.isSuccessful) response.body()!!.toDomain()
        else throw Exception("Error al cargar factura")
    }

    override suspend fun create(data: InvoiceCreateRequest): Result<Invoice> = runCatching {
        val response = api.createInvoice(data)
        if (response.isSuccessful) response.body()!!.toDomain()
        else throw Exception("Error al crear factura")
    }

    override suspend fun update(id: String, data: InvoiceCreateRequest): Result<Invoice> = runCatching {
        val response = api.updateInvoice(id, data)
        if (response.isSuccessful) response.body()!!.toDomain()
        else throw Exception("Error al actualizar factura")
    }

    override suspend fun delete(id: String): Result<Unit> = runCatching {
        val response = api.deleteInvoice(id)
        if (!response.isSuccessful && response.code() != 204) {
            throw Exception("Error al eliminar factura")
        }
    }
}
