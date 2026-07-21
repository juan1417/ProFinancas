package com.profinanzas.core.api

object ApiConstants {
    private const val DEFAULT_HOST = "localhost:8000"
    const val BASE_URL = "http://$DEFAULT_HOST/api"

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
}
