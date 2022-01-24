package com.example.pantryapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import com.example.pantryapp.db.AppDB
import com.example.pantryapp.db.Entities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList

// Activity that is pulled up when the user tries to enter an item to the Shopping List
class activity_additem_shopping : AppCompatActivity() {

    // Called on application creation in the android lifecycle.
    override fun onCreate(savedInstanceState: Bundle?) {
        // Call this to reset the database on app start
        super.onCreate(savedInstanceState)
        // Set the layout to the associated xml file
        setContentView(R.layout.activity_additem_shopping)

        // Create an instance of the database
        val db = AppDB.AppDatabase.getInstance(application)
        // Scope of the Coroutine. Default is the IO background scope.
        val co_scope = CoroutineScope(Dispatchers.IO)

        // Access the name and quantity entry fields.
        val name = findViewById<EditText>(R.id.shoppingitem_name)
        val quantity = findViewById<EditText>(R.id.shopping_quantity)

        // Instantiate the button that adds items into the Shopping table
        val add_button = findViewById<Button>(R.id.backtoshopping)
        // Set a listener to look for a button click
        add_button.setOnClickListener()
        {
            co_scope.launch {
                withContext(Dispatchers.IO) {

                    // Make sure none of the entry fields are empty
                    if(name.text.toString() != "" && quantity.text.toString() != "") {

                        // Convert the name into the format Xxxx in order to maintain consistency
                        val name_lower = name.text.toString().lowercase()
                        val name_proper = name_lower.replaceFirstChar { it.uppercase() }

                        // Check that the item does not already exist in the Shopping Table
                        if (db.listDao().getExistence(name_proper) == 0) {
                            // If not, check that the item does exist in the item table
                            if (db.itemDao().getExistence(name_proper) == 1) {

                                // If it does, create and add teh item to the Shopping list
                                val entry: Entities.ShoppingList = Entities.ShoppingList(
                                    item_id = db.itemDao().getItemId(name_proper),
                                    quantity = quantity.text.toString().toInt()
                                )

                                val shopping_list = ArrayList<Entities.ShoppingList>()
                                shopping_list.add(entry)

                                db.listDao().insertItems(shopping_list.toList())

                            } else {

                                // If the item is not in the item table, then create an intent to move
                                // to the activity_additem_itemstable activity
                                val intent = Intent(
                                    // Reference the current activity as context
                                    this@activity_additem_shopping,
                                    // Declare the activity to be opened
                                    activity_additem_itemstable::class.java
                                )
                                // Launch the activity
                                startActivity(intent)

                            }
                        }
                    }
                }
            }
            // Terminate the activity and return the user to the Shopping List
            finish()
        }
    }
}