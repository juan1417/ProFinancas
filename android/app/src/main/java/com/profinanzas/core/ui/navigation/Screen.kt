package com.profinanzas.core.ui.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Profile : Screen("profile")
    data object Dashboard : Screen("dashboard")
    data object Facturas : Screen("facturas")
    data object Wallet : Screen("wallet")
    data object Transactions : Screen("transactions")
    data object Scanner : Screen("scanner")
    data object Analytics : Screen("analytics")
    data object Categories : Screen("categories")
    data object Budgets : Screen("budgets")
}
