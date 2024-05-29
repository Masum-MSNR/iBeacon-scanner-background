package dev.almasum.ibeacon_scanner_background.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

class MyNotification {
    companion object {
        const val NOTIFICATION_ID = 101
        private const val SERVICE_CHANNEL_ID = "inv_app_ibeacon"
        private const val SERVICE_CHANNEL_NAME = "iBeacon Scanner"
        private const val REGULAR_CHANNEL_ID = "inv_app_ibeacon.notifier"
        private const val REGULAR_CHANNEL_NAME = "Notifier"

        fun createNotificationChannels(context: Context) {
            val serviceNotificationChannel = NotificationChannel(
                SERVICE_CHANNEL_ID,
                SERVICE_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                vibrationPattern = longArrayOf(0)
            }

            val regularNotificationChannel = NotificationChannel(
                REGULAR_CHANNEL_ID,
                REGULAR_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                vibrationPattern = longArrayOf(0)
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(serviceNotificationChannel)
            notificationManager.createNotificationChannel(regularNotificationChannel)
        }

        fun showNotification(
            context: Context,
            id: Int,
            title: String,
            message: String,
            autoCancel: Boolean = true
        ) {
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.notify(
                id,
                createNotification(context, title, message, autoCancel)
            )
        }

        fun createNotification(
            context: Context,
            title: String,
            message: String,
            autoCancel: Boolean = true,
            isService: Boolean = false
        ): Notification {
            return Notification.Builder(
                context,
                if (isService) SERVICE_CHANNEL_ID else REGULAR_CHANNEL_ID
            )
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
                .setAutoCancel(autoCancel)
                .build()
        }
    }
}