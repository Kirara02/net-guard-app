package com.uniguard.netguard_app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.uniguard.netguard_app.Logger
import com.uniguard.netguard_app.data.local.database.DatabaseProvider
import com.uniguard.netguard_app.data.local.preferences.AppPreferences
import com.uniguard.netguard_app.data.remote.api.NetGuardApi
import com.uniguard.netguard_app.domain.model.ApiResult
import com.uniguard.netguard_app.domain.model.ServerStatus
import com.uniguard.netguard_app.domain.model.UpdateServerStatusRequest
import com.uniguard.netguard_app.domain.repository.ServerStatusRepository
import com.uniguard.netguard_app.utils.createNetworkMonitor
import com.uniguard.netguard_app.utils.getCurrentTimestamp
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlin.time.Instant
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ServerMonitoringWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val httpClient: HttpClient by inject()
    private val api: NetGuardApi by inject()
    private val databaseProvider: DatabaseProvider by inject()
    private val appPreferences: AppPreferences by inject()
    private val serverStatusRepository: ServerStatusRepository by inject()

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
            val servers = serverQueries.getAllServers().executeAsList()

            Logger.i("Found ${servers.size} servers to monitor", tag = "ServerMonitoring")
            if (servers.isEmpty()) return@withContext Result.success()

            val token = appPreferences.getToken() ?: return@withContext Result.failure()

            for (serverEntity in servers) {
                Logger.d("Checking server ${serverEntity.name} (${serverEntity.url})", tag = "ServerMonitoring")

                val startTime = Instant.parse(getCurrentTimestamp())

                try {
                    val response: HttpResponse = httpClient.get(serverEntity.url)
                    val isUp = response.status.isSuccess()

                    val endTime = Instant.parse(getCurrentTimestamp())
                    val durationMillis = (endTime.epochSeconds - startTime.epochSeconds) * 1000 +
                            ((endTime.nanosecondsOfSecond - startTime.nanosecondsOfSecond) / 1_000_000)

                    val status = if (isUp) ServerStatus.UP else ServerStatus.DOWN
                    Logger.i("Server ${serverEntity.name} is ${status.name} (${durationMillis}ms)", tag = "ServerMonitoring")

                    val updateLocalResult = serverStatusRepository.updateServerStatus(
                        serverId = serverEntity.id,
                        status = status.name,
                        lastChecked = getCurrentTimestamp(),
                        responseTime = durationMillis,
                        updatedAt = getCurrentTimestamp()
                    )

                    when (updateLocalResult) {
                        is ApiResult.Success -> Logger.d("Updated local status for ${serverEntity.name}", tag = "ServerMonitoring")
                        is ApiResult.Error -> Logger.e("Failed local update for ${serverEntity.name}: ${updateLocalResult.message}", tag = "ServerMonitoring")
                        else -> {}
                    }

                    if (!isUp) {
                        val updateResult = api.updateServerStatus(
                            token = token,
                            serverId = serverEntity.id,
                            request = UpdateServerStatusRequest(
                                status = status.name,
                                responseTime = durationMillis
                            )
                        )

                        when (updateResult) {
                            is ApiResult.Success -> Logger.i("Updated status via API for ${serverEntity.name}", tag = "ServerMonitoring")
                            is ApiResult.Error -> Logger.e("Failed API update for ${serverEntity.name}: ${updateResult.message}", tag = "ServerMonitoring")
                            else -> {}
                        }
                    }

                } catch (e: Exception) {
                    val endTime = Instant.parse(getCurrentTimestamp())
                    val durationMillis = (endTime.epochSeconds - startTime.epochSeconds) * 1000 +
                            ((endTime.nanosecondsOfSecond - startTime.nanosecondsOfSecond) / 1_000_000)

                    Logger.w("Exception checking ${serverEntity.name}: ${e.message}", tag = "ServerMonitoring")

                    serverStatusRepository.updateServerStatus(
                        serverId = serverEntity.id,
                        status = ServerStatus.DOWN.name,
                        lastChecked = getCurrentTimestamp(),
                        responseTime = durationMillis,
                        updatedAt = getCurrentTimestamp()
                    )

                    api.updateServerStatus(
                        token = token,
                        serverId = serverEntity.id,
                        request = UpdateServerStatusRequest(
                            status = ServerStatus.DOWN.name,
                            responseTime = durationMillis
                        )
                    )
                }
            }

            Logger.i("Server monitoring completed successfully", tag = "ServerMonitoring")
            Result.success()

        } catch (e: Exception) {
            Logger.e("Server monitoring failed: ${e.message}", tag = "ServerMonitoring")
            Result.failure()
        }
    }
}
