package com.uniguard.netguard_app.worker

expect class ServerMonitoringScheduler() {
    fun scheduleServerMonitoring(intervalMinutes: Long = 30)
    fun cancelServerMonitoring()
}