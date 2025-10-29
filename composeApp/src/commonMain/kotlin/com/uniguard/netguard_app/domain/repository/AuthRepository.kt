package com.uniguard.netguard_app.domain.repository

import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.AuthData
import com.uniguard.netguard_app.domain.model.ChangePasswordRequest
import com.uniguard.netguard_app.domain.model.RegisterRequest
import com.uniguard.netguard_app.domain.model.UpdateProfileRequest
import com.uniguard.netguard_app.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): ApiResult<AuthData>
    suspend fun register(request: RegisterRequest): ApiResult<AuthData>
    suspend fun getCurrentUser(): ApiResult<User>
    suspend fun updateProfile(request: UpdateProfileRequest): ApiResult<User>
    suspend fun changePassword(request: ChangePasswordRequest): ApiResult<Unit>
    fun saveAuthData(token: String, user: User)
    fun getSavedToken(): String?
    fun getSavedUser(): User?
    fun clearAuthData()
    fun isLoggedIn(): Boolean
}