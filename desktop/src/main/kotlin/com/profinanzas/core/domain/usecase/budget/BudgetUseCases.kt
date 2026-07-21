package com.profinanzas.core.domain.usecase.budget

import com.profinanzas.core.domain.repository.BudgetRepository

class GetBudgetsUseCase(private val repository: BudgetRepository) {
    suspend operator fun invoke(period: String? = null, isActive: Boolean? = null) =
        repository.list(period, isActive)
}

class CreateBudgetUseCase(private val repository: BudgetRepository) {
    suspend operator fun invoke(
        categoryId: Int?,
        amount: String,
        period: String,
        startDate: String,
        endDate: String,
    ) = repository.create(categoryId, amount, period, startDate, endDate)
}

class DeleteBudgetUseCase(private val repository: BudgetRepository) {
    suspend operator fun invoke(id: Int) = repository.delete(id)
}
