package com.profinanzas.core.api

object ApiConstants {
    // Default: producción Vercel. Para desarrollo local, pasar una de estas:
    //   - env:    PROFINANZAS_API_BASE_URL=http://localhost:8000/api
    //   - system: -Dprofinanzas.apiBaseUrl=http://localhost:8000/api
    val BASE_URL: String = (
        System.getenv("PROFINANZAS_API_BASE_URL")
            ?: System.getProperty("profinanzas.apiBaseUrl")
            ?: "https://back-wine-nu.vercel.app/api"
        ).trimEnd('/')

    const val REGISTER = "auth/register/"
    const val LOGIN = "auth/login/"
    const val LOGOUT = "auth/logout/"
    const val PROFILE = "auth/profile/"
    const val CHANGE_PASSWORD = "auth/change-password/"
    const val TOKEN_REFRESH = "auth/token/refresh/"

    const val CATEGORIES = "categories/"
    const val CATEGORIES_BY_TYPE = "categories/by_type/"

    const val TRANSACTIONS = "transactions/"
    const val TRANSACTIONS_SUMMARY = "transactions/summary/"
    const val TRANSACTIONS_BY_CATEGORY = "transactions/by_category/"

    const val BUDGETS = "budgets/"

    const val INVOICES = "invoices/"
}
