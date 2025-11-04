package com.uniguard.netguard_app.firebase

import cocoapods.FirebaseMessaging.FIRMessaging
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
actual object FirebaseTopicManager {
    actual fun subscribe(topic: String) {
        FIRMessaging.messaging().subscribeToTopic(topic)
    }

    actual fun unsubscribe(topic: String) {
        FIRMessaging.messaging().unsubscribeFromTopic(topic)
    }
}
