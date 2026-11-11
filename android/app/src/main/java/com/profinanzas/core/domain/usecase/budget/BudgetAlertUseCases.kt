package com.profinanzas.core.domain.usecase.budget

import com.profinanzas.core.data.model.BudgetAlertDto
import com.profinanzas.core.domain.repository.BudgetAlertRepository
import javax.inject.Inject

class GetBudgetAlertsUseCase @Inject constructor(
    private val repository: BudgetAlertRepository,
) {
    suspend operator fun invoke() = repository.list()
}
