package com.uniguard.netguard_app.domain.service

import com.uniguard.netguard_app.data.local.preferences.AppPreferences
import com.uniguard.netguard_app.domain.model.User
import com.uniguard.netguard_app.domain.model.UserRole
import com.uniguard.netguard_app.firebase.FirebaseTopicManager
import com.uniguard.netguard_app.log
import com.uniguard.netguard_app.worker.ServerMonitoringScheduler

class UserSessionService(
    private val preferences: AppPreferences,
    private val scheduler: ServerMonitoringScheduler,
    private val firebase: FirebaseTopicManager
) {

    private fun currentUser(): User? = preferences.getUser()

    private fun isAllowed(): Boolean {
        val user = currentUser() ?: return false
        return user.userRole != UserRole.SUPER_ADMIN
    }

    private fun topicForUser(): String? {
        val user = currentUser() ?: return null
        if (!isAllowed()) return null
        val groupId = user.group?.id
        return "serverdown_group_$groupId"
    }

    fun getUserRole(): UserRole? {
        return currentUser()?.userRole
    }

    // ---------------------------
    // FCM
    // ---------------------------

    fun subscribeTopic() {
        log { "subscribe topic: ${topicForUser()}" }
        topicForUser()?.let { firebase.subscribe(it) }
    }

    fun unsubscribeTopic() {
        log { "unsubscribe topic: ${topicForUser()}" }
        topicForUser()?.let { firebase.unsubscribe(it) }
    }

    // ---------------------------
    // Monitoring
    // ---------------------------

    fun startMonitoring(interval: Long? = null) {
        if (isAllowed()) {
            scheduler.scheduleServerMonitoring(interval)
        }
    }

    fun stopMonitoring() {
        if (isAllowed()) {
            scheduler.cancelServerMonitoring()
        }
    }

    // ---------------------------
    // One-call session functions
    // ---------------------------

    fun startSession() {
        subscribeTopic()
        startMonitoring(preferences.getMonitoringInterval())
    }

    fun endSession() {
        unsubscribeTopic()
        stopMonitoring()
    }
}
