package com.uniguard.netguard_app.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.messaging.messaging

object FirebaseTopicManager {
    fun subscribe(topic: String) {
        Firebase.messaging.subscribeToTopic(topic)
    }
    fun unsubscribe(topic: String) {
        Firebase.messaging.unsubscribeFromTopic(topic)
    }
}