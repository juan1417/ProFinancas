package com.profinanzas.core.data.repository

import com.profinanzas.core.api.ApiService
import com.profinanzas.core.data.model.BudgetAlertDto
import com.profinanzas.core.domain.repository.BudgetAlertRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetAlertRepositoryImpl @Inject constructor(
    private val api: ApiService,
) : BudgetAlertRepository {
    override suspend fun list(): Result<List<BudgetAlertDto>> = runCatching {
        val response = api.getBudgetAlerts()
        if (response.isSuccessful) response.body() ?: emptyList()
        else throw Exception("Error al cargar alertas de presupuesto")
    }
}
