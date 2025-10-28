package com.uniguard.netguard_app.data.local.preferences

import com.uniguard.netguard_app.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import platform.Foundation.NSUserDefaults

actual class AppPreferences {

    private val userDefaults = NSUserDefaults.standardUserDefaults()

    private object Keys {
        const val TOKEN = "auth_token"
        const val USER_DATA = "user_data"
        const val THEME_PREFERENCE = "theme_preference"
        const val LANG = "language"
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

    actual fun saveThemePreference(isDarkMode: Boolean) {
        userDefaults.setBool(isDarkMode, Keys.THEME_PREFERENCE)
        userDefaults.synchronize()
        _themePreferenceFlow.value = isDarkMode
    }

    private val _themePreferenceFlow = MutableStateFlow(
        userDefaults.boolForKey(Keys.THEME_PREFERENCE)
    )

    actual val themePreferenceFlow : Flow<Boolean> = _themePreferenceFlow.asStateFlow()
    private val _langFlow = MutableStateFlow(userDefaults.stringForKey(Keys.LANG) ?: "en")
    actual val languageFlow : Flow<String> = _langFlow.asStateFlow()

    actual fun saveLanguage(code: String) {
        userDefaults.setObject(code, Keys.LANG)
        userDefaults.synchronize()
        _langFlow.value = code
    }
}