package com.uniguard.netguard_app.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class History(
    val id: String,
    @SerialName("server_id") val serverId: String,
    @SerialName("server_name") val serverName: String,
    val url: String,
    val status: String,
    val timestamp: String,
    @SerialName("created_by") val createdBy: String,
    @SerialName("resolved_by") val resolvedBy: String? = null,
    @SerialName("resolved_at") val resolvedAt: String? = null,
    @SerialName("resolve_note") val resolveNote: String? = null,
    @SerialName("assigned_to") val assignedTo: String? = null
)

@Serializable
data class CreateHistoryRequest(
    @SerialName("server_id") val serverId: String,
    @SerialName("server_name") val serverName: String,
    val url: String,
    val status: String
)

@Serializable
data class ResolveHistoryRequest(
    @SerialName("resolve_note") val resolveNote: String
)

@Serializable
data class HistoryResponse(
    val success: Boolean,
    val message: String,
    val data: History? = null,
    val error: String? = null
)

@Serializable
data class HistoriesResponse(
    val success: Boolean,
    val data: List<History> = emptyList(),
    val error: String? = null
)

@Serializable
data class MonthlyReportResponse(
    val success: Boolean,
    val message: String,
    val data: MonthlyReportData? = null,
    val error: String? = null
)

@Serializable
data class MonthlyReportData(
    val year: Int,
    val month: Int,
    val report: List<ServerReport>
)

@Serializable
data class ServerReport(
    val serverId: String,
    val serverName: String,
    val url: String,
    val downCount: Int,
    val resolvedCount: Int,
    val avgResolutionTime: Double
)