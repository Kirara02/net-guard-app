package com.uniguard.netguard_app.domain.repository

import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.AuthData
import com.uniguard.netguard_app.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): ApiResult<AuthData>
    suspend fun register(request: com.uniguard.netguard_app.domain.model.RegisterRequest): ApiResult<AuthData>
    suspend fun getCurrentUser(): ApiResult<User>
    fun saveAuthData(token: String, user: User)
    fun getSavedToken(): String?
    fun getSavedUser(): User?
    fun clearAuthData()
    fun isLoggedIn(): Boolean
}