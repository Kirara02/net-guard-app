package com.uniguard.netguard_app.domain.repository

import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.Server
import com.uniguard.netguard_app.domain.model.ServerStatus
import kotlinx.coroutines.flow.Flow

interface ServerRepository {
    // Remote operations
    suspend fun syncServersFromRemote(): ApiResult<List<Server>>
    suspend fun createServer(name: String, url: String): ApiResult<Server>
    suspend fun updateServer(serverId: String, name: String, url: String): ApiResult<Server>
    suspend fun deleteServer(serverId: String): ApiResult<Unit>
    suspend fun updateServerStatus(serverId: String, status: ServerStatus, responseTime: Long? = null): ApiResult<Server>

    // Local operations
    fun getAllServers(): Flow<List<Server>>
    suspend fun getServerById(serverId: String): Server?
    suspend fun insertOrUpdateServer(server: Server)
    suspend fun insertOrUpdateServers(servers: List<Server>)
    suspend fun deleteServerLocally(serverId: String)
    suspend fun clearAllServers()

    // Sync operations
    suspend fun syncServerToRemote(server: Server): ApiResult<Server>
    suspend fun syncAllServersToRemote(): ApiResult<List<Server>>
}