package com.uniguard.netguard_app.worker

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.uniguard.netguard_app.Logger
import com.uniguard.netguard_app.di.applicationContext
import java.util.concurrent.TimeUnit

actual class ServerMonitoringScheduler actual constructor() {

    actual fun scheduleServerMonitoring(intervalMinutes: Long?) {
        val interval = intervalMinutes ?: 15L
        val context = applicationContext
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Only run when network is available
            .build()

        val workRequest = PeriodicWorkRequestBuilder<ServerMonitoringWorker>(
            interval, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE, // Replace existing work if it exists
            workRequest
        )

        Logger.i("Android WorkManager: Server monitoring scheduled with $interval minutes interval", tag = "ServerMonitoring")
    }

    actual fun cancelServerMonitoring() {
        val context = applicationContext
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        Logger.i("Android WorkManager: Server monitoring cancelled", tag = "ServerMonitoring")
    }

    companion object Companion {
        private const val WORK_NAME = "server_monitoring_work"
    }
}