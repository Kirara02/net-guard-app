package com.uniguard.netguard_app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.uniguard.netguard_app.Logger
import com.uniguard.netguard_app.data.local.database.DatabaseProvider
import com.uniguard.netguard_app.data.local.preferences.AuthPreferences
import com.uniguard.netguard_app.data.remote.api.NetGuardApi
import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.ServerStatus
import com.uniguard.netguard_app.domain.model.UpdateServerStatusRequest
import com.uniguard.netguard_app.utils.createNetworkMonitor
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.measureTime

class ServerMonitoringWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {
    
    private val httpClient: HttpClient by inject()
    private val api: NetGuardApi by inject()
    private val databaseProvider: DatabaseProvider by inject()
    private val authPreferences: AuthPreferences by inject()

    private val networkMonitor = createNetworkMonitor()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Logger.i("Android WorkManager: Server monitoring worker started", tag = "ServerMonitoring")

        val isOnline = networkMonitor.isConnected.first()
        if (!isOnline) {
            Logger.w("Android WorkManager: No internet connection — retry later", tag = "ServerMonitoring")
            return@withContext Result.retry()
        }


        try {
            val database = databaseProvider.getDatabase()
            val serverQueries = database.appDatabaseQueries

            // Get all servers from local database
            val servers = serverQueries.getAllServers().executeAsList()
            Logger.i("Android WorkManager: Found ${servers.size} servers to monitor", tag = "ServerMonitoring")

            if (servers.isEmpty()) {
                Logger.i("Android WorkManager: No servers to monitor, finishing", tag = "ServerMonitoring")
                return@withContext Result.success()
            }

            val token = authPreferences.getToken()
            if (token == null) {
                Logger.w("Android WorkManager: No authentication token found", tag = "ServerMonitoring")
                return@withContext Result.failure()
            }

            // Check each server
            for (serverEntity in servers) {
                Logger.d("Android WorkManager: Checking server ${serverEntity.name} (${serverEntity.url})", tag = "ServerMonitoring")

                val responseTime = measureTime {
                    try {
                        val response: HttpResponse = httpClient.get(serverEntity.url)
                        val isUp = response.status.isSuccess()

                        val status = if (isUp) ServerStatus.UP else ServerStatus.DOWN
                        Logger.i("Android WorkManager: Server ${serverEntity.name} is ${status.name}", tag = "ServerMonitoring")

                        // If server is down, update status via API
                        if (!isUp) {
                            val updateResult = api.updateServerStatus(
                                token = token,
                                serverId = serverEntity.id,
                                request = UpdateServerStatusRequest(
                                    status = status.name,
                                    responseTime = null // Could add response time measurement
                                )
                            )

                            when (updateResult) {
                                is ApiResult.Success -> {
                                    Logger.i("Android WorkManager: Successfully updated status for server ${serverEntity.name}", tag = "ServerMonitoring")
                                }
                                is ApiResult.Error -> {
                                    Logger.e("Android WorkManager: Failed to update status for server ${serverEntity.name}: ${updateResult.message}", tag = "ServerMonitoring")
                                }

                                else -> {}
                            }
                        }

                    } catch (e: Exception) {
                        Logger.w("Android WorkManager: Exception checking server ${serverEntity.name}: ${e.message}", tag = "ServerMonitoring")
                        // Server is down - update status
                        val updateResult = api.updateServerStatus(
                            token = token,
                            serverId = serverEntity.id,
                            request = UpdateServerStatusRequest(
                                status = ServerStatus.DOWN.name,
                                responseTime = null
                            )
                        )

                        when (updateResult) {
                            is ApiResult.Success -> {
                                Logger.i("Android WorkManager: Successfully updated DOWN status for server ${serverEntity.name}", tag = "ServerMonitoring")
                            }
                            is ApiResult.Error -> {
                                Logger.e("Android WorkManager: Failed to update DOWN status for server ${serverEntity.name}: ${updateResult.message}", tag = "ServerMonitoring")
                            }

                            else -> {}
                        }
                    }
                }

                Logger.d("Android WorkManager: Server ${serverEntity.name} checked in ${responseTime.inWholeMilliseconds}ms", tag = "ServerMonitoring")
            }

            Logger.i("Android WorkManager: Server monitoring completed successfully", tag = "ServerMonitoring")
            Result.success()
        } catch (e: Exception) {
            Logger.e("Android WorkManager: Server monitoring failed with exception: ${e.message}", tag = "ServerMonitoring")
            Result.failure()
        }
    }

    // Network check moved to common NetworkUtils
}