package com.example.pantryapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.pantryapp.databinding.ActivityMainBinding
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.example.pantryapp.db.AppDB
import com.example.pantryapp.db.Entities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList


/*
Pantry MainActivity class. Handles application loading and
navigation between the three main fragments: Pantry, Shopping, and Settings.
Holds the Menu for navigation and the App Bar
*/
class MainActivity : AppCompatActivity() {
    // App Bar Configuration layoud files(xml) kotlin binding.
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    // Check that API 26 or greater is in use.
    @RequiresApi(Build.VERSION_CODES.O)
    // Called on application creation in the android lifecycle.
    override fun onCreate(savedInstanceState: Bundle?) {
        // Call this to reset the database on app start.
        //this.deleteDatabase("app_db")
        super.onCreate(savedInstanceState)

        // Kotlin layout inflater gets layout associated with main activity.
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Set the action bar layout though the binding.
        setSupportActionBar(binding.appBarMain.toolbar)

        // Menu and navigation xml layout files.
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        // Android navigation controller is loaded as the navigation fragment from content_main.xml
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        // Menu navigation build, see activity_main_drawer.xml
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_slideshow, R.id.nav_gallery
            ), drawerLayout
        )
        // Final set up calls for navcontroller
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        // Call this to populate the db with default items.
        //populate_db(this)

        /*On start we create an instance of the saveData class, and use it to either save the
        given alarm time, or use it create the default alarm time of 12:00 PM
        */
        val saveDataobj = saveData(applicationContext)
        saveDataobj.setAlarm()

    }

    // Anrdroid overiide function for handling upward navigation through activity hierarchy.
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }



}

// Function requires api 26
// Calculate date as string given integer value from current date.
@RequiresApi(Build.VERSION_CODES.O)
fun calculate_exp_date(expo_period : Int): String {
    val current_date = LocalDate.now()
    val expo_date = current_date.plusDays(expo_period.toLong()).toString()
    return expo_date
}

// Populate db function fills db with default items.
@RequiresApi(Build.VERSION_CODES.O)
fun populate_db(context: Context) {
    // Create an instance of the database
    val db = AppDB.AppDatabase.getInstance(context)
    // Scope of the Coroutine. Default is the IO background scope.
    val co_scope = CoroutineScope(Dispatchers.IO)
    co_scope.launch {
        // Reset the db tables.
        withContext(Dispatchers.IO) {
            db.itemDao().resetItem()
            db.pantryDao().resetPantry()
        }
        withContext(Dispatchers.IO) {
            // Create and add Items to item table.
            val a: Entities.Item = Entities.Item(
                item_type = "Vegetable", name = "Onion", expiration_date = 14
            )
            val b: Entities.Item = Entities.Item(
                item_type = "Meat", name = "Chicken Breast", expiration_date = 5
            )
            val c: Entities.Item = Entities.Item(
                item_type = "Meat", name = "Ground Beef", expiration_date = 5
            )
            val d: Entities.Item = Entities.Item(
                item_type = "Vegetable", name = "Lettuce", expiration_date = 7
            )
            val e: Entities.Item = Entities.Item(
                item_type = "Fruit", name = "Strawberries", expiration_date = 7
            )
            val f: Entities.Item = Entities.Item(
                item_type = "Fruit", name = "Mango", expiration_date = 7
            )
            val g: Entities.Item = Entities.Item(
                item_type = "Grain", name = "Rice", expiration_date = 90
            )
            val h: Entities.Item = Entities.Item(
                item_type = "Dairy", name = "Milk", expiration_date = 10
            )
            val i: Entities.Item = Entities.Item(
                item_type = "Fruit", name = "Apple", expiration_date = 7
            )
            val j: Entities.Item = Entities.Item(
                item_type = "Misc", name = "Bread", expiration_date = 15
            )
            val item_list = ArrayList<Entities.Item>()
            item_list.addAll(Arrays.asList(a, b, c, d, e, f, g, h, i, j))
            // Add list of itemds to the item table.
            db.itemDao().insertItems(item_list.toList())

            // Create and add Pantry object to pantry table.
            val a_: Entities.Pantry = Entities.Pantry(
                item_id = db.itemDao().getItemId("Onion"),
                quantity = 10,
                user_exp = calculate_exp_date(14),
                default_exp = "01/01/2030"
            )
            val b_: Entities.Pantry = Entities.Pantry(
                item_id = db.itemDao().getItemId("Lettuce"),
                quantity = 4,
                user_exp = calculate_exp_date(5),
                default_exp = "01/01/2030"
            )
            val c_: Entities.Pantry = Entities.Pantry(
                item_id = db.itemDao().getItemId("Chicken Breast"),
                quantity = 6,
                user_exp = calculate_exp_date(5),
                default_exp = "01/01/2030"
            )
            val d_: Entities.Pantry = Entities.Pantry(
                item_id = db.itemDao().getItemId("Ground Beef"),
                quantity = 10,
                user_exp = calculate_exp_date(7),
                default_exp = "01/01/2030"
            )
            val e_: Entities.Pantry = Entities.Pantry(
                item_id = db.itemDao().getItemId("Strawberries"),
                quantity = 20,
                user_exp = calculate_exp_date(7),
                default_exp = "01/01/2030"
            )
            val f_: Entities.Pantry = Entities.Pantry(
                item_id = db.itemDao().getItemId("Mango"),
                quantity = 6,
                user_exp = calculate_exp_date(7),
                default_exp = "01/01/2030"
            )
            val g_: Entities.Pantry = Entities.Pantry(
                item_id = db.itemDao().getItemId("Rice"),
                quantity = 6,
                user_exp = calculate_exp_date(90),
                default_exp = "01/01/2030"
            )
            val h_: Entities.Pantry = Entities.Pantry(
                item_id = db.itemDao().getItemId("Milk"),
                quantity = 1,
                user_exp = calculate_exp_date(10),
                default_exp = "01/01/2030"
            )
            val pantry_list = ArrayList<Entities.Pantry>()
            pantry_list.addAll(Arrays.asList(a_, b_, c_, d_, e_, f_, g_, h_))
            // Add list of items to the pantry table.
            db.pantryDao().insertItems(pantry_list.toList())

            // Create ShoppingList items.
            val a__: Entities.ShoppingList = Entities.ShoppingList(
                item_id = db.itemDao().getItemId("Apple"),
                quantity = 6
            )
            val b__: Entities.ShoppingList = Entities.ShoppingList(
                item_id = db.itemDao().getItemId("Bread"),
                quantity = 2
            )
            val shop_list = ArrayList<Entities.ShoppingList>()
            shop_list.addAll(Arrays.asList(a__, b__))
            // Add items to the shopping table.
            db.listDao().insertItems(shop_list.toList())
        }
    }
}
