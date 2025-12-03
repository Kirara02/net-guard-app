package com.uniguard.netguard_app.data.repository

import com.uniguard.netguard_app.data.local.database.DatabaseProvider
import com.uniguard.netguard_app.data.local.preferences.AppPreferences
import com.uniguard.netguard_app.data.remote.api.NetGuardApi
import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.GroupInfo
import com.uniguard.netguard_app.domain.model.ServerRequest
import com.uniguard.netguard_app.domain.model.Server
import com.uniguard.netguard_app.domain.model.ServerStatus
import com.uniguard.netguard_app.domain.model.UpdateServerStatusRequest
import com.uniguard.netguard_app.domain.repository.ServerRepository
import com.uniguard.netguardapp.db.ServerEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ServerRepositoryImpl(
    databaseProvider: DatabaseProvider,
    private val api: NetGuardApi,
    private val appPreferences: AppPreferences
) : ServerRepository {

    private val database = databaseProvider.getDatabase()
    private val serverQueries = database.appDatabaseQueries

    // Remote operations
    override suspend fun syncServersFromRemote(
        withLocal: Boolean,
    ): ApiResult<List<Server>> {
        val token = appPreferences.getToken()
        return if (token != null) {
            val result = api.getServers(token)
            when (result) {
                is ApiResult.Success -> {
                    if (withLocal) {
                        insertOrUpdateServers(result.data)
                    }
                    result
                }
                else -> result
            }
        } else {
            ApiResult.Error("No authentication token found")
        }
    }

    override suspend fun createServer(
        name: String,
        url: String,
        groupId: String?,
        withLocal: Boolean
    ): ApiResult<Server> {
        val token = appPreferences.getToken()
        return if (token != null) {
            val result = api.createServer(token, ServerRequest(name, url, groupId))
            when (result) {
                is ApiResult.Success -> {
                    if (withLocal) {
                        insertOrUpdateServer(result.data)
                    }
                    result
                }
                else -> result
            }
        } else {
            ApiResult.Error("No authentication token found")
        }
    }

    override suspend fun updateServer(
        serverId: String,
        name: String,
        url: String,
        groupId: String?,
        withLocal: Boolean
    ): ApiResult<Server> {
        val token = appPreferences.getToken()
        return if (token != null) {
            val result = api.updateServer(token, serverId, ServerRequest(name, url, groupId))
            when (result) {
                is ApiResult.Success -> {
                    if (withLocal) {
                        insertOrUpdateServer(result.data)
                    }
                    result
                }
                else -> result
            }
        } else {
            ApiResult.Error("No authentication token found")
        }
    }

    override suspend fun deleteServer(
        serverId: String,
        withLocal: Boolean
    ): ApiResult<Unit> {
        val token = appPreferences.getToken()
        return if (token != null) {
            val result = api.deleteServer(token, serverId)
            when (result) {
                is ApiResult.Success -> {
                    if (withLocal) {
                        deleteServerLocally(serverId)
                    }
                    result
                }
                else -> result
            }
        } else {
            ApiResult.Error("No authentication token found")
        }
    }

    override suspend fun updateServerStatus(serverId: String, status: ServerStatus, responseTime: Long?): ApiResult<Server> {
        val token = appPreferences.getToken()
        return if (token != null) {
            val result = api.updateServerStatus(
                token,
                serverId,
                UpdateServerStatusRequest(status.name, responseTime)
            )
            when (result) {
                is ApiResult.Success -> {
                    // Update local database
                    insertOrUpdateServer(result.data)
                    result
                }
                else -> result
            }
        } else {
            ApiResult.Error("No authentication token found")
        }
    }

    // Local operations
    override fun getAllServers(): Flow<List<Server>> {
        return flow {
            val entities = serverQueries.getAllServers().executeAsList()
            emit(entities.map { it.toDomain() })
        }
    }

    override suspend fun getServerById(serverId: String): Server? {
        return serverQueries.getServerById(serverId).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun insertOrUpdateServer(server: Server) {
        serverQueries.insertServer(
            id = server.id,
            name = server.name,
            url = server.url,
            group_id = server.group.id,
            created_by = server.createdBy,
            created_at = server.createdAt
        )
    }

    override suspend fun insertOrUpdateServers(servers: List<Server>) {
        servers.forEach { insertOrUpdateServer(it) }
    }

    override suspend fun deleteServerLocally(serverId: String) {
        serverQueries.deleteServerById(serverId)
    }

    override suspend fun clearAllServers() {
        serverQueries.clearServers()
    }

    // Sync operations
    override suspend fun syncServerToRemote(server: Server): ApiResult<Server> {
        return updateServer(server.id, server.name, server.url, withLocal = true)
    }

    override suspend fun syncAllServersToRemote(): ApiResult<List<Server>> {
        val localServers = serverQueries.getAllServers().executeAsList().map { it.toDomain() }
        val results = mutableListOf<Server>()

        for (server in localServers) {
            when (val result = syncServerToRemote(server)) {
                is ApiResult.Success -> results.add(result.data)
                is ApiResult.Error -> return ApiResult.Error(result.message, result.code)
                is ApiResult.Loading -> continue
                is ApiResult.Initial -> continue
            }
        }

        return ApiResult.Success(results)
    }

    // Extension function to convert database entity to domain model
    private fun ServerEntity.toDomain(): Server {
        return Server(
            id = id,
            name = name,
            url = url,
            group = GroupInfo(
                id = group_id,
                name = ""
            ),
            createdBy = created_by,
            createdAt = created_at,
        )
    }
}