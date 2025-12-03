package com.uniguard.netguard_app.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Group(
    val id: String,
    val name: String,
    val description: String? = null,
    @SerialName("max_members") val maxMembers: Int,
    @SerialName("current_members") val currentMembers: Int,
    @SerialName("is_active") val isActive: Boolean,
    @SerialName("created_by") val createdBy: String,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class GroupRequest(
    val name: String,
    val description: String? = null,
    @SerialName("max_members") val maxMembers: Int = 1
)


@Serializable
data class GroupsResponse(
    val success: Boolean,
    val data: List<Group> = emptyList(),
    val error: String? = null
)

@Serializable
data class GroupResponse(
    val success: Boolean,
    val message: String,
    val data: Group? = null,
    val error: String? = null
)

@Serializable
data class GroupInfo(
    val id: String,
    val name: String
)

fun Group.toInfo() = GroupInfo(
    id = id,
    name = name
)