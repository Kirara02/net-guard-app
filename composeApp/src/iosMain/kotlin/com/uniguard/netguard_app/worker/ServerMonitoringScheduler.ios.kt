package com.uniguard.netguard_app.worker

actual class ServerMonitoringScheduler actual constructor() {

    private val worker = ServerMonitoringWorker()

    actual fun scheduleServerMonitoring(intervalMinutes: Long?) {
        val interval = intervalMinutes ?: 30L
        worker.scheduleMonitoring(interval)
    }

    actual fun cancelServerMonitoring() {
        worker.cancelMonitoring()
    }
}