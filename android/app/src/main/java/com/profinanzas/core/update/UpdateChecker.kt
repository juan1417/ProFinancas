package com.profinanzas.core.update

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
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

    fun check(baseUrl: String): AppVersionInfo? {
        return try {
            val request = Request.Builder()
                .url("${baseUrl.trimEnd('/')}/app/version/")
                .get()
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val json = JSONObject(response.body?.string() ?: return null)
                AppVersionInfo(
                    latestVersion = json.optString("latest_version", ""),
                    minVersion = json.optString("min_version", ""),
                    downloadUrl = json.optString("download_url_android", ""),
                    changelog = json.optString("changelog", ""),
                    forceUpdate = json.optBoolean("force_update", false),
                )
            }
        } catch (_: Exception) {
            null
        }
    }
}
