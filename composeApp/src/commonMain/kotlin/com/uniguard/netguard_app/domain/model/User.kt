package com.uniguard.netguard_app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String,
    val division: String,
    val phone: String,
    val role: String,
    val isActive: Boolean = true,
    val createdAt: String? = null
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val division: String,
    val phone: String,
    val role: String = "USER"
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
data class ErrorResponse(
    val success: Boolean,
    val error: String
)