package com.profinanzas.core.domain.usecase.budget

import com.profinanzas.core.domain.repository.BudgetRepository
import javax.inject.Inject

class GetBudgetsUseCase @Inject constructor(
    private val repository: BudgetRepository,
) {
    suspend operator fun invoke(period: String? = null, isActive: Boolean? = null) =
        repository.list(period, isActive)
}

class CreateBudgetUseCase @Inject constructor(
    private val repository: BudgetRepository,
) {
    suspend operator fun invoke(
        categoryId: Int?,
        amount: String,
        period: String,
        startDate: String,
        endDate: String,
    ) = repository.create(categoryId, amount, period, startDate, endDate)
}

class DeleteBudgetUseCase @Inject constructor(
    private val repository: BudgetRepository,
) {
    suspend operator fun invoke(id: Int) = repository.delete(id)
}
