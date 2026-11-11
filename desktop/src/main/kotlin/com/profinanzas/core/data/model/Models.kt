package com.profinanzas.core.data.model

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    val user: UserDto,
    val access: String,
    val refresh: String,
)

data class TokenRefreshResponse(
    val access: String,
    // El backend rota el refresh token (ROTATE_REFRESH_TOKENS=True), así que
    // cada refresh devuelve uno nuevo y hay que persistirlo.
    val refresh: String? = null,
)

data class UserDto(
    val id: String,
    val email: String,
    val username: String,
    @SerializedName("first_name") val firstName: String?,
    @SerializedName("last_name") val lastName: String?,
)

data class CategoryDto(
    val id: Int,
    val name: String,
    val type: String,
    val description: String?,
    val user: String,
    @SerializedName("is_active") val isActive: Boolean = true,
    @SerializedName("transactions_count") val transactionsCount: Int = 0,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
)

data class CategoryCreateRequest(
    val name: String,
    val type: String,
    val description: String? = null,
)

data class TransactionDto(
    val id: Int,
    val user: String,
    val category: Int,
    @SerializedName("category_name") val categoryName: String? = null,
    @SerializedName("category_type") val categoryType: String? = null,
    val type: String,
    val amount: String,
    val description: String,
    val notes: String? = null,
    @SerializedName("transaction_date") val transactionDate: String,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
)

data class TransactionCreateRequest(
    val category: Int,
    val type: String,
    val amount: String,
    val description: String,
    val notes: String? = null,
    @SerializedName("transaction_date") val transactionDate: String,
)

data class SummaryDto(
    val period: String,
    @SerializedName("total_income") val totalIncome: Double,
    @SerializedName("total_expenses") val totalExpenses: Double,
    val balance: Double,
    @SerializedName("transaction_count") val transactionCount: Int,
    @SerializedName("income_count") val incomeCount: Int,
    @SerializedName("expense_count") val expenseCount: Int,
    @SerializedName("by_category") val byCategory: List<CategoryBreakdownDto>,
)

data class CategoryBreakdownDto(
    @SerializedName("category__id") val categoryId: Int?,
    @SerializedName("category__name") val categoryName: String?,
    @SerializedName("category__type") val categoryType: String?,
    @SerializedName("total_amount") val totalAmount: Double,
    @SerializedName("transaction_count") val transactionCount: Int,
)

data class BudgetDto(
    val id: Int,
    val user: String,
    val category: Int?,
    @SerializedName("category_name") val categoryName: String? = null,
    val amount: String,
    val period: String,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String,
    @SerializedName("is_active") val isActive: Boolean = true,
    val spent: Double = 0.0,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
)

data class BudgetCreateRequest(
    val category: Int? = null,
    val amount: String,
    val period: String,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String,
)

data class CardDto(
    val id: Int,
    val user: String,
    val brand: String,
    val bank: String = "",
    val last4: String,
    val expiry: String,
    @SerializedName("holder_name") val holderName: String = "",
    @SerializedName("color_hex") val colorHex: String = "#1A4570",
    @SerializedName("is_active") val isActive: Boolean = true,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
)

data class CardCreateRequest(
    val brand: String,
    val bank: String = "",
    val last4: String,
    val expiry: String,
    @SerializedName("holder_name") val holderName: String = "",
    @SerializedName("color_hex") val colorHex: String = "#1A4570",
)

data class BudgetAlertDto(
    @SerializedName("budget_id") val budgetId: Int,
    @SerializedName("category_name") val categoryName: String? = null,
    val period: String,
    val amount: Double,
    val spent: Double,
    val remaining: Double,
    val percentage: Double,
    val status: String,
)

data class InvoiceDto(
    val id: String,
    @SerializedName("invoice_number") val invoiceNumber: String,
    val user: String,
    val provider: String,
    val amount: String,
    val status: String,
    val category: String,
    @SerializedName("due_date") val dueDate: String,
    @SerializedName("registered_date") val registeredDate: String,
    @SerializedName("paid_date") val paidDate: String? = null,
    val notes: String,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
)

data class InvoiceCreateRequest(
    val provider: String,
    val amount: String,
    val status: String,
    val category: String,
    @SerializedName("due_date") val dueDate: String,
    @SerializedName("registered_date") val registeredDate: String,
    @SerializedName("paid_date") val paidDate: String? = null,
    val notes: String = "",
)
