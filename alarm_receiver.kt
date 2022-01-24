package com.example.pantryapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.util.Log




class alarm_receiver: BroadcastReceiver() {
    companion object {
        const val NOTIFICATION_ID = 101
        const val NOTIFICATION_CHANNEL_ID = "1000"
    }

    override fun onReceive(context: Context, intent: Intent) {
        createNotificationChannel(context)
        notifyNotification(context, "onion")
    }

    private fun createNotificationChannel(context: Context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Notification title",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            NotificationManagerCompat.from(context).createNotificationChannel(notificationChannel)
        }
    }


    private fun notifyNotification(context: Context, name: String) {
        val title = "items are about to expire"
        val descritpion = "your " + name + "s are about to expire"
        with(NotificationManagerCompat.from(context)) {
            val build = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(descritpion)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            notify(NOTIFICATION_ID, build.build())

        }

    }

}