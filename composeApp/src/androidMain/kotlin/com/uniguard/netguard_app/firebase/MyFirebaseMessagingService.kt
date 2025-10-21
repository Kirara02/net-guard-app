package com.uniguard.netguard_app.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.uniguard.netguard_app.R
import com.uniguard.netguard_app.log

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val channelId = "server_status"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        log { "New Token: $token" }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        log { "Notification data: ${remoteMessage.data}" }

        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"]
        val message = remoteMessage.notification?.body ?: remoteMessage.data["body"]

        showNotification(title, message)
    }

    private fun showNotification(title: String?, message: String?) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title ?: "No Title")
            .setContentText(message ?: "No Message")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)

        notificationManager.notify(0, notificationBuilder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Server Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for server status updates"
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
