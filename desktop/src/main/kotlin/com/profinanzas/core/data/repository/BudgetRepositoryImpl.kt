package com.profinanzas.core.data.repository

import com.profinanzas.core.api.ApiService
import com.profinanzas.core.data.model.BudgetCreateRequest
import com.profinanzas.core.domain.model.Budget
import com.profinanzas.core.domain.repository.BudgetRepository

class BudgetRepositoryImpl(
    private val api: ApiService,
) : BudgetRepository {

    private fun com.profinanzas.core.data.model.BudgetDto.toDomain() = Budget(
        id = id,
        category = category,
        categoryName = categoryName,
        amount = amount.toDoubleOrNull() ?: 0.0,
        period = period,
        startDate = startDate,
        endDate = endDate,
        isActive = isActive,
        spent = spent,
    )

    override suspend fun list(period: String?, isActive: Boolean?): Result<List<Budget>> = runCatching {
        val response = api.getBudgets(period = period, isActive = isActive)
        if (response.isSuccessful) response.body()!!.map { it.toDomain() }
        else throw Exception("Error al cargar presupuestos")
    }

    override suspend fun get(id: Int): Result<Budget> = runCatching {
        val response = api.getBudget(id)
        if (response.isSuccessful) response.body()!!.toDomain()
        else throw Exception("Error al cargar presupuesto")
    }

    override suspend fun create(
        categoryId: Int?,
        amount: String,
        period: String,
        startDate: String,
        endDate: String,
    ): Result<Budget> = runCatching {
        val response = api.createBudget(
            BudgetCreateRequest(categoryId, amount, period, startDate, endDate)
        )
        if (response.isSuccessful) response.body()!!.toDomain()
        else throw Exception("Error al crear presupuesto")
    }

    override suspend fun update(
        id: Int,
        categoryId: Int?,
        amount: String,
        period: String,
        startDate: String,
        endDate: String,
    ): Result<Budget> = runCatching {
        val response = api.updateBudget(
            id, BudgetCreateRequest(categoryId, amount, period, startDate, endDate)
        )
        if (response.isSuccessful) response.body()!!.toDomain()
        else throw Exception("Error al actualizar presupuesto")
    }

    override suspend fun delete(id: Int): Result<Unit> = runCatching {
        val response = api.deleteBudget(id)
        if (!response.isSuccessful && response.code() != 204) {
            throw Exception("Error al eliminar presupuesto")
        }
    }
}
