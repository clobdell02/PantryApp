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
/*
File Name: alarm_receiver,kt

File purpose: This file creates the notification channel and calls the notification.  We can
create the notification channel every time that the notificition is needed as andriod only uses
resources to create the notification channel the first time the channel is created, everyother time
the notification channel is created it is free

Authours: Joseph Cates

How this fits into the larger system: this file sends out the notifications
*/



class Notifications{
    //create the NOTIFICATION_ID and the NOTIFICATION_CHANNEL_ID, both of which are needed
    val NOTIFICATION_ID = 101
    val NOTIFICATION_CHANNEL_ID = "1000"


     fun Notify(context: Context, title:String, name: String){
         //create the notification channel
        createNotificationChannel(context)
         //create the actual notification
        notifyNotification(context,title,name)
         //log for debugging
         Log.d("Notifications","notified")

    }

    private fun createNotificationChannel(context: Context) {
        //check that the andriod api, the phone is using supports notification channels
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //create a varaible that holds all the data needed for the notification channel
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Notification title",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            //actually create the notification channel
            NotificationManagerCompat.from(context).createNotificationChannel(notificationChannel)
        }
    }


    private fun notifyNotification(context: Context, title:String, name: String) {
        //create the notification
        with(NotificationManagerCompat.from(context)) {
            val build = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                //set the title of the notification
                .setContentTitle(title)
                //set the body of the notification
                .setContentText(name)
                //set the icon of the notification
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                //set the PRIORITY of the notification
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            //build the notification
            notify(NOTIFICATION_ID, build.build())

        }

    }

}