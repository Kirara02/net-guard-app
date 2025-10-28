package com.uniguard.netguard_app.data.local.preferences

import com.uniguard.netguard_app.domain.model.User
import kotlinx.coroutines.flow.Flow

expect class AppPreferences {
    fun saveToken(token: String)
    fun getToken(): String?
    fun saveUser(user: User)
    fun getUser(): User?
    fun clearAll()
    fun isLoggedIn(): Boolean
    fun saveThemePreference(isDarkMode: Boolean)
    val themePreferenceFlow: Flow<Boolean>
    fun saveLanguage(code: String)
    val languageFlow: Flow<String>

}