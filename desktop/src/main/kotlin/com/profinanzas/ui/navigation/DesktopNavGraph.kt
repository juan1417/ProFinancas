package com.profinanzas.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.profinanzas.core.ui.theme.*
import com.profinanzas.features.auth.AuthViewModel
import com.profinanzas.features.auth.LoginScreen
import com.profinanzas.features.auth.RegisterScreen
import com.profinanzas.features.auth.ProfileScreen
import com.profinanzas.features.categories.CategoriesScreen
import com.profinanzas.features.categories.CategoriesViewModel
import com.profinanzas.features.dashboard.DashboardViewModel
import com.profinanzas.features.dashboard.DashboardScreen
import com.profinanzas.features.facturas.FacturasViewModel
import com.profinanzas.features.facturas.FacturasScreen
import com.profinanzas.features.transactions.TransactionsScreen
import com.profinanzas.features.transactions.TransactionsViewModel
import com.profinanzas.features.wallet.WalletViewModel
import com.profinanzas.features.wallet.WalletScreen

@Composable
fun DesktopNavGraph() {
    val authViewModel = remember { AuthViewModel() }
    val authUiState by authViewModel.uiState.collectAsState()

    if (authUiState.isAuthenticated) {
        MainDesktopShell(
            authUiState = authUiState,
            onLogout = { authViewModel.logout() },
        )
    } else {
        var showRegister by remember { mutableStateOf(false) }

        if (showRegister) {
            RegisterScreen(
                onRegisterSuccess = { showRegister = false },
                onBack = { showRegister = false },
                viewModel = authViewModel,
            )
        } else {
            LoginScreen(
                onLoginSuccess = { },
                onNavigateToRegister = { showRegister = true },
                viewModel = authViewModel,
            )
        }
    }
}

@Composable
private fun MainDesktopShell(
    authUiState: com.profinanzas.features.auth.AuthUiState,
    onLogout: () -> Unit,
) {
    var selectedRoute by remember { mutableStateOf(Screen.Dashboard.route) }
    var showProfile by remember { mutableStateOf(false) }

    val dashboardViewModel = remember { DashboardViewModel() }
    val transactionsViewModel = remember { TransactionsViewModel() }
    val facturasViewModel = remember { FacturasViewModel() }
    val walletViewModel = remember { WalletViewModel() }
    val categoriesViewModel = remember { CategoriesViewModel() }

    val sidebarItems = listOf(
        SidebarItem("Dashboard", Icons.Default.Dashboard, Screen.Dashboard.route),
        SidebarItem("Transacciones", Icons.Default.SwapHoriz, Screen.Transactions.route),
        SidebarItem("Facturas", Icons.Default.Receipt, Screen.Facturas.route),
        SidebarItem("Categorías", Icons.Default.Category, Screen.Categories.route),
        SidebarItem("Tarjetas", Icons.Default.CreditCard, Screen.Wallet.route),
    )

    val sidebarUser = SidebarUser(
        initials = authUiState.user?.fullName?.take(2)?.uppercase() ?: "?",
        name = authUiState.user?.fullName ?: "Usuario",
        email = authUiState.user?.email ?: "",
    )

    if (showProfile) {
        ProfileScreen(
            user = authUiState.user,
            onLogout = {
                showProfile = false
                onLogout()
            },
            onNavigateBack = { showProfile = false },
        )
    } else {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(Bg),
        ) {
            Sidebar(
                items = sidebarItems,
                selectedRoute = selectedRoute,
                onItemSelected = { selectedRoute = it },
                user = sidebarUser,
                onLogout = onLogout,
            )

            Column(modifier = Modifier.weight(1f)) {
                DesktopTopBar(
                    title = sidebarItems.firstOrNull { it.route == selectedRoute }?.label ?: "Dashboard",
                    userInitials = sidebarUser.initials,
                    onProfileClick = { showProfile = true },
                )

                when (selectedRoute) {
                    Screen.Dashboard.route -> DashboardScreen(dashboardViewModel)
                    Screen.Transactions.route -> TransactionsScreen(transactionsViewModel)
                    Screen.Facturas.route -> FacturasScreen(facturasViewModel)
                    Screen.Categories.route -> CategoriesScreen(categoriesViewModel)
                    Screen.Wallet.route -> WalletScreen(walletViewModel)
                    else -> DashboardScreen(dashboardViewModel)
                }
            }
        }
    }
}
