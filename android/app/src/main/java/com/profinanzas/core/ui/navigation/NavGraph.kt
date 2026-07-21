package com.profinanzas.core.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.profinanzas.core.ui.components.ProAppBar
import com.profinanzas.core.ui.components.ProBottomNav
import com.profinanzas.core.ui.components.BottomNavItem
import com.profinanzas.features.auth.presentation.screens.ProfileScreen
import com.profinanzas.features.auth.presentation.viewmodel.AuthUiState
import com.profinanzas.features.dashboard.presentation.screens.DashboardScreen
import com.profinanzas.features.facturas.presentation.screens.FacturasScreen
import com.profinanzas.features.wallet.presentation.screens.WalletScreen

@Composable
fun MainShell(
    authUiState: AuthUiState,
    onLogout: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    var showProfile by remember { mutableStateOf(false) }

    val items = listOf(
        BottomNavItem("Inicio", Icons.Default.Home, Screen.Dashboard.route),
        BottomNavItem("Facturas", Icons.Default.Receipt, Screen.Facturas.route),
        BottomNavItem("Tarjetas", Icons.Default.CreditCard, Screen.Wallet.route),
    )

    val selectedIndex = items.indexOfFirst { it.route == currentDestination?.route }.coerceAtLeast(0)

    if (showProfile) {
        ProfileScreen(
            uiState = authUiState,
            onLogout = {
                showProfile = false
                onLogout()
            },
            onNavigateBack = { showProfile = false },
        )
    } else {
        Scaffold(
            topBar = {
                ProAppBar(
                    title = "ProFinanzas",
                    onProfileClick = { showProfile = true },
                )
            },
            bottomBar = {
                ProBottomNav(
                    items = items,
                    selectedIndex = selectedIndex,
                    onItemSelected = { index ->
                        navController.navigate(items[index].route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            },
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.padding(padding),
            ) {
                composable(Screen.Dashboard.route) { DashboardScreen() }
                composable(Screen.Facturas.route) { FacturasScreen() }
                composable(Screen.Wallet.route) { WalletScreen() }
            }
        }
    }

    // Auto-navigate to login when logged out
    LaunchedEffect(authUiState.isAuthenticated) {
        if (!authUiState.isAuthenticated && !authUiState.isLoading) {
            onNavigateToLogin()
        }
    }
}
