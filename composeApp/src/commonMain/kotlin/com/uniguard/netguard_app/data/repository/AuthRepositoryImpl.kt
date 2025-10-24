package com.uniguard.netguard_app.data.repository

import com.uniguard.netguard_app.data.local.preferences.AuthPreferences
import com.uniguard.netguard_app.data.remote.api.NetGuardApi
import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.AuthData
import com.uniguard.netguard_app.domain.model.LoginRequest
import com.uniguard.netguard_app.domain.model.RegisterRequest
import com.uniguard.netguard_app.domain.model.UpdateProfileRequest
import com.uniguard.netguard_app.domain.model.User
import com.uniguard.netguard_app.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val api: NetGuardApi,
    private val authPreferences: AuthPreferences
) : AuthRepository {

    override suspend fun login(email: String, password: String): ApiResult<AuthData> {
        return try {
            val result = api.login(
                LoginRequest(email, password)
            )
            when (result) {
                is ApiResult.Success -> {
                    // Save auth data locally
                    saveAuthData(result.data.token, result.data.user)
                    result
                }
                is ApiResult.Error -> result
                is ApiResult.Loading -> result
                is ApiResult.Initial -> result
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Login failed")
        }
    }

    override suspend fun register(request: RegisterRequest): ApiResult<AuthData> {
        return try {
            val result = api.register(request)
            when (result) {
                is ApiResult.Success -> {
                    // Save auth data locally
                    saveAuthData(result.data.token, result.data.user)
                    result
                }
                is ApiResult.Error -> result
                is ApiResult.Loading -> result
                is ApiResult.Initial -> result
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Registration failed")
        }
    }

    override suspend fun getCurrentUser(): ApiResult<User> {
        val token = getSavedToken()
        return if (token != null) {
            api.getCurrentUser(token)
        } else {
            ApiResult.Error("No authentication token found")
        }
    }

    override suspend fun updateProfile(request: UpdateProfileRequest): ApiResult<User> {
        val token = getSavedToken()
        return if (token != null) {
            val result = api.updateProfile(token, request)
            when (result) {
                is ApiResult.Success -> {
                    // Update saved user data with new profile info
                    saveAuthData(token, result.data)
                    result
                }
                is ApiResult.Error -> result
                is ApiResult.Loading -> result
                is ApiResult.Initial -> result
            }
        } else {
            ApiResult.Error("No authentication token found")
        }
    }

    override fun saveAuthData(token: String, user: User) {
        authPreferences.saveToken(token)
        authPreferences.saveUser(user)
    }

    override fun getSavedToken(): String? {
        return authPreferences.getToken()
    }

    override fun getSavedUser(): User? {
        return authPreferences.getUser()
    }

    override fun clearAuthData() {
        authPreferences.clearAll()
    }

    override fun isLoggedIn(): Boolean {
        return authPreferences.isLoggedIn()
    }
}