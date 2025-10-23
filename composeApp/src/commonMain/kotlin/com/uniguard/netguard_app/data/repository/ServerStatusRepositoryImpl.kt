package com.uniguard.netguard_app.data.repository

import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.repository.ServerStatusRepository
import com.uniguard.netguard_app.data.local.database.DatabaseProvider
import com.uniguard.netguardapp.db.ServerStatusEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ServerStatusRepositoryImpl(
    databaseProvider: DatabaseProvider
) : ServerStatusRepository {

    private val database = databaseProvider.getDatabase()
    private val serverStatusQueries = database.appDatabaseQueries

    override fun getAllServerStatuses(): Flow<List<ServerStatusEntity>> {
        return flow {
            val entities = serverStatusQueries.getAllServerStatuses().executeAsList()
            emit(entities)
        }
    }

    override suspend fun getServerStatus(serverId: String): ServerStatusEntity? {
        return serverStatusQueries.getServerStatus(serverId).executeAsOneOrNull()
    }

    override suspend fun updateServerStatus(
        serverId: String,
        status: String,
        lastChecked: String,
        responseTime: Long?,
        updatedAt: String
    ): ApiResult<Unit> {
        return try {
            serverStatusQueries.insertOrUpdateServerStatus(
                server_id = serverId,
                status = status,
                last_checked = lastChecked,
                response_time = responseTime,
                updated_at = updatedAt
            )
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Failed to update server status")
        }
    }

    override suspend fun deleteServerStatus(serverId: String): ApiResult<Unit> {
        return try {
            serverStatusQueries.deleteServerStatus(serverId)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Failed to delete server status")
        }
    }

    override suspend fun clearAllStatuses(): ApiResult<Unit> {
        return try {
            serverStatusQueries.clearServerStatuses()
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Failed to clear server statuses")
        }
    }
}