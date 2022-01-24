package com.example.pantryapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import java.util.*
/*
File Name: saveData.kt

File purpose: This file contains the code for the alarm manager, and also saves all the data
neccessary for notifications.  This file save the time the user would like the notifications
to go out, and also saves how far in advance  the user wants to be notified of an item nearing its
expiaration.  For example the user might want to be notified when an item is 5 days or one week
away from its expiration date.

Authours: Joseph Cates

How this fits into the larger system: This file is need for the notification system to work.
In this file, we create the alarm manager, which will go off either at a user specified time, or the
default time of 12:00pm.  All of the user input data is also saved, so 24 hours later the alarm
manager will go off again at the same time, pinging alarm_receiver.kt to see if notifications
need to be sent out or not
*/

class saveData {
    /*here we have the constructor that saves the context for the class and creates the
    SharedPreferences instance
     */
    var context: Context?=null
    var sharedRef:SharedPreferences?=null
    constructor(context:Context){
        this.context=context
        sharedRef=context.getSharedPreferences("myref",Context.MODE_PRIVATE)
    }

    /*this function uses the SharedPreferences interface to save the given time that
    * the user would like the alarm to go off */
    fun SaveData(hour:Int,minute:Int){
        //debugging logs
        val string = "hour: " + hour + " min: " + minute
        Log.d("saveData",string)
        //creates a new editor which allows us to save the given data
        var editor=sharedRef!!.edit()
        //save and commit the given data
        editor.putInt("hour",hour)
        editor.putInt("minute",minute)
        editor.commit()
    }
    /*this function uses the SharedPreferences interface to how many days in advance the user wants
    the expiration notification to go off
     */
    fun saveDate(date:String) {
        //creates a new editor which allows us to save the given data
        var editor = sharedRef!!.edit()
        //save and commit the given data
        editor.putString("date", date)
        editor.commit()
    }

    //function to get the saved date
    fun getDate():String?{
        return sharedRef!!.getString("date","One Day")
    }

    //function to get the saved hour
    fun  getHour():Int{
        return sharedRef!!.getInt("hour",0)
    }

    //function to get the saved minute
    fun  getMinute():Int{
        return sharedRef!!.getInt("minute",0)
    }


    /*this function creates the alarm manager and tells the alarm manager what to do when the
    alarm goes off
    */
    fun setAlarm(){
        //grab all the saved data
        val hour:Int=getHour()
        val minute:Int=getMinute()
        val intervalString:String= getDate().toString()
        //log for debugging
        val string = "hour: " + hour + " min: " + minute
        Log.d("setAlarm",string)
        /*create a calendar instance that we will use to store the time the user wants
        the alarm to off*/
        val calender=Calendar.getInstance()
        calender.set(Calendar.HOUR_OF_DAY,hour)
        calender.set(Calendar.MINUTE,minute)
        calender.set(Calendar.SECOND,0)

        //create the alarm manager instance
        val am= context!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        /*create the intent for the alarm manager the intent is what the alarm will do when it
        goes off*/
        var intent=Intent(context,alarm_receiver::class.java)
        //store the intervalString as an extra part of the intent
        intent.putExtra("date",intervalString)
        /*set the action as the pantryapp, so our alarm_reciver class will only execute its
        logic for the pantry app alarm*/
        intent.action="com.tester.pantryapp"

        /*create the pending intent for the alarm manager*/
        val pi=PendingIntent.getBroadcast(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT)


        /*val msUntilTriggerHour: Long = 30000
        val msUntilTriggerHour2: Long = 60000
        debugging values, sets the alarm to go off 30secs after being called, and to repeat a minute
        later
        */

        /*create a repeating alarm that when it goes off will wake up the phone, will go off on the
        user specified time, will repeat a day later, and when it goes off will execute the pending
        intent, which will call the alarm_receiver class
         */
        am.setRepeating(AlarmManager.RTC_WAKEUP,calender.timeInMillis,AlarmManager.INTERVAL_DAY
            ,pi)



    }
}






