package com.profinanzas.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profinanzas.core.api.AuthInterceptor
import com.profinanzas.core.data.local.TokenManager
import com.profinanzas.core.domain.model.User
import com.profinanzas.core.domain.repository.AuthResult
import com.profinanzas.core.domain.usecase.auth.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val user: User? = null,
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val tokenManager: TokenManager,
    private val authInterceptor: AuthInterceptor,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        tryRestoreSession()
    }

    private fun tryRestoreSession() {
        viewModelScope.launch {
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
        viewModelScope.launch {
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
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            registerUseCase(email, username, password)
                .onSuccess { result ->
                    _uiState.value = _uiState.value.copy(
                        user = result.user,
                        isAuthenticated = true,
                        isLoading = false,
                        successMessage = "Registro exitoso",
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
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val refreshToken = tokenManager.refreshToken ?: ""
            logoutUseCase(refreshToken)
                .onSuccess {
                    _uiState.value = AuthUiState()
                }
                .onFailure {
                    tokenManager.clear()
                    _uiState.value = AuthUiState()
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun updateProfile(firstName: String?, lastName: String?, username: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            updateProfileUseCase(firstName, lastName, username)
                .onSuccess { user ->
                    tokenManager.saveUser(user.id, user.email, user.username)
                    _uiState.value = _uiState.value.copy(
                        user = user,
                        isLoading = false,
                        successMessage = "Perfil actualizado",
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Error al actualizar perfil",
                    )
                }
        }
    }
}
