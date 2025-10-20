package com.uniguard.netguard_app.data.local.preferences

import com.uniguard.netguard_app.domain.model.User
import kotlinx.serialization.json.Json
import platform.Foundation.NSUserDefaults

actual class AuthPreferences {

    private val userDefaults = NSUserDefaults.standardUserDefaults()

    private object Keys {
        const val TOKEN = "auth_token"
        const val USER_DATA = "user_data"
    }

    actual fun saveToken(token: String) {
        userDefaults.setObject(token, Keys.TOKEN)
        userDefaults.synchronize()
    }

    actual fun getToken(): String? {
        return userDefaults.stringForKey(Keys.TOKEN)
    }

    actual fun saveUser(user: User) {
        val userJson = Json.encodeToString(user)
        userDefaults.setObject(userJson, Keys.USER_DATA)
        userDefaults.synchronize()
    }

    actual fun getUser(): User? {
        val userJson = userDefaults.stringForKey(Keys.USER_DATA)
        return userJson?.let {
            try {
                Json.decodeFromString<User>(it)
            } catch (e: Exception) {
                null
            }
        }
    }

    actual fun clearAll() {
        userDefaults.removeObjectForKey(Keys.TOKEN)
        userDefaults.removeObjectForKey(Keys.USER_DATA)
        userDefaults.synchronize()
    }

    actual fun isLoggedIn(): Boolean {
        return getToken() != null && getUser() != null
    }
}