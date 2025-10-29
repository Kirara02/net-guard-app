package com.uniguard.netguard_app.data.repository

import com.uniguard.netguard_app.data.local.preferences.AppPreferences
import com.uniguard.netguard_app.data.remote.api.NetGuardApi
import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.AuthData
import com.uniguard.netguard_app.domain.model.ChangePasswordRequest
import com.uniguard.netguard_app.domain.model.LoginRequest
import com.uniguard.netguard_app.domain.model.RegisterRequest
import com.uniguard.netguard_app.domain.model.UpdateProfileRequest
import com.uniguard.netguard_app.domain.model.User
import com.uniguard.netguard_app.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val api: NetGuardApi,
    private val appPreferences: AppPreferences
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
        appPreferences.saveToken(token)
        appPreferences.saveUser(user)
    }

    override fun getSavedToken(): String? {
        return appPreferences.getToken()
    }

    override fun getSavedUser(): User? {
        return appPreferences.getUser()
    }

    override fun clearAuthData() {
        appPreferences.clearAll()
    }

    override suspend fun changePassword(request: ChangePasswordRequest): ApiResult<Unit> {
        val token = getSavedToken()
        return if (token != null) {
            api.changePassword(token, request)
        } else {
            ApiResult.Error("No authentication token found")
        }
    }

    override fun isLoggedIn(): Boolean {
        return appPreferences.isLoggedIn()
    }
}