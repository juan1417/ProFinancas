package com.profinanzas.core.config

import com.profinanzas.core.api.ApiConstants
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
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
                val json = JSONObject(response.body?.string() ?: return features)
                val obj = json.optJSONObject("features") ?: return features
                val map = mutableMapOf<String, Boolean>()
                val keys = obj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    map[k] = obj.optBoolean(k, false)
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
