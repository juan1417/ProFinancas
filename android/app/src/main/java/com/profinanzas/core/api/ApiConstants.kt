package com.profinanzas.core.api

object ApiConstants {
    // Base configurada en build.gradle.kts (BuildConfig.API_BASE_URL).
    // Default: producción Vercel. Para desarrollo local:
    //   ./gradlew :app:assembleDebug -PAPI_BASE_URL=http://10.0.2.2:8000/api
    val BASE_URL: String = com.profinanzas.BuildConfig.API_BASE_URL.trimEnd('/')

    // Auth
    const val REGISTER = "auth/register/"
    const val LOGIN = "auth/login/"
    const val LOGOUT = "auth/logout/"
    const val PROFILE = "auth/profile/"
    const val CHANGE_PASSWORD = "auth/change-password/"
    const val TOKEN_REFRESH = "auth/token/refresh/"

    // Categories
    const val CATEGORIES = "categories/"
    const val CATEGORIES_BY_TYPE = "categories/by_type/"

    // Transactions
    const val TRANSACTIONS = "transactions/"
    const val TRANSACTIONS_SUMMARY = "transactions/summary/"
    const val TRANSACTIONS_BY_CATEGORY = "transactions/by_category/"

    // Budgets
    const val BUDGETS = "budgets/"

    // Invoices
    const val INVOICES = "invoices/"
}
