package com.profinanzas.core.api

import com.google.gson.JsonParser
import com.profinanzas.core.data.local.TokenManager
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Renueva el access token cuando el backend responde 401.
 *
 * Usa un cliente HTTP propio (sin este Authenticator) para el refresh y evita
 * recursión. Como el backend rota el refresh token, guarda también el nuevo.
 */
@Singleton
class TokenRefreshAuthenticator @Inject constructor(
    private val authInterceptor: AuthInterceptor,
    private val tokenManager: TokenManager,
) : Authenticator {

    private val refreshClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (priorResponseCount(response) >= 2) return null

        val refresh = authInterceptor.refreshToken ?: return null

        synchronized(this) {
            val failedAccess = response.request.header("Authorization")
                ?.removePrefix("Bearer ")
                ?.trim()
            val currentAccess = authInterceptor.accessToken
            // Otro hilo ya refrescó mientras esta request esperaba el lock.
            if (currentAccess != null && currentAccess != failedAccess) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentAccess")
                    .build()
            }

            val payload = """{"refresh":${quote(refresh)}}""".toRequestBody(JSON)
            val refreshRequest = Request.Builder()
                .url("${ApiConstants.BASE_URL}/${ApiConstants.TOKEN_REFRESH}")
                .post(payload)
                .build()

            return try {
                refreshClient.newCall(refreshRequest).execute().use { refreshResponse ->
                    if (!refreshResponse.isSuccessful) {
                        onRefreshFailed()
                        return null
                    }
                    val json = refreshResponse.body?.string()
                        ?.let { JsonParser.parseString(it).asJsonObject }
                    val newAccess = json?.get("access")
                        ?.takeIf { !it.isJsonNull }
                        ?.asString
                    if (newAccess.isNullOrBlank()) {
                        onRefreshFailed()
                        return null
                    }
                    val newRefresh = json.get("refresh")
                        ?.takeIf { !it.isJsonNull }
                        ?.asString
                    authInterceptor.accessToken = newAccess
                    tokenManager.accessToken = newAccess
                    if (newRefresh != null) {
                        authInterceptor.refreshToken = newRefresh
                        tokenManager.refreshToken = newRefresh
                    }
                    response.request.newBuilder()
                        .header("Authorization", "Bearer $newAccess")
                        .build()
                }
            } catch (_: Exception) {
                onRefreshFailed()
                null
            }
        }
    }

    private fun onRefreshFailed() {
        authInterceptor.accessToken = null
        authInterceptor.refreshToken = null
        tokenManager.clear()
    }

    private fun priorResponseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }

    /** Escapa el refresh token para incrustarlo en un JSON mínimo. */
    private fun quote(value: String): String = buildString {
        append('"')
        for (char in value) {
            when (char) {
                '"' -> append("\\\"")
                '\\' -> append("\\\\")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(char)
            }
        }
        append('"')
    }

    private companion object {
        val JSON = "application/json; charset=utf-8".toMediaType()
    }
}
