package com.uniguard.netguard_app.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.uniguard.netguard_app.domain.model.User
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

actual class AuthPreferences(private val context: Context) {

    private object PreferencesKeys {
        val TOKEN = stringPreferencesKey("token")
        val USER_DATA = stringPreferencesKey("user_data")
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
                preferences.clear()
            }
        }
    }

    actual fun isLoggedIn(): Boolean {
        return getToken() != null && getUser() != null
    }
}