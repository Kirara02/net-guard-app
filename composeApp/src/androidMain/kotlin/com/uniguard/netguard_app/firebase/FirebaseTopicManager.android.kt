package com.uniguard.netguard_app.firebase

import com.google.firebase.messaging.FirebaseMessaging

actual object FirebaseTopicManager {
    actual fun subscribe(topic: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
    }

    actual fun unsubscribe(topic: String) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
    }
}
