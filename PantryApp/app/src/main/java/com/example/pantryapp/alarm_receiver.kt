package com.example.pantryapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.pantryapp.db.AppDB
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.pantryapp.db.Entities
import com.example.pantryapp.ui.gallery.GalleryFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
/*
File Name: alarm_receiver,kt

File purpose: This file contains the logic for what to do when the alarm manger goes off

Authours: Joseph Cates

How this fits into the larger system: if the alarm manager goes off and we have items that we
need to notify the user about, then we call the notification class, if we have a single item or
multiple items then the logic on what notification to create changes.  If the phone shuts off,
then we save the alarm that was saved.
*/

//we have to make sure that the andriod phone has API version 26 to use the LocalDate.now() function
@RequiresApi(Build.VERSION_CODES.O)

class alarm_receiver: BroadcastReceiver() {
    //When the alarm manager goes off this onReceive function is called
    override fun onReceive(context: Context?, intent: Intent) {
        // we first check to see if the given intent was from the pantryapp
        if (intent!!.action.equals("com.tester.pantryapp")) {
            //we first grab the date from the interval spinner
            var spinnerDate = intent.getStringExtra("date")
            //debugging logs
            val dateString:String = spinnerDate.toString()
            Log.d("alarm_reciver",dateString)

            /*we then set the value of daysInAdvance and daysInAdvanceString, the daysInAdvance is used to
            calculate the date that we check the database with to see if we have an items we need
            to notify the user about.  The daysInAdvanceString is used to add information to the
            notification
             */
            var daysInAdvance:Long = 1
            var daysInAdvanceString = "one day"
            val current_date = LocalDate.now()
            if(dateString == "Two Days"){
                daysInAdvanceString = "two days"
                daysInAdvance = 2
            }else if(dateString == "Five Days"){
                daysInAdvanceString = "five days"
                daysInAdvance = 5
            }else if(dateString == "One Week") {
                daysInAdvanceString = "one week"
                daysInAdvance = 7
            }else if(dateString == "Two Weeks") {
                daysInAdvanceString = "two weeks"
                daysInAdvance = 14
            }
            //here we actually calculate the expiration date we want to check for
            val expDate = current_date.plusDays(daysInAdvance).toString()
            //logic to create the database instance
            val db = AppDB.AppDatabase.getInstance(context!!)
            val co_scope = CoroutineScope(Dispatchers.IO)
            co_scope.launch {
                /*here we run the getPantryItemsByDate(expDate) which returns a list of all the
                items that have an expiration date of the date that is given
                 */
                val list = db.pantryDao().getPantryItemsByDate(expDate)
                /*here we see if the list has one or multiple items, as if it has
                multiple items then the notification we need to create needs to be different
                 */
                val size = list.size
                if (size == 1) {
                    //logs for debugging
                    val name = "list: " + list[0].name
                    Log.d("list with one item",name)
                    //create an instance of the notifcation class
                    val notifyMe = Notifications()
                    //create the body of the notification
                    val message = list[0].name + " will expire in " + daysInAdvanceString + "."
                    /*call the notify function, pass it a context, the first string is the
                    title of the notification, and the second string is the body of the notification
                    */
                    notifyMe.Notify(context!!, "An Item will expire soon",message)
                }
                else if(size  >  1) {
                    /*here we create a string that has every item name that is about to expire added
                        to the string*/
                    var itemNameStr = ""
                    for(i in 0..(size-1)){
                        itemNameStr = itemNameStr + list[i].name + " "
                    }

                    //debugging log
                    Log.d("list with multiple item", itemNameStr)
                    //create an instance of the notifcation class
                    val notifyMe = Notifications()
                    //create the body of the notification
                    val message = itemNameStr + "will all expire in " + daysInAdvanceString + "."
                    /*call the notify function, pass it a context, the first string is the
                  title of the notification, and the second string is the body of the notification
                  */
                    notifyMe.Notify(context!!, "Items are about to expire",message)
                }
                // Check to see if the user has enabled auto-population
                if (db.settingsDao().getSwitchState() == 1){
                    // Create a list of all items that have expiry periods set to the current day
                    val expired_list = db.pantryDao().getPantryItemsByDate(LocalDate.now().toString())

                    // Ensure that list is not empty
                    if (expired_list.size >= 1){
                        // Iterate through all the expired items
                        for (i in expired_list){

                            // Create and add expired items to the shopping list in the same quantity they currently have
                            val addMeToShopping : Entities.ShoppingList = Entities.ShoppingList(
                                item_id = db.itemDao().getItemId(i.name),
                                quantity = db.pantryDao().getQuantityFromName(i.name)
                            )

                            val shopping_list = ArrayList<Entities.ShoppingList>()
                            shopping_list.add(addMeToShopping)

                            db.listDao().insertItems(shopping_list.toList())

                            db.pantryDao().modifyQuantity(0, i.name)

                        }
                    }
                }
            }
        }
        /*if the phone is shut off we save the alarm data*/
        else if(intent!!.action.equals("android.intent.action.BOOT_COMPLETED")){

            val saveData= saveData(context!!)
            saveData.setAlarm()
        }
    }
}