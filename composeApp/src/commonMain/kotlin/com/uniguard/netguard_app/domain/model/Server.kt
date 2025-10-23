package com.uniguard.netguard_app.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Server(
    val id: String,
    val name: String,
    val url: String,
    @SerialName("created_by") val createdBy: String,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class CreateServerRequest(
    val name: String,
    val url: String
)

@Serializable
data class UpdateServerStatusRequest(
    val status: String,
    val responseTime: Long? = null
)

@Serializable
data class ServerResponse(
    val success: Boolean,
    val message: String,
    val data: Server? = null,
    val error: String? = null
)

@Serializable
data class ServersResponse(
    val success: Boolean,
    val data: List<Server> = emptyList(),
    val error: String? = null
)

enum class ServerStatus {
    UP,
    DOWN,
    UNKNOWN,
    RESOLVED
}