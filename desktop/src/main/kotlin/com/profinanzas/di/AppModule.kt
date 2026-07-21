package com.profinanzas.di

import com.profinanzas.core.api.ApiService
import com.profinanzas.core.api.AuthInterceptor
import com.profinanzas.core.data.repository.*
import com.profinanzas.core.domain.repository.*
import com.profinanzas.core.domain.usecase.auth.*
import com.profinanzas.core.domain.usecase.budget.*
import com.profinanzas.core.domain.usecase.category.*
import com.profinanzas.core.domain.usecase.transaction.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object AppModule {

    val authInterceptor: AuthInterceptor = AuthInterceptor()

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("${com.profinanzas.core.api.ApiConstants.BASE_URL}/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(apiService, authInterceptor)
    }

    val categoryRepository: CategoryRepository by lazy {
        CategoryRepositoryImpl(apiService)
    }

    val transactionRepository: TransactionRepository by lazy {
        TransactionRepositoryImpl(apiService)
    }

    val budgetRepository: BudgetRepository by lazy {
        BudgetRepositoryImpl(apiService)
    }

    val loginUseCase by lazy { LoginUseCase(authRepository) }
    val registerUseCase by lazy { RegisterUseCase(authRepository) }
    val logoutUseCase by lazy { LogoutUseCase(authRepository) }
    val listCategoriesUseCase by lazy { ListCategoriesUseCase(categoryRepository) }
    val createCategoryUseCase by lazy { CreateCategoryUseCase(categoryRepository) }
    val getTransactionsUseCase by lazy { GetTransactionsUseCase(transactionRepository) }
    val createTransactionUseCase by lazy { CreateTransactionUseCase(transactionRepository) }
    val getSummaryUseCase by lazy { GetSummaryUseCase(transactionRepository) }
    val getBudgetsUseCase by lazy { GetBudgetsUseCase(budgetRepository) }
    val createBudgetUseCase by lazy { CreateBudgetUseCase(budgetRepository) }
    val deleteBudgetUseCase by lazy { DeleteBudgetUseCase(budgetRepository) }
}
