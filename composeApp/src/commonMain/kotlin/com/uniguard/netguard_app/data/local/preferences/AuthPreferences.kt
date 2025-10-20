package com.uniguard.netguard_app.data.local.preferences

import com.uniguard.netguard_app.domain.model.User

expect class AuthPreferences {
    fun saveToken(token: String)
    fun getToken(): String?
    fun saveUser(user: User)
    fun getUser(): User?
    fun clearAll()
    fun isLoggedIn(): Boolean
}