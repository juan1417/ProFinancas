package com.profinanzas.core.data.local

import java.util.prefs.Preferences

class TokenManager {

    private val prefs: Preferences = Preferences.userNodeForPackage(TokenManager::class.java)

    var accessToken: String?
        get() = prefs.get(KEY_ACCESS_TOKEN, null)
        set(value) = value?.let { prefs.put(KEY_ACCESS_TOKEN, it) } ?: prefs.remove(KEY_ACCESS_TOKEN)

    var refreshToken: String?
        get() = prefs.get(KEY_REFRESH_TOKEN, null)
        set(value) = value?.let { prefs.put(KEY_REFRESH_TOKEN, it) } ?: prefs.remove(KEY_REFRESH_TOKEN)

    var userId: String?
        get() = prefs.get(KEY_USER_ID, null)
        set(value) = value?.let { prefs.put(KEY_USER_ID, it) } ?: prefs.remove(KEY_USER_ID)

    var userEmail: String?
        get() = prefs.get(KEY_USER_EMAIL, null)
        set(value) = value?.let { prefs.put(KEY_USER_EMAIL, it) } ?: prefs.remove(KEY_USER_EMAIL)

    var username: String?
        get() = prefs.get(KEY_USERNAME, null)
        set(value) = value?.let { prefs.put(KEY_USERNAME, it) } ?: prefs.remove(KEY_USERNAME)

    fun saveTokens(access: String, refresh: String) {
        accessToken = access
        refreshToken = refresh
    }

    fun saveUser(id: String, email: String, userName: String) {
        userId = id
        userEmail = email
        username = userName
    }

    fun clear() {
        prefs.remove(KEY_ACCESS_TOKEN)
        prefs.remove(KEY_REFRESH_TOKEN)
        prefs.remove(KEY_USER_ID)
        prefs.remove(KEY_USER_EMAIL)
        prefs.remove(KEY_USERNAME)
    }

    val isLoggedIn: Boolean
        get() = accessToken != null && refreshToken != null

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USERNAME = "username"
    }
}
