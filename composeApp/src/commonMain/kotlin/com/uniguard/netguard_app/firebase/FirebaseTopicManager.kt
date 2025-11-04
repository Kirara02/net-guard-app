package com.uniguard.netguard_app.firebase

expect object FirebaseTopicManager {
    fun subscribe(topic: String)
    fun unsubscribe(topic: String)
}