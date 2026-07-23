package com.profinanzas.features.auth

import com.profinanzas.core.api.AuthInterceptor
import com.profinanzas.core.data.local.TokenManager
import com.profinanzas.core.domain.model.User
import com.profinanzas.core.domain.repository.AuthResult
import com.profinanzas.core.domain.usecase.auth.*
import com.profinanzas.di.AppModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val user: User? = null,
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)

class AuthViewModel(
    private val loginUseCase: LoginUseCase = AppModule.loginUseCase,
    private val registerUseCase: RegisterUseCase = AppModule.registerUseCase,
    private val logoutUseCase: LogoutUseCase = AppModule.logoutUseCase,
    private val tokenManager: TokenManager = AppModule.tokenManager,
    private val authInterceptor: AuthInterceptor = AppModule.authInterceptor,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        tryRestoreSession()
    }

    private fun tryRestoreSession() {
        scope.launch {
            if (tokenManager.isLoggedIn) {
                authInterceptor.accessToken = tokenManager.accessToken
                authInterceptor.refreshToken = tokenManager.refreshToken
                val user = User(
                    id = tokenManager.userId ?: "",
                    email = tokenManager.userEmail ?: "",
                    username = tokenManager.username ?: "",
                    firstName = null,
                    lastName = null,
                )
                _uiState.value = AuthUiState(
                    user = user,
                    isAuthenticated = true,
                )
            }
        }
    }

    fun login(email: String, password: String) {
        scope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            loginUseCase(email, password)
                .onSuccess { result ->
                    _uiState.value = _uiState.value.copy(
                        user = result.user,
                        isAuthenticated = true,
                        isLoading = false,
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Error al iniciar sesión",
                    )
                }
        }
    }

    fun register(email: String, username: String, password: String) {
        scope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            registerUseCase(email, username, password)
                .onSuccess { result ->
                    _uiState.value = _uiState.value.copy(
                        user = result.user,
                        isAuthenticated = true,
                        isLoading = false,
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Error al registrar",
                    )
                }
        }
    }

    fun logout() {
        scope.launch {
            val refreshToken = tokenManager.refreshToken ?: ""
            logoutUseCase(refreshToken)
            _uiState.value = AuthUiState()
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
