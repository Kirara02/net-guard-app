package com.uniguard.netguard_app.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReportsResponse(
    val success: Boolean,
    val data: List<Report> = emptyList(),
    val error: String? = null
)

@Serializable
data class ExportResponse(
    val success: Boolean,
    val data: String? = null, // URL or file path for download
    val error: String? = null
)

@Serializable
data class Report(
    val id: String,
    @SerialName("server_id") val serverId: String,
    @SerialName("server_name") val serverName: String,
    val url: String,
    val status: String,
    val timestamp: String,
    @SerialName("created_by") val createdBy: String,
    @SerialName("resolved_by") val resolvedBy: String? = null,
    @SerialName("resolved_at") val resolvedAt: String? = null,
    @SerialName("resolve_note") val resolveNote: String? = null
)

@Serializable
data class ReportParams(
    val status: ServerStatus? = null,
    @SerialName("server_name") val serverName: String? = null,
    val limit: Int? = null,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("end_date")   val endDate: String? = null,
    val format: ReportType? = null
)

enum class ReportType(val value: String) {
    EXCEL("excel"),
    PDF("pdf")
}
