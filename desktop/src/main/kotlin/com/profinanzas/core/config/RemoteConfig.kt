package com.profinanzas.core.config

import com.google.gson.JsonParser
import com.profinanzas.core.api.ApiConstants
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object RemoteConfig {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    @Volatile
    var features: Map<String, Boolean> = emptyMap()
        private set

    fun refresh(): Map<String, Boolean> {
        return try {
            val request = Request.Builder()
                .url("${ApiConstants.BASE_URL}/app/config/")
                .get()
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return features
                val body = response.body?.string() ?: return features
                val json = JsonParser.parseString(body).asJsonObject
                val obj = json.getAsJsonObject("features") ?: return features
                val map = mutableMapOf<String, Boolean>()
                for ((k, v) in obj.entrySet()) {
                    map[k] = v.asBoolean
                }
                features = map
                map
            }
        } catch (_: Exception) {
            features
        }
    }

    fun isEnabled(key: String): Boolean = features[key] == true
}
