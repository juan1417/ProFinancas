package com.profinanzas.core.domain.model

data class User(
    val id: String,
    val email: String,
    val username: String,
    val firstName: String?,
    val lastName: String?,
) {
    val fullName: String
        get() = listOfNotNull(firstName, lastName).joinToString(" ").ifBlank { username }
}

data class Category(
    val id: Int,
    val name: String,
    val type: String,
    val description: String?,
    val isActive: Boolean = true,
    val transactionsCount: Int = 0,
) {
    val isIncome get() = type == "INCOME"
    val isExpense get() = type == "EXPENSE"
}

data class Transaction(
    val id: Int,
    val type: String,
    val amount: Double,
    val description: String,
    val categoryId: Int,
    val categoryName: String?,
    val transactionDate: String,
    val notes: String? = null,
) {
    val isIncome get() = type == "INCOME"
    val isExpense get() = type == "EXPENSE"
    val signedAmount: Double get() = if (isIncome) amount else -amount
}

data class Summary(
    val period: String,
    val totalIncome: Double,
    val totalExpenses: Double,
    val balance: Double,
    val transactionCount: Int,
    val byCategory: List<CategoryBreakdown>,
)

data class CategoryBreakdown(
    val categoryId: Int?,
    val categoryName: String?,
    val categoryType: String?,
    val totalAmount: Double,
    val transactionCount: Int,
)

data class Budget(
    val id: Int,
    val category: Int?,
    val categoryName: String?,
    val amount: Double,
    val period: String,
    val startDate: String,
    val endDate: String,
    val isActive: Boolean = true,
    val spent: Double = 0.0,
) {
    val remaining: Double get() = amount - spent
    val percentage: Double get() = if (amount > 0) (spent / amount * 100) else 0.0
}
