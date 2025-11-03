package com.uniguard.netguard_app.domain.repository

import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.CreateUserRequest
import com.uniguard.netguard_app.domain.model.UpdateUserRequest
import com.uniguard.netguard_app.domain.model.User

interface UserRepository {
    suspend fun getUsers() : ApiResult<List<User>>
    suspend fun getUserById(userId: String) : ApiResult<User>
    suspend fun createUser(request: CreateUserRequest) : ApiResult<User>
    suspend fun updateUser(id: String, request: UpdateUserRequest) : ApiResult<User>
    suspend fun deleteById(id: String) : ApiResult<String>
}