package com.profinanzas.core.domain.repository

import com.profinanzas.core.data.model.BudgetAlertDto

interface BudgetAlertRepository {
    suspend fun list(): Result<List<BudgetAlertDto>>
}
