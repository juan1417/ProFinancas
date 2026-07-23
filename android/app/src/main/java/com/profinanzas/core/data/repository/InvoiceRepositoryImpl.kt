package com.profinanzas.core.data.repository

import com.profinanzas.core.api.ApiService
import com.profinanzas.core.data.model.InvoiceCreateRequest
import com.profinanzas.core.domain.model.Invoice
import com.profinanzas.core.domain.repository.InvoiceRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvoiceRepositoryImpl @Inject constructor(
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
        createdAt = createdAt,
        updatedAt = updatedAt,
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

    override suspend fun create(
        provider: String,
        amount: Double,
        status: String,
        category: String,
        dueDate: String,
        registeredDate: String,
        paidDate: String?,
        notes: String,
    ): Result<Invoice> = runCatching {
        val response = api.createInvoice(
            InvoiceCreateRequest(provider, amount, status, category, dueDate, registeredDate, paidDate, notes)
        )
        if (response.isSuccessful) response.body()!!.toDomain()
        else throw Exception("Error al crear factura")
    }

    override suspend fun update(
        id: String,
        provider: String,
        amount: Double,
        status: String,
        category: String,
        dueDate: String,
        registeredDate: String,
        paidDate: String?,
        notes: String,
    ): Result<Invoice> = runCatching {
        val response = api.updateInvoice(
            id,
            InvoiceCreateRequest(provider, amount, status, category, dueDate, registeredDate, paidDate, notes)
        )
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
