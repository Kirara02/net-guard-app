package com.uniguard.netguard_app.utils

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import platform.Network.*
import platform.darwin.dispatch_get_main_queue

class IOSNetworkMonitor : NetworkMonitor {

    override val isConnected: Flow<Boolean> = callbackFlow {
        val monitor = nw_path_monitor_create()
        nw_path_monitor_set_update_handler(monitor) { path ->
            val connected = nw_path_get_status(path) == nw_path_status_satisfied
            trySend(connected)
        }
        nw_path_monitor_set_queue(monitor, dispatch_get_main_queue())
        nw_path_monitor_start(monitor)

        awaitClose {
            nw_path_monitor_cancel(monitor)
        }
    }.distinctUntilChanged()
}

actual fun createNetworkMonitor(): NetworkMonitor = IOSNetworkMonitor()
