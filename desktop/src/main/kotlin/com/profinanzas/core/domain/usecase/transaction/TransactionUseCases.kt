package com.profinanzas.core.domain.usecase.transaction

import com.profinanzas.core.domain.repository.TransactionRepository

class GetTransactionsUseCase(private val repository: TransactionRepository) {
    suspend operator fun invoke(
        type: String? = null,
        categoryId: Int? = null,
        search: String? = null,
    ) = repository.list(type, categoryId, search)
}

class CreateTransactionUseCase(private val repository: TransactionRepository) {
    suspend operator fun invoke(
        categoryId: Int,
        type: String,
        amount: String,
        description: String,
        notes: String?,
        transactionDate: String,
    ) = repository.create(categoryId, type, amount, description, notes, transactionDate)
}

class GetSummaryUseCase(private val repository: TransactionRepository) {
    suspend operator fun invoke(period: String = "month") = repository.getSummary(period)
}
