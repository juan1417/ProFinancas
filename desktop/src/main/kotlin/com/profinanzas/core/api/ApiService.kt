package com.profinanzas.core.api

import com.profinanzas.core.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST(ApiConstants.LOGIN)
    suspend fun login(@Body body: Map<String, String>): Response<AuthResponse>

    @POST(ApiConstants.REGISTER)
    suspend fun register(@Body body: Map<String, String>): Response<AuthResponse>

    @POST(ApiConstants.LOGOUT)
    suspend fun logout(@Body body: Map<String, String>): Response<Unit>

    @GET(ApiConstants.PROFILE)
    suspend fun getProfile(): Response<UserDto>

    @PATCH(ApiConstants.PROFILE)
    suspend fun updateProfile(@Body body: Map<String, String>): Response<UserDto>

    @POST(ApiConstants.CHANGE_PASSWORD)
    suspend fun changePassword(@Body body: Map<String, String>): Response<Unit>

    @POST(ApiConstants.TOKEN_REFRESH)
    suspend fun refreshToken(@Body body: Map<String, String>): Response<TokenRefreshResponse>

    @GET(ApiConstants.CATEGORIES)
    suspend fun getCategories(
        @Query("type") type: String? = null,
        @Query("is_active") isActive: Boolean? = null,
        @Query("ordering") ordering: String = "name",
    ): Response<List<CategoryDto>>

    @POST(ApiConstants.CATEGORIES)
    suspend fun createCategory(@Body body: CategoryCreateRequest): Response<CategoryDto>

    @PATCH("${ApiConstants.CATEGORIES}{id}/")
    suspend fun updateCategory(
        @Path("id") id: Int,
        @Body body: CategoryCreateRequest,
    ): Response<CategoryDto>

    @DELETE("${ApiConstants.CATEGORIES}{id}/")
    suspend fun deleteCategory(@Path("id") id: Int): Response<Unit>

    @GET(ApiConstants.TRANSACTIONS)
    suspend fun getTransactions(
        @Query("type") type: String? = null,
        @Query("category") categoryId: Int? = null,
        @Query("search") search: String? = null,
        @Query("ordering") ordering: String = "-transaction_date",
    ): Response<List<TransactionDto>>

    @POST(ApiConstants.TRANSACTIONS)
    suspend fun createTransaction(@Body body: TransactionCreateRequest): Response<TransactionDto>

    @DELETE("${ApiConstants.TRANSACTIONS}{id}/")
    suspend fun deleteTransaction(@Path("id") id: Int): Response<Unit>

    @GET(ApiConstants.TRANSACTIONS_SUMMARY)
    suspend fun getSummary(
        @Query("period") period: String = "month",
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
    ): Response<SummaryDto>

    @GET(ApiConstants.TRANSACTIONS_BY_CATEGORY)
    suspend fun getTransactionsByCategory(
        @Query("type") type: String? = null,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
    ): Response<List<CategoryBreakdownDto>>

    @GET(ApiConstants.BUDGETS)
    suspend fun getBudgets(
        @Query("period") period: String? = null,
        @Query("is_active") isActive: Boolean? = null,
    ): Response<List<BudgetDto>>

    @POST(ApiConstants.BUDGETS)
    suspend fun createBudget(@Body body: BudgetCreateRequest): Response<BudgetDto>

    @GET("${ApiConstants.BUDGETS}{id}/")
    suspend fun getBudget(@Path("id") id: Int): Response<BudgetDto>

    @PUT("${ApiConstants.BUDGETS}{id}/")
    suspend fun updateBudget(
        @Path("id") id: Int,
        @Body body: BudgetCreateRequest,
    ): Response<BudgetDto>

    @DELETE("${ApiConstants.BUDGETS}{id}/")
    suspend fun deleteBudget(@Path("id") id: Int): Response<Unit>
}
