package com.example.pantryapp

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.example.pantryapp.db.AppDB
import com.example.pantryapp.db.Entities
import com.example.pantryapp.ui.home.HomeFragment
import com.example.pantryapp.ui.home.HomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.time.LocalDate

//Activity that is pulled up when a user wants to add an item to the Pantry List
class activity_additem_pantry : AppCompatActivity() {
    // Check that API 26 or greater is in use.
    @RequiresApi(Build.VERSION_CODES.O)

    // Function that calculates an expiration date by adding the expiry period
    // to the current date.
    fun calculate_exp_date(expo_period : Int): String {

        val current_date = LocalDate.now()
        val expo_date = current_date.plusDays(expo_period.toLong()).toString()

        return expo_date
    }

    // Check that API 26 or greater is in use.
    @RequiresApi(Build.VERSION_CODES.O)
    // Called on application creation in the android lifecycle.
    override fun onCreate(savedInstanceState: Bundle?) {
        // Call this to reset the database on app start.
        super.onCreate(savedInstanceState)
        // Set the layout to the associated xml file
        setContentView(R.layout.activity_additem_pantry)

        // Create an instance of the database
        val db = AppDB.AppDatabase.getInstance(application)
        // Scope of the Coroutine. Default is the IO background scope.
        val co_scope = CoroutineScope(Dispatchers.IO)

        // Access the name, quantity, and expiry period entries
        val name = findViewById<EditText>(R.id.product_field)
        val quantity = findViewById<EditText>(R.id.quantity_field)
        val date = findViewById<EditText>(R.id.expiration_date_field)

        // Instantiate the spinner that stores the item categories
        val categoryspinner2 = findViewById<Spinner>(R.id.categoryspinner2)

        // Create an Array Adapter to display items in the spinner
        ArrayAdapter.createFromResource(
            // Reference the current activity as context
            this,
            // Pull information from the category array in values.xml
            R.array.category_array,
            // Set the layout of items in the spinner
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Structure items to populate into the drop down menu and set the adapter.
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categoryspinner2.adapter = adapter
        }

        // Instantiate the button that adds items into the Pantry table and then takes the user
        // back to the Pantry List
        val add_button = findViewById<Button>(R.id.backtopantry)
        add_button.setOnClickListener()
        {
            co_scope.launch {
                withContext(Dispatchers.IO) {
                    // Check to ensure none of the entry fields are empty
                    if(name.text.toString() != "" && date.text.toString() != "" && quantity.text.toString() != "") {

                        // Get the expiration period from the input
                        val expo_period = date.text.toString().toInt()

                        // Convert the name into the format Xxxx in order to maintain consistency
                        val name_lower = name.text.toString().lowercase()
                        val name_proper = name_lower.replaceFirstChar { it.uppercase() }

                        // Debug logs. No impact on functionality
                        Log.d("name_input", name.text.toString())
                        Log.d("name_lower", name_lower)
                        Log.d("name_proper", name_proper)

                        // Compute a date from the expiration period use the calculate_exp_date funciton
                        val expo_date = calculate_exp_date(expo_period)
                        // Check to see if the item does not exist in the item table.
                        if (db.itemDao().getExistence(name_proper) == 0) {

                            // If it does not, then create and add the item profile to the item table
                            val item: Entities.Item = Entities.Item(
                                item_type = categoryspinner2.selectedItem.toString(),
                                name = name_proper,
                                expiration_date = expo_period
                            )

                            val item_list = ArrayList<Entities.Item>()
                            item_list.add(item)

                            db.itemDao().insertItems(item_list.toList())
                        }

                        // Check to see if the item does not exist in the Pantry table
                        if (db.pantryDao().getExistence(name_proper) == 0) {

                            // If it does not, the create and add the item into the pantry table
                            val entry: Entities.Pantry = Entities.Pantry(
                                item_id = db.itemDao().getItemId(name_proper),
                                quantity = quantity.text.toString().toInt(),
                                user_exp = expo_date,
                                default_exp = "01/01/2030"
                            )

                            val pantry_list = ArrayList<Entities.Pantry>()
                            pantry_list.add(entry)

                            db.pantryDao().insertItems(pantry_list.toList())
                        }
                    }
                }
            }
            // Terminate the activity and return the user to the Pantry List
            finish()
        }
    }
}