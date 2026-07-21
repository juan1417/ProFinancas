package com.profinanzas.ui.navigation

sealed class Screen(val route: String, val label: String) {
    data object Dashboard : Screen("dashboard", "Dashboard")
    data object Facturas : Screen("facturas", "Facturas")
    data object Wallet : Screen("wallet", "Tarjetas")
    data object Profile : Screen("profile", "Configuración")
}
