package com.uniguard.netguard_app.worker

expect class ServerMonitoringScheduler() {
    fun scheduleServerMonitoring(intervalMinutes: Long? = null)
    fun cancelServerMonitoring()
}