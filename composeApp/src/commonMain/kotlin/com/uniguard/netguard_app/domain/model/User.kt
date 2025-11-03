package com.uniguard.netguard_app.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String,
    val division: String? = null,
    val phone: String? = null,
    val role: String,
    val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val success: Boolean,
    val message: String? = "Something Wrong" ,
    val data: AuthData? = null,
    val error: String? = null
)



@Serializable
data class AuthData(
    val token: String,
    val user: User
)

@Serializable
data class UserResponse(
    val success: Boolean,
    val message: String? = "Something Wrong" ,
    val data: User? = null,
    val error: String? = null
)

@Serializable
data class UsersResponse(
    val success: Boolean,
    val message: String? = "Something Wrong" ,
    val data: List<User> = emptyList(),
    val error: String? = null
)


@Serializable
data class ErrorResponse(
    val success: Boolean,
    val error: String
)

@Serializable
data class UpdateProfileRequest(
    val name: String,
    val division: String,
    val phone: String
)

@Serializable
data class UpdateProfileResponse(
    val success: Boolean,
    val message: String,
    val data: User? = null,
    val error: String? = null
)


@Serializable
data class ChangePasswordRequest(
    @SerialName("current_password") val currentPassword: String,
    @SerialName("new_password") val newPassword: String
)

@Serializable
data class CreateUserRequest(
    val name: String,
    val email: String,
    val password: String,
    val division: String? = null,
    val phone: String? = null,
    val role: String
)

@Serializable
data class UpdateUserRequest(
    val name: String? = null,
    val email: String? = null,
    val password: String? = null,
    val division: String? = null,
    val phone: String? = null,
    val role: String? = null
)