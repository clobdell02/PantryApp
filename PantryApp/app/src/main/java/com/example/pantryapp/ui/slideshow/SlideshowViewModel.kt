package com.example.pantryapp.ui.slideshow

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pantryapp.db.AppDB
import com.example.pantryapp.db.Entities
import com.example.pantryapp.db.ItemInfo
import com.example.pantryapp.db.ShoppingInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class SlideshowViewModel(application: Application) : AndroidViewModel(application) {


    // Create an instance of the database
    private val db = AppDB.AppDatabase.getInstance(application)
    // Scope of the Coroutine. Default is the IO background scope.
    private val co_scope = CoroutineScope(Dispatchers.IO)

    // Mutable live data that will hold the list HashMap
    private val _list = MutableLiveData<HashMap<String, ArrayList<ShoppingInfo>>>()
    // On initiation of the view model, fill the db item and pantry table with some values and set value of MutableLiveData.
    // This is a mess, mainly because the data is being put into the db in this function first.
    // To ensure that order is applied to the async calls, we use withContext() which forces
    //      the sequential execution of each withContext block in the current Coroutine scope.
    init {
        // Asynchronously launch the contained code.
        /*
        co_scope.launch {
            withContext(Dispatchers.IO) {
                // Create and add Items to item table.
                val a: Entities.Item = Entities.Item(
                    item_type = "Vegetable", name = "Lemon", expiration_date = 14
                )
                val b: Entities.Item = Entities.Item(
                    item_type = "Meat", name = "Steak", expiration_date = 5
                )

                val e: Entities.Item = Entities.Item(
                    item_type = "Fruit", name = "Blueberries", expiration_date = 7
                )

                val item_list = ArrayList<Entities.Item>()
                item_list.addAll(Arrays.asList(a, b, e))

                db.itemDao().insertItems(item_list.toList())

                // Create and add Pantry object to pantry table.
                val g: Entities.ShoppingList = Entities.ShoppingList(
                    item_id = db.itemDao().getItemId("Lemon"),
                    quantity = 10
                )
                val i: Entities.ShoppingList = Entities.ShoppingList(
                    item_id = db.itemDao().getItemId("Steak"),
                    quantity = 6
                )
                val k: Entities.ShoppingList = Entities.ShoppingList(
                    item_id = db.itemDao().getItemId("Blueberries"),
                    quantity = 20
                )
                val shopping_list = ArrayList<Entities.ShoppingList>()
                shopping_list.addAll(Arrays.asList(g, i, k))

                db.listDao().insertItems(shopping_list.toList())

                // Debugging logs to console calls.
                /*
                Log.d("Item ID: ", db.itemDao().getItemId("Onion").toString())
                Log.d("Items in Pantry #: ", db.pantryDao().getAll().size.toString())
                Log.d("pantry[0] item_id: ", db.pantryDao().getAll()[0].item_id.toString())
                */

            }
        */
        co_scope.launch{
            withContext(Dispatchers.IO){
                update_data()
            }
        }
    }

    var shoppingItems: LiveData<List<Entities.ShoppingList>> = db.listDao().getAll()
    // Set the live data variable to the mutable live data variable value..
    val foodList: LiveData<HashMap<String, ArrayList<ShoppingInfo>>> = _list
    // Update live data object.
    // Gets data from the DB, updating LiveData which is observed in HomeFragment. Observer notifies Fragment and UI is updated.
    fun update_data() {
        _list.apply {
            // Create HashMap that will be passed back to fragment and fed to ExpandableListAdapter
            val foodMap: HashMap<String, ArrayList<ShoppingInfo>> = HashMap<String,ArrayList<ShoppingInfo>>()
            // Launch async coroutine that suspends current thread.
            co_scope.launch {

                // Get list of categories List<String> and list of shopping list items List<ShoppingInfo>
                withContext(Dispatchers.IO) {
                    val categories = db.itemDao().getAllCategories()
                    val shopping = db.listDao().getAllShoppingListItems()
                    // Fill hashmap with Category -> List of items in category: <String, ArrayList<String>>
                    for (i in categories) {
                        val cat_list = ArrayList<ShoppingInfo>()
                        for (j in shopping) {
                            if (j.item_type == i) {
                                cat_list.add(j)
                            }
                        }
                        if(cat_list.size > 0) {
                            foodMap.put(i, cat_list)
                        }
                    }
                }
                // Asynchronously post value to livedata variable once above code has completed.
                withContext(Dispatchers.IO) {
                    postValue(foodMap)
                }
            }
        }
    }

    fun filter_update(sortString: String) {
        _list.apply {
            // Create HashMap that will be passed back to fragment and fed to ExpandableListAdapter
            val foodMap: HashMap<String, ArrayList<ShoppingInfo>> = HashMap<String,ArrayList<ShoppingInfo>>()
            // Launch async coroutine that suspends current thread.
            co_scope.launch {

                // Get list of categories List<String> and list of shopping list items List<ShoppingInfo>
                withContext(Dispatchers.IO) {
                    val searchString = sortString.plus("%")
                    val categories = db.itemDao().getCategoryFromName(searchString)
                    val shopping = db.listDao().getSLItemsFromName(searchString)
                    // Fill hashmap with Category -> List of items in category: <String, ArrayList<String>>
                    for (i in categories) {
                        val cat_list = ArrayList<ShoppingInfo>()
                        for (j in shopping) {
                            if (j.item_type == i) {
                                cat_list.add(j)
                            }
                        }
                        if(cat_list.size > 0) {
                            foodMap.put(i, cat_list)
                        }
                    }


                    val categories2 = db.itemDao().getSpecificCategory(searchString)
                    val shopping2 = db.listDao().getAllShoppingListItems()

                    // Fill hashmap with Category -> List of items in category: <String, ArrayList<String>>
                    for (i in categories2) {
                        val cat_list2 = ArrayList<ShoppingInfo>()
                        for (j in shopping2) {
                            if (j.item_type == i) {
                                cat_list2.add(j)
                            }
                        }
                        if(cat_list2.size > 0) {
                            foodMap.put(i, cat_list2)
                        }
                    }
                }
                // Asynchronously post value to livedata variable once above code has completed.
                withContext(Dispatchers.IO) {
                    postValue(foodMap)
                }
            }
        }
    }
}