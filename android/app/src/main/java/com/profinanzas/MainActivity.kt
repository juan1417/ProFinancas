package com.profinanzas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.profinanzas.core.api.ApiConstants
import com.profinanzas.core.config.RemoteConfig
import com.profinanzas.core.ui.navigation.MainShell
import com.profinanzas.core.ui.navigation.Screen
import com.profinanzas.core.ui.theme.AppTheme
import com.profinanzas.core.ui.theme.ProFinanzasTheme
import com.profinanzas.core.update.AppVersionInfo
import com.profinanzas.core.update.UpdateChecker
import com.profinanzas.features.auth.presentation.screens.LoginScreen
import com.profinanzas.features.auth.presentation.screens.RegisterScreen
import com.profinanzas.features.auth.presentation.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AppTheme.init(this)
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

    var updateInfo by remember { mutableStateOf<AppVersionInfo?>(null) }
    LaunchedEffect(authUiState.isAuthenticated) {
        if (authUiState.isAuthenticated) {
            withContext(Dispatchers.IO) {
                RemoteConfig.refresh()
                updateInfo = UpdateChecker.check(ApiConstants.BASE_URL)
            }
        }
    }

    if (updateInfo != null && updateInfo!!.forceUpdate) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Actualización disponible") },
            text = { Text(updateInfo!!.changelog.ifBlank { "Hay una nueva versión disponible." }) },
            confirmButton = {
                TextButton(onClick = {
                    // Abrir URL de descarga en el navegador
                    val intent = android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse(updateInfo!!.downloadUrl)
                    )
                    navController.context.startActivity(intent)
                }) { Text("Descargar") }
            },
            dismissButton = {
                TextButton(onClick = { updateInfo = null }) { Text("Más tarde") }
            },
        )
    }

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
