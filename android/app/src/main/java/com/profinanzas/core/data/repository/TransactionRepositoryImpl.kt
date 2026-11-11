package com.profinanzas.core.data.repository

import com.profinanzas.core.api.ApiService
import com.profinanzas.core.data.model.TransactionCreateRequest
import com.profinanzas.core.domain.model.*
import com.profinanzas.core.domain.repository.TransactionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val api: ApiService,
) : TransactionRepository {

    private fun com.profinanzas.core.data.model.TransactionDto.toDomain() = Transaction(
        id = id,
        type = type,
        amount = amount.toDoubleOrNull() ?: 0.0,
        description = description,
        categoryId = category,
        categoryName = categoryName,
        transactionDate = transactionDate,
        notes = notes,
    )

    private fun com.profinanzas.core.data.model.SummaryDto.toDomain() = Summary(
        period = period,
        totalIncome = totalIncome,
        totalExpenses = totalExpenses,
        balance = balance,
        transactionCount = transactionCount,
        byCategory = byCategory.map { it.toDomain() },
    )

    private fun com.profinanzas.core.data.model.CategoryBreakdownDto.toDomain() = CategoryBreakdown(
        categoryId = categoryId,
        categoryName = categoryName,
        categoryType = categoryType,
        totalAmount = totalAmount,
        transactionCount = transactionCount,
    )

    override suspend fun list(
        type: String?,
        categoryId: Int?,
        search: String?,
    ): Result<List<Transaction>> = runCatching {
        val response = api.getTransactions(type = type, categoryId = categoryId, search = search)
        if (response.isSuccessful) response.body()!!.map { it.toDomain() }
        else throw Exception("Error al cargar transacciones")
    }

    override suspend fun create(
        categoryId: Int,
        type: String,
        amount: String,
        description: String,
        notes: String?,
        transactionDate: String,
    ): Result<Transaction> = runCatching {
        val response = api.createTransaction(
            TransactionCreateRequest(categoryId, type, amount, description, notes, transactionDate)
        )
        if (response.isSuccessful) response.body()!!.toDomain()
        else throw Exception("Error al crear transacción")
    }

    override suspend fun delete(id: Int): Result<Unit> = runCatching {
        val response = api.deleteTransaction(id)
        if (!response.isSuccessful && response.code() != 204) {
            throw Exception("Error al eliminar transacción")
        }
    }

    override suspend fun getSummary(period: String): Result<Summary> = runCatching {
        val response = api.getSummary(period = period)
        if (response.isSuccessful) response.body()!!.toDomain()
        else throw Exception("Error al cargar resumen")
    }

    override suspend fun export(
        format: String,
        startDate: String?,
        endDate: String?,
        type: String?,
    ): Result<ByteArray> = runCatching {
        val response = api.exportTransactions(
            format = format,
            startDate = startDate,
            endDate = endDate,
            type = type,
        )
        if (response.isSuccessful) response.body()!!.bytes()
        else throw Exception("Error al exportar transacciones")
    }
}
