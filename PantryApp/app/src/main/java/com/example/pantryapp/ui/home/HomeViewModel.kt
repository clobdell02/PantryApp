package com.example.pantryapp.ui.home

import android.app.Application
import android.content.Context
import com.example.pantryapp.db.*
import androidx.annotation.LayoutRes

import android.os.Handler
import android.util.Log
import androidx.lifecycle.*

import com.example.pantryapp.ui.home.HomeFragment
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.HashMap

/*
HomeViewModel handles data retrieval from the db and delivery to the HomeFragment
See update_data
    - Call this function to update pantry display list after updating db.
    - Also serves as a good example for how to access the db and get lists of categories or pantry/shopping list items.
*/

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    // Create an instance of the database
    private val db = AppDB.AppDatabase.getInstance(application)


    // Scope of the Coroutine. Default is the IO background scope.
    private val co_scope = CoroutineScope(Dispatchers.IO)

    // Mutable live data that will hold the list HashMap
    private val _list = MutableLiveData<HashMap<String,ArrayList<ItemInfo>>>()

    // Retreives DB LiveData list of Pantry name strings. LiveData is updated when the DB changes,
    //      which in turn notifies the observer in the HomeFragment.
    var pantryItems: LiveData<List<String>> = db.pantryDao().getAllLive()

    // Set the live data variable to the mutable live data variable value..
    val foodList: LiveData<HashMap<String, ArrayList<ItemInfo>>> = _list
    fun getFoodList(): HashMap<String, ArrayList<ItemInfo>> {
        return foodList.value!!
    }
    // Update live data object.
    // Gets data from the DB, updating LiveData which is observed in HomeFragment. Observer notifies Fragment and UI is updated.
    // To ensure that order is applied to the async calls, we use withContext() which forces
    //      the sequential execution of each withContext block in the current Coroutine scope.
    fun update_data() {
        _list.apply {
            // Create HashMap that will be passed back to fragment and fed to ExapandableListAdapter
            val foodMap: HashMap<String,ArrayList<ItemInfo>> = HashMap<String,ArrayList<ItemInfo>>()
            // Launch async coroutine that suspends current thread.
            co_scope.launch {

                // Get list of categories List<String> and list of pantry items List<ItemInfo>
                withContext(Dispatchers.IO) {
                    val categories = db.itemDao().getAllCategories()
                    val pantry = db.pantryDao().getAllPantryItems()
                    // Fill hashmap with Category -> List of items in category: <String, ArrayList<String>>
                    val sorted_categories = categories.sortedBy { it }
                    Log.d("This","Value")
                    for (i in sorted_categories) {
                        val cat_list = ArrayList<ItemInfo>()
                        for (j in pantry) {
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
            // Create HashMap that will be passed back to fragment and fed to ExapandableListAdapter
            val foodMap: HashMap<String,ArrayList<ItemInfo>> = HashMap<String,ArrayList<ItemInfo>>()
            // Launch async coroutine that suspends current thread.
            co_scope.launch {

                // Get list of categories List<String> and list of pantry items List<ItemInfo>
                withContext(Dispatchers.IO) {
                    val searchString = sortString.plus("%")
                    val categories = db.itemDao().getCategoryFromName(searchString)
                    val pantry = db.pantryDao().getItemsFromName(searchString)

                    // Fill hashmap with Category -> List of items in category: <String, ArrayList<String>>
                    val sorted_categories = categories.sortedBy { it }
                    Log.d("This","Value")
                    for (i in sorted_categories) {
                        val cat_list = ArrayList<ItemInfo>()
                        for (j in pantry) {
                            if (j.item_type == i) {
                                cat_list.add(j)//.name)
                            }
                        }
                        foodMap.put(i, cat_list)
                    }



                    val categories2 = db.itemDao().getSpecificCategory(searchString)
                    val pantry2 = db.pantryDao().getAllPantryItems()

                    // Fill hashmap with Category -> List of items in category: <String, ArrayList<String>>
                    val sorted_categories2 = categories2.sortedBy { it }
                    Log.d("This","Value")
                    for (i in sorted_categories2) {
                        val cat_list2 = ArrayList<ItemInfo>()
                        for (j in pantry2) {
                            if (j.item_type == i) {
                                cat_list2.add(j)//.name)
                            }
                        }
                        foodMap.put(i, cat_list2)
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
