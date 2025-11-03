package com.uniguard.netguard_app.data.repository

import com.uniguard.netguard_app.data.local.preferences.AppPreferences
import com.uniguard.netguard_app.data.remote.api.NetGuardApi
import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.CreateUserRequest
import com.uniguard.netguard_app.domain.model.UpdateUserRequest
import com.uniguard.netguard_app.domain.model.User
import com.uniguard.netguard_app.domain.repository.UserRepository

class UserRepositoryImpl(
    private val api: NetGuardApi,
    private val prefs: AppPreferences
) : UserRepository {
    override suspend fun getUsers(): ApiResult<List<User>> {
        val token = prefs.getToken()
        return if(token != null) {
            val result = api.getUsers(token)
            result
        } else {
            ApiResult.Error("No authentication token found")
        }
    }

    override suspend fun getUserById(userId: String): ApiResult<User> {
        val token = prefs.getToken()
        return if(token != null) {
            val result = api.getUserById(token, userId)
            result
        } else {
            ApiResult.Error("No authentication token found")
        }
    }

    override suspend fun createUser(request: CreateUserRequest): ApiResult<User> {
        val token = prefs.getToken()
        return if(token != null) {
            val result = api.createUser(token, request)
            result
        } else {
            ApiResult.Error("No authentication token found")
        }
    }

    override suspend fun updateUser(
        id: String,
        request: UpdateUserRequest
    ): ApiResult<User> {
        val token = prefs.getToken()
        return if(token != null) {
            val result = api.updateUser(token, id, request)
            result
        } else {
            ApiResult.Error("No authentication token found")
        }
    }

    override suspend fun deleteById(id: String): ApiResult<String> {
        val token = prefs.getToken()
        return if(token != null) {
            val result = api.deleteUserById(token, id)
            result
        } else {
            ApiResult.Error("No authentication token found")
        }
    }
}