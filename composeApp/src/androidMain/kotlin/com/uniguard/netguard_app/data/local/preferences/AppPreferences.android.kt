package com.uniguard.netguard_app.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.uniguard.netguard_app.domain.model.User
import com.uniguard.netguard_app.log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_prefs")

actual class AppPreferences(private val context: Context) {

    private object PreferencesKeys {
        val TOKEN = stringPreferencesKey("token")
        val USER_DATA = stringPreferencesKey("user_data")
        val THEME_PREFERENCE = booleanPreferencesKey("theme_preference")
        val LANGUAGE = stringPreferencesKey("language")
    }

    actual fun saveToken(token: String) {
        runBlocking {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.TOKEN] = token
            }
        }
    }

    actual fun getToken(): String? {
        return runBlocking {
            context.dataStore.data.first()[PreferencesKeys.TOKEN]
        }
    }

    actual fun saveUser(user: User) {
        runBlocking {
            val userJson = Json.encodeToString(user)
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.USER_DATA] = userJson
            }
        }
    }

    actual fun getUser(): User? {
        return runBlocking {
            val userJson = context.dataStore.data.first()[PreferencesKeys.USER_DATA]
            userJson?.let {
                try {
                    Json.decodeFromString<User>(it)
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    actual fun clearAll() {
        runBlocking {
            context.dataStore.edit { preferences ->
                preferences.remove(PreferencesKeys.TOKEN)
                preferences.remove(PreferencesKeys.USER_DATA)
            }
        }
    }

    actual fun isLoggedIn(): Boolean {
        return getToken() != null && getUser() != null
    }

    actual fun saveThemePreference(isDarkMode: Boolean) {
        runBlocking {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.THEME_PREFERENCE] = isDarkMode
            }
        }
    }

    actual val themePreferenceFlow: Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.THEME_PREFERENCE] ?: false
        }

    actual fun saveLanguage(code: String) {
        runBlocking {
            context.dataStore.edit { prefs -> prefs[PreferencesKeys.LANGUAGE] = code }
        }
    }

    actual val languageFlow: Flow<String> =
        context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.LANGUAGE] ?: "en"
        }
}