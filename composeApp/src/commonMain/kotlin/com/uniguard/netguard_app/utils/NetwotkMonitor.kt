package com.uniguard.netguard_app.utils

import kotlinx.coroutines.flow.Flow

/**
 * Shared NetworkMonitor interface for KMP.
 * Emits `true` when network is available, `false` when disconnected.
 */
interface NetworkMonitor {
    val isConnected: Flow<Boolean>
}

/**
 * Platform-specific provider
 */
expect fun createNetworkMonitor(): NetworkMonitor