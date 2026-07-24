package com.profinanzas.core.domain.usecase.auth

import com.profinanzas.core.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String) =
        repository.login(email, password)
}

class RegisterUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(email: String, username: String, password: String) =
        repository.register(email, username, password)
}

class LogoutUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(refreshToken: String) =
        repository.logout(refreshToken)
}

class UpdateProfileUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(firstName: String?, lastName: String?, username: String?) =
        repository.updateProfile(firstName, lastName, username)
}
