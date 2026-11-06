package com.profinanzas.core.update

import com.profinanzas.core.api.ApiConstants
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

data class AppVersionInfo(
    val latestVersion: String,
    val minVersion: String,
    val downloadUrl: String,
    val changelog: String,
    val forceUpdate: Boolean,
)

object UpdateChecker {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    fun check(): AppVersionInfo? {
        return try {
            val request = Request.Builder()
                .url("${ApiConstants.BASE_URL}/app/version/")
                .get()
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body?.string() ?: return null
                val json = JsonParser.parseString(body).asJsonObject
                AppVersionInfo(
                    latestVersion = json.get("latest_version")?.asString ?: "",
                    minVersion = json.get("min_version")?.asString ?: "",
                    downloadUrl = json.get("download_url_desktop")?.asString ?: "",
                    changelog = json.get("changelog")?.asString ?: "",
                    forceUpdate = json.get("force_update")?.asBoolean ?: false,
                )
            }
        } catch (_: Exception) {
            null
        }
    }
}
