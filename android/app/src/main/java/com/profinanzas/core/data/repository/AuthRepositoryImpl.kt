package com.profinanzas.core.data.repository

import com.profinanzas.core.api.ApiService
import com.profinanzas.core.api.AuthInterceptor
import com.profinanzas.core.data.local.TokenManager
import com.profinanzas.core.data.model.*
import com.profinanzas.core.domain.model.*
import com.profinanzas.core.domain.repository.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val authInterceptor: AuthInterceptor,
    private val tokenManager: TokenManager,
) : AuthRepository {

    private fun UserDto.toDomain() = User(id, email, username, firstName, lastName)

    override suspend fun login(email: String, password: String): Result<AuthResult> = runCatching {
        val response = api.login(mapOf("email" to email, "password" to password))
        if (response.isSuccessful) {
            val body = response.body()!!
            authInterceptor.accessToken = body.access
            authInterceptor.refreshToken = body.refresh
            tokenManager.saveTokens(body.access, body.refresh)
            tokenManager.saveUser(body.user.id, body.user.email, body.user.username)
            AuthResult(body.user.toDomain(), body.access, body.refresh)
        } else {
            throw Exception(extractError(response.errorBody()?.string()))
        }
    }

    override suspend fun register(email: String, username: String, password: String, passwordConfirm: String): Result<AuthResult> = runCatching {
        val response = api.register(mapOf(
            "email" to email, "username" to username, "password" to password, "password_confirm" to passwordConfirm
        ))
        if (response.isSuccessful) {
            val body = response.body()!!
            authInterceptor.accessToken = body.access
            authInterceptor.refreshToken = body.refresh
            tokenManager.saveTokens(body.access, body.refresh)
            tokenManager.saveUser(body.user.id, body.user.email, body.user.username)
            AuthResult(body.user.toDomain(), body.access, body.refresh)
        } else {
            throw Exception(extractError(response.errorBody()?.string()))
        }
    }

    override suspend fun logout(refreshToken: String): Result<Unit> = runCatching {
        val response = api.logout(mapOf("refresh" to refreshToken))
        if (response.isSuccessful || response.code() == 204) {
            authInterceptor.accessToken = null
            authInterceptor.refreshToken = null
            tokenManager.clear()
        } else {
            throw Exception(extractError(response.errorBody()?.string()))
        }
    }

    override suspend fun getProfile(): Result<User> = runCatching {
        val response = api.getProfile()
        if (response.isSuccessful) response.body()!!.toDomain()
        else throw Exception(extractError(response.errorBody()?.string()))
    }

    override suspend fun updateProfile(
        firstName: String?,
        lastName: String?,
        username: String?,
    ): Result<User> = runCatching {
        val body = mutableMapOf<String, String>()
        firstName?.let { body["first_name"] = it }
        lastName?.let { body["last_name"] = it }
        username?.let { body["username"] = it }
        val response = api.updateProfile(body)
        if (response.isSuccessful) response.body()!!.toDomain()
        else throw Exception(extractError(response.errorBody()?.string()))
    }

    override suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit> = runCatching {
        val response = api.changePassword(mapOf(
            "old_password" to oldPassword,
            "new_password" to newPassword,
            "new_password_confirm" to newPassword,
        ))
        if (!response.isSuccessful) throw Exception(extractError(response.errorBody()?.string()))
    }

    override suspend fun refreshToken(refreshToken: String): Result<String> = runCatching {
        val response = api.refreshToken(mapOf("refresh" to refreshToken))
        if (response.isSuccessful) {
            val token = response.body()!!.access
            authInterceptor.accessToken = token
            tokenManager.accessToken = token
            token
        } else {
            throw Exception(extractError(response.errorBody()?.string()))
        }
    }

    private fun extractError(errorBody: String?): String {
        if (errorBody == null) return "Error desconocido"
        return try {
            val json = com.google.gson.JsonParser.parseString(errorBody).asJsonObject
            when {
                json.has("detail") -> json.get("detail").asString
                json.has("non_field_errors") -> {
                    json.getAsJsonArray("non_field_errors").get(0).asString
                }
                else -> {
                    val firstKey = json.keySet().firstOrNull() ?: return "Error"
                    val value = json.get(firstKey)
                    if (value.isJsonArray) value.asJsonArray.get(0).asString
                    else value.asString
                }
            }
        } catch (_: Exception) {
            "Error del servidor"
        }
    }
}
