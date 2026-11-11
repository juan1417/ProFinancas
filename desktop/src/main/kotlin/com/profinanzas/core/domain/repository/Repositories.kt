package com.profinanzas.core.domain.repository

import com.profinanzas.core.data.model.InvoiceCreateRequest
import com.profinanzas.core.domain.model.*

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthResult>
    suspend fun register(email: String, username: String, password: String, passwordConfirm: String): Result<AuthResult>
    suspend fun logout(refreshToken: String): Result<Unit>
    suspend fun getProfile(): Result<User>
    suspend fun updateProfile(firstName: String?, lastName: String?, username: String?): Result<User>
    suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit>
    suspend fun refreshToken(refreshToken: String): Result<String>
}

data class AuthResult(
    val user: User,
    val accessToken: String,
    val refreshToken: String,
)

interface CategoryRepository {
    suspend fun list(type: String? = null, isActive: Boolean? = null): Result<List<Category>>
    suspend fun create(name: String, type: String, description: String?): Result<Category>
    suspend fun update(id: Int, name: String, type: String, description: String?): Result<Category>
    suspend fun delete(id: Int): Result<Unit>
}

interface TransactionRepository {
    suspend fun list(
        type: String? = null,
        categoryId: Int? = null,
        search: String? = null,
    ): Result<List<Transaction>>

    suspend fun create(
        categoryId: Int,
        type: String,
        amount: String,
        description: String,
        notes: String?,
        transactionDate: String,
    ): Result<Transaction>

    suspend fun delete(id: Int): Result<Unit>
    suspend fun getSummary(period: String = "month"): Result<Summary>

    suspend fun export(
        format: String = "csv",
        startDate: String? = null,
        endDate: String? = null,
        type: String? = null,
    ): Result<ByteArray>
}

interface BudgetRepository {
    suspend fun list(period: String? = null, isActive: Boolean? = null): Result<List<Budget>>
    suspend fun get(id: Int): Result<Budget>
    suspend fun create(
        categoryId: Int?,
        amount: String,
        period: String,
        startDate: String,
        endDate: String,
    ): Result<Budget>
    suspend fun update(
        id: Int,
        categoryId: Int?,
        amount: String,
        period: String,
        startDate: String,
        endDate: String,
    ): Result<Budget>
    suspend fun delete(id: Int): Result<Unit>
}

interface BudgetAlertRepository {
    suspend fun list(): Result<List<com.profinanzas.core.data.model.BudgetAlertDto>>
}

interface InvoiceRepository {
    suspend fun list(): Result<List<Invoice>>
    suspend fun get(id: String): Result<Invoice>
    suspend fun create(data: InvoiceCreateRequest): Result<Invoice>
    suspend fun update(id: String, data: InvoiceCreateRequest): Result<Invoice>
    suspend fun delete(id: String): Result<Unit>
}
