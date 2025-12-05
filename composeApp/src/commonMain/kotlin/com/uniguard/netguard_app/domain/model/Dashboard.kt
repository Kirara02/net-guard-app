package com.uniguard.netguard_app.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DashboardResponse(
    val success: Boolean,
    val message: String? =null,
    val data: Dashboard? = null,
    val error: String? = null
)

@Serializable
data class Dashboard(
    val overview: OverviewDashboard,
    val users: UserDashboard,
    val groups: GroupDashboard,
    val servers: ServerDashboard,
    val incidents: IncidentDashboard
)

@Serializable
data class OverviewDashboard(
    @SerialName("total_users")
    val totalUsers: Int,

    @SerialName("total_groups")
    val totalGroups: Int,

    @SerialName("total_servers")
    val totalServers: Int,

    @SerialName("active_sessions")
    val activeSessions: Int,

    @SerialName("unresolved_incidents")
    val unresolvedIncidents: Int,

    @SerialName("today_incidents")
    val todayIncidents: Int,
)


@Serializable
data class UserDashboard(
    @SerialName("by_role")
    val byRole: Map<String, Int> = emptyMap(),

    @SerialName("by_status")
    val byStatus: Map<String, Int> = emptyMap(),

    @SerialName("by_group")
    val byGroup: List<UserByGroup> = emptyList(),

    val recent: List<UserRecent> = emptyList()
)

@Serializable
data class UserByGroup(
    @SerialName("group_name")
    val groupName: String,

    val count: Int
)

@Serializable
data class UserRecent(
    val id: String,
    val name: String,
    val email: String,
    val role: String,

    @SerialName("group_name")
    val groupName: String? = null,

    @SerialName("created_at")
    val createdAt: String
)


@Serializable
data class GroupDashboard(
    val total: Int,
    val active: Int,
    val inactive: Int,

    @SerialName("with_members")
    val withMembers: List<GroupWithMembers> = emptyList()
)

@Serializable
data class GroupWithMembers(
    @SerialName("group_name")
    val groupName: String,

    @SerialName("member_count")
    val memberCount: Int,

    @SerialName("max_members")
    val maxMembers: Int,

    val utilization: Double,

    @SerialName("server_count")
    val serverCount: Int
)


@Serializable
data class ServerDashboard(
    val total: Int,

    @SerialName("by_group")
    val byGroup: List<ServerByGroup> = emptyList(),

    val recent: List<ServerRecent> = emptyList()
)

@Serializable
data class ServerByGroup(
    @SerialName("group_name")
    val groupName: String,

    val count: Int
)

@Serializable
data class ServerRecent(
    val id: String,
    val name: String,
    val url: String,

    @SerialName("group_name")
    val groupName: String,

    @SerialName("created_at")
    val createdAt: String
)


@Serializable
data class IncidentDashboard(
    val total: Int,
    val unresolved: Int,
    val resolved: Int,

    val recent: List<RecentIncident> = emptyList(),

    @SerialName("top_problematic")
    val topProblematic: List<TopProblematicServer> = emptyList(),

    @SerialName("avg_resolution_time")
    val avgResolutionTime: String,

    @SerialName("by_status")
    val byStatus: Map<String, Int> = emptyMap()
)

@Serializable
data class RecentIncident(
    val id: String,

    @SerialName("server_name")
    val serverName: String,

    val url: String,
    val status: String,
    val timestamp: String,

    @SerialName("resolved_at")
    val resolvedAt: String? = null
)

@Serializable
data class TopProblematicServer(
    @SerialName("server_name")
    val serverName: String,

    val url: String,

    @SerialName("incident_count")
    val incidentCount: Int
)

data class DashboardRowData(
    val title: String,
    val subtitle: String? = null,
    val badge: String? = null,
    val createdAt: String? = null // ✅ new
)