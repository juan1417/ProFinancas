package com.profinanzas.core.domain.usecase.auth

import com.profinanzas.core.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String) =
        repository.login(email, password)
}

class RegisterUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, username: String, password: String) =
        repository.register(email, username, password)
}

class LogoutUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(refreshToken: String) =
        repository.logout(refreshToken)
}
