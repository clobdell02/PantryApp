package com.example.pantryapp

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.annotation.RequiresApi
import com.example.pantryapp.db.AppDB
import com.example.pantryapp.db.Entities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

// Activity that is pulled up when a user tries to enter an item
// into the shopping list that does not have an item profile
class activity_additem_itemstable : AppCompatActivity() {
    // Check that API 26 or greater is in use.
    @RequiresApi(Build.VERSION_CODES.O)
    // Called on application creation in the android lifecycle.
    override fun onCreate(savedInstanceState: Bundle?) {
        // Call this to reset the database on app start.
        super.onCreate(savedInstanceState)
        // Set the layout to the associated xml file
        setContentView(R.layout.activity_additem_itemstable)

        // Create an instance of the database
        val db = AppDB.AppDatabase.getInstance(application)
        // Scope of the Coroutine. Default is the IO background scope.
        val co_scope = CoroutineScope(Dispatchers.IO)

        // Access the name, quantity, and expiry period entries
        val name = findViewById<EditText>(R.id.new_product_field)
        val quantity = findViewById<EditText>(R.id.new_quantity_field)
        val date = findViewById<EditText>(R.id.new_expiration_date_field)

        // Instantiate the spinner that stores the item categories
        val categoryspinner4 = findViewById<Spinner>(R.id.categoryspinner4)

        // Create an array adapter to display elements inside the spinner
        ArrayAdapter.createFromResource(
            // Reference the current activity as context
            this,
            // Pull information from the category array in values.xml
            R.array.category_array,
            // Set the layout of items in the spinner
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Structure the items to populate into the drop down menu and set the adapter.
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categoryspinner4.adapter = adapter
        }

        // Instantiate the button that adds a items into the item table and then takes the user
        // back to the Shopping List
        val add_button = findViewById<Button>(R.id.backtoshopping1)
        // Set a listener to look for a button click
        add_button.setOnClickListener()
        {

            // Get the expiration period from the input
            val expo_period = date.text.toString().toInt()
            co_scope.launch {
                withContext(Dispatchers.IO) {

                    // Convert the name into the format Xxxx in order to maintain consistency
                    val name_lower = name.text.toString().lowercase()
                    val name_proper = name_lower.replaceFirstChar { it.uppercase() }

                    // Create and add the item into the items table
                    val item : Entities.Item = Entities.Item(
                        item_type = categoryspinner4.selectedItem.toString(), name = name_proper, expiration_date = expo_period
                    )

                    val item_list = ArrayList<Entities.Item>()
                    item_list.add(item)

                    db.itemDao().insertItems(item_list.toList())

                    // Create and add the item into the shopping list
                    val entry: Entities.ShoppingList = Entities.ShoppingList(
                        item_id = db.itemDao().getItemId(name_proper),
                        quantity = quantity.text.toString().toInt()
                    )

                    val shopping_list = ArrayList<Entities.ShoppingList>()
                    shopping_list.add(entry)

                    db.listDao().insertItems(shopping_list.toList())
                }
            }
            // Terminate the activity and return to the Shopping List
            finish()
        }
    }
}