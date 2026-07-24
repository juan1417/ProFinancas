package com.profinanzas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.profinanzas.core.ui.navigation.MainShell
import com.profinanzas.core.ui.navigation.Screen
import com.profinanzas.core.ui.theme.ProFinanzasTheme
import com.profinanzas.features.auth.presentation.screens.LoginScreen
import com.profinanzas.features.auth.presentation.screens.RegisterScreen
import com.profinanzas.features.auth.presentation.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProFinanzasTheme {
                ProFinanzasNavHost()
            }
        }
    }
}

@Composable
fun ProFinanzasNavHost() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val authUiState by authViewModel.uiState.collectAsState()

    if (authUiState.isAuthenticated) {
        MainShell(
            authUiState = authUiState,
            onLogout = { authViewModel.logout() },
            onNavigateToLogin = {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            },
            onUpdateProfile = { firstName, lastName, username ->
                authViewModel.updateProfile(firstName, lastName, username)
            },
        )
    } else {
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = { /* AuthViewModel updates state */ },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    viewModel = authViewModel,
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = { /* AuthViewModel updates state */ },
                    onBack = { navController.popBackStack() },
                    viewModel = authViewModel,
                )
            }
        }
    }
}
