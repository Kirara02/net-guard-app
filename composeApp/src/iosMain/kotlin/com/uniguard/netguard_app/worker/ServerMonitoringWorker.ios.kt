package com.uniguard.netguard_app.worker

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
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import platform.BackgroundTasks.BGTask
import platform.BackgroundTasks.BGProcessingTaskRequest
import platform.BackgroundTasks.BGTaskScheduler
import platform.Foundation.NSDate
import platform.Foundation.dateWithTimeIntervalSinceNow
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalForeignApi::class)
class ServerMonitoringWorker : KoinComponent {

    private val httpClient: HttpClient by inject()
    private val api: NetGuardApi by inject()
    private val databaseProvider: DatabaseProvider by inject()
    private val authPreferences: AuthPreferences by inject()
    private val networkMonitor = createNetworkMonitor()

    fun scheduleMonitoring(intervalMinutes: Long = 30) {
        val scheduler = BGTaskScheduler.sharedScheduler

        // Cancel existing tasks
        scheduler.cancelAllTaskRequests()

        // Create new background task request
        val request = BGProcessingTaskRequest("com.uniguard.netguard_app.server_check")
        request.earliestBeginDate = NSDate.dateWithTimeIntervalSinceNow(intervalMinutes.minutes.inWholeSeconds.toDouble())
        request.requiresNetworkConnectivity = true
        request.requiresExternalPower = false

        val success = scheduler.submitTaskRequest(request, null)
        if (success) {
            Logger.i("iOS BGTaskScheduler: Background task scheduled successfully with $intervalMinutes minutes interval", tag = "ServerMonitoring")
        } else {
            Logger.w("iOS BGTaskScheduler: Failed to schedule background task (may be normal on simulator)", tag = "ServerMonitoring")
        }
    }

    fun cancelMonitoring() {
        val scheduler = BGTaskScheduler.sharedScheduler
        scheduler.cancelAllTaskRequests()
        Logger.i("iOS BGTaskScheduler: Background monitoring cancelled", tag = "ServerMonitoring")
    }

    fun handleBackgroundTask(task: BGTask) {
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            val isOnline = networkMonitor.isConnected.first()
            if (!isOnline) {
                Logger.w("No internet — skipping task", tag = "ServerMonitoring")
                task.setTaskCompletedWithSuccess(false)
                return@launch
            }

            val success = performMonitoring()
            if (success) scheduleMonitoring()
            task.setTaskCompletedWithSuccess(success)
        }

        task.expirationHandler = {
            scope.launch { Logger.w("BGTask expired", tag = "ServerMonitoring") }
        }
    }

    private suspend fun performMonitoring(): Boolean {
        Logger.i("iOS BGTaskScheduler: Background monitoring started", tag = "ServerMonitoring")

        return try {
            val database = databaseProvider.getDatabase()
            val serverQueries = database.appDatabaseQueries

            // Get all servers from local database
            val servers = serverQueries.getAllServers().executeAsList()
            Logger.i("iOS BGTaskScheduler: Found ${servers.size} servers to monitor", tag = "ServerMonitoring")

            if (servers.isEmpty()) {
                Logger.i("iOS BGTaskScheduler: No servers to monitor, finishing", tag = "ServerMonitoring")
                return true
            }

            val token = authPreferences.getToken()
            if (token == null) {
                Logger.w("iOS BGTaskScheduler: No authentication token found", tag = "ServerMonitoring")
                return false
            }

            // Check each server
            for (serverEntity in servers) {
                Logger.d("iOS BGTaskScheduler: Checking server ${serverEntity.name} (${serverEntity.url})", tag = "ServerMonitoring")

                try {
                    val response: HttpResponse = httpClient.get(serverEntity.url)
                    val isUp = response.status.isSuccess()

                    val status = if (isUp) ServerStatus.UP else ServerStatus.DOWN
                    Logger.i("iOS BGTaskScheduler: Server ${serverEntity.name} is ${status.name}", tag = "ServerMonitoring")

                    // If server is down, update status via API
                    if (!isUp) {
                        val updateResult = api.updateServerStatus(
                            token = token,
                            serverId = serverEntity.id,
                            request = UpdateServerStatusRequest(
                                status = status.name,
                                responseTime = null
                            )
                        )

                        when (updateResult) {
                            is ApiResult.Success -> {
                                Logger.i("iOS BGTaskScheduler: Successfully updated status for server ${serverEntity.name}", tag = "ServerMonitoring")
                            }
                            is ApiResult.Error -> {
                                Logger.e("iOS BGTaskScheduler: Failed to update status for server ${serverEntity.name}: ${updateResult.message}", tag = "ServerMonitoring")
                            }

                            else -> {}
                        }
                    }

                } catch (e: Exception) {
                    Logger.w("iOS BGTaskScheduler: Exception checking server ${serverEntity.name}: ${e.message}", tag = "ServerMonitoring")
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
                            Logger.i("iOS BGTaskScheduler: Successfully updated DOWN status for server ${serverEntity.name}", tag = "ServerMonitoring")
                        }
                        is ApiResult.Error -> {
                            Logger.e("iOS BGTaskScheduler: Failed to update DOWN status for server ${serverEntity.name}: ${updateResult.message}", tag = "ServerMonitoring")
                        }

                        else -> {}
                    }
                }
            }

            Logger.i("iOS BGTaskScheduler: Background monitoring completed successfully", tag = "ServerMonitoring")
            true
        } catch (e: Exception) {
            Logger.e("iOS BGTaskScheduler: Background monitoring failed: ${e.message}", tag = "ServerMonitoring")
            false
        }
    }

}