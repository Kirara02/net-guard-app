package com.uniguard.netguard_app.utils

import io.ktor.client.plugins.logging.Logger
import com.uniguard.netguard_app.Logger as AppLogger

class KtorNapierLogger : Logger {
    override fun log(message: String) {
        AppLogger.d(message, tag = "Ktor")
    }
}