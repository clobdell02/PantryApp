package com.example.pantryapp.ui

import android.content.Context
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.LayoutRes
import com.example.pantryapp.R
import com.example.pantryapp.db.ItemInfo
import com.example.pantryapp.db.ShoppingInfo
import android.content.DialogInterface
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.example.pantryapp.db.AppDB
import com.example.pantryapp.db.Entities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Integer.parseInt
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

// Custom implementation of Expandable List Adapter.
// Expects input of HashTable<String, ArrayList<String>>
// Each food category is a key in the hash table and points to a list of items in the category.
// For use with the Pantry List, has different functionality from the Shopping List.
// Overrides basic functionality of adapter, implementing custom views and logic.
class CustomExpandAdapter(
    context: Context, map: HashMap<String, ArrayList<ItemInfo>>)
    : BaseExpandableListAdapter() {
    // Context for execution and assign map to local variable.
    private val _context = context
    private val catagories: HashMap<String, ArrayList<ItemInfo>> = map

    // Instantiate database instance.
    private val db = AppDB.AppDatabase.getInstance(_context)
    // Scope of the Coroutine. Default is the IO background scope.
    private val co_scope = CoroutineScope(Dispatchers.IO)

    // Get list of food categories from the keys of the HashMap
    private val titles: ArrayList<String> = ArrayList(catagories.keys.toList().sortedBy { it })

    // Calculate expiration date given expiration period.
    // Requires api 26 and above.
    @RequiresApi(Build.VERSION_CODES.O)
    fun calculate_exp_date(expo_period : Int): String {
        val current_date = LocalDate.now()
        val expo_date = current_date.plusDays(expo_period.toLong()).toString()
        return expo_date
    }

    // Adapter override function gets item associated with child list.
    override fun getChild(position: Int, exp_position: Int): ItemInfo {
        return this.catagories.get(this.titles.get(position))!!.get(exp_position)
    }

    // Get id of item in child list.
    override fun getChildId(position: Int, exp_position: Int): Long {
        return exp_position.toLong()
    }

    // Get view (build view) for child of list. This is a Item within a category.
    @RequiresApi(Build.VERSION_CODES.O)
    override fun getChildView(
        position: Int,
        exp_position: Int,
        is_last: Boolean,
        convertView: View?,
        parent: ViewGroup
    ): View {
        // Inflate list_item xml file.
        val item = convertView ?: LayoutInflater.from(_context)
            .inflate(R.layout.list_item, parent, false)
        // Get the data associated with current child of adapter.
        val item_info = getChild(position, exp_position)
        // Find food_label TextView in inflated xml item and set the text.
        val food_name: TextView = item.findViewById(R.id.food_label)
        food_name.setText(item_info.name)

        // Get TextView for storing item expiration date and set value.
        val expire_date: TextView = item.findViewById(R.id.expand_1_expire_date)
        expire_date.setText(item_info.user_exp)

        // Build update expire functionality attached to update_expire button.
        val update_expire: Button = item.findViewById(R.id.update_expire)
        // Dialog creates popup window where text is input.
        val build = AlertDialog.Builder(_context)
        build.setTitle("Update Expiration")
        // Inflate xml file for the dialog.
        val editView: View = LayoutInflater.from(_context).inflate(R.layout.expire_update_layout, parent, false)
        val update_input: EditText = editView.findViewById(R.id.update_text)
        // Set the inflated view as the dialog view.
        build.setView(editView)
        // Set functionality of click of Update button which is positive end of dialog.
        build.setPositiveButton("Update", DialogInterface.OnClickListener { dialog, which ->
            // Async call for db operations.
            co_scope.launch {
                withContext(Dispatchers.IO) {
                    // Call the modifyDate DAO SQL wrapper function, replacing date in db with
                    //   new calculated date based on user input expiration period.
                    // UI list auto updates when db is updated.
                    val ex_per = parseInt(update_input.text.toString())
                    db.itemDao().modifyExpire(ex_per, item_info.name)
                    Log.d("Item Expire", db.itemDao().getItem(item_info.name).expiration_date.toString())
                    db.pantryDao().modifyDate(calculate_exp_date(ex_per), item_info.name)
                }
            }
        })
        // On negative resolution of dialog cancel it.
        build.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
            dialog.cancel()
        })
        // Set displaying the dialog to a click listener on the update_expire button.
        update_expire.setOnClickListener() {
            build.show()
        }

        // Get item quantity display text field and set value.
        val quantity: TextView = item.findViewById((R.id.item_counter))
        quantity.setText(item_info.quantity.toString())
        // Get increment and decrement quantity buttons and set on click listeners.
        val add_quantity: ImageButton = item.findViewById(R.id.decrement_counter)
        add_quantity.setOnClickListener() {
            // Async db operations.
            co_scope.launch {
                withContext(Dispatchers.IO) {
                    // DAO SQL function modifies quantity of item in db.
                    db.pantryDao().modifyQuantity(item_info.quantity - 1, item_info.name)
                }
            }
        }
        val sub_quantity: ImageButton = item.findViewById(R.id.increment_counter)
        sub_quantity.setOnClickListener() {
            co_scope.launch {
                withContext(Dispatchers.IO) {
                    // DAO SQL function modifies quantity of item in db.
                    db.pantryDao().modifyQuantity(item_info.quantity + 1, item_info.name)
                }
            }
        }

        // Get the expanding view that appears when a list item is clicked and set the click listener.
        val hidden_view: LinearLayout = item.findViewById(R.id.item_expand_1)
        item.setOnClickListener() {
            // If visible, set invisible when clicked using transistion manager.
            if(hidden_view.visibility == View.VISIBLE) {
                TransitionManager.beginDelayedTransition(parent, AutoTransition())
                hidden_view.visibility = View.GONE
            } else {
                // Else set to visible.
                TransitionManager.beginDelayedTransition(parent, AutoTransition())
                hidden_view.visibility = View.VISIBLE
            }
        }
        // Default is invisible.
        hidden_view.visibility = View.GONE

        // Get the add to shopping list button and set click listener.
        val add_shopping: Button = item.findViewById(R.id.item_add_button)
        add_shopping.setOnClickListener() {
            // Async db operations.
            co_scope.launch {
                withContext(Dispatchers.IO) {
                    // Check that the item does not already exist in the db use SQL call.
                    if(db.listDao().getExistence(item_info.name) == 0) {
                        // Get the ItemTable id of the item and consruct new ShoppingList Item.
                        val item_p_id = db.itemDao().getItemId(item_info.name)
                        val item_s = Entities.ShoppingList(item_id = item_p_id, quantity = 1)
                        // Insert new item into Shopping Table.
                        db.listDao().insertItems(listOf(item_s))
                    }
                }
            }
        }

        // Get remove item button and set click listener.
        val remove: Button = item.findViewById(R.id.item_remove_button)
        remove.setOnClickListener() {
            // Async db operations.
            co_scope.launch {
                withContext(Dispatchers.IO) {
                    // DAO SQL function removes associated Item with given name from Pantry Table, updating UI.
                    db.pantryDao().deletePantryItemFromName(item_info.name)
                }
            }
        }
        // Return the generated list item from the adapter for UI display.
        return item
    }
    // Override function gets size of child list.
    override fun getChildrenCount(position: Int): Int {
        return this.catagories.get(this.titles.get(position))!!.size
    }
    // Override function gets current category group position.
    override fun getGroup(position: Int): String {
        return this.titles.get(position)
    }
    // Override function gets categories size.
    override fun getGroupCount(): Int {
        return this.titles.size
    }
    // Override function retreives the position of current category in data structure.
    override fun getGroupId(position: Int): Long {
        return position.toLong()
    }
    // Override function for getting view of the category or parent views.
    override fun getGroupView(position: Int, is_expanded: Boolean, convertView: View?, parent: ViewGroup): View {
        // Get category name string.
        val title: String = getGroup(position)
        // Inflate xml file for the category view.
        val item = convertView ?: LayoutInflater.from(parent.context)
            .inflate(R.layout.group_list_item, parent, false)
        // Set the name to the TextView in the group View.
        val food_name: TextView = item.findViewById(R.id.group_label)
        food_name.setText(title)
        // Return generated group view.
        return item
    }
    override fun hasStableIds(): Boolean {
        return false
    }
    override fun isChildSelectable(position: Int, exp_position: Int): Boolean {
        return true
    }
}

// Custom implementation of Expandable List Adapter.
// Expects input of HashTable<String, ArrayList<String>>
// Each food category is a key in the hash table and points to a list of items in the category.
// For use with the Shopping List UI. Has different functionality from the Pantry List.
// Overrides basic functionality of adapter, implementing custom views and logic.
// See comments in CustomExpandAdater for full detail.
class ShoppingExpandAdapter(
    context: Context, map: HashMap<String, ArrayList<ShoppingInfo>>)
    : BaseExpandableListAdapter() {

    // Context for execution and assign map to local variable.
    private val _context = context
    private val catagories: HashMap<String, ArrayList<ShoppingInfo>> = map
    // DB instance
    private val db = AppDB.AppDatabase.getInstance(_context)
    // Scope of the Coroutine. Default is the IO background scope.
    private val co_scope = CoroutineScope(Dispatchers.IO)
    // Get list of food categories from the keys of the HashMap
    private val titles: ArrayList<String> = ArrayList(catagories.keys.toList().sortedBy { it })

    // Date function requires api 26
    @RequiresApi(Build.VERSION_CODES.O)
    fun calculate_exp_date(expo_period : Int): String {
        val current_date = LocalDate.now()
        val expo_date = current_date.plusDays(expo_period.toLong()).toString()
        return expo_date
    }
    override fun getChild(position: Int, exp_position: Int): ShoppingInfo {
        return this.catagories.get(this.titles.get(position))!!.get(exp_position)
    }
    override fun getChildId(position: Int, exp_position: Int): Long {
        return exp_position.toLong()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun getChildView(
        position: Int,
        exp_position: Int,
        is_last: Boolean,
        convertView: View?,
        parent: ViewGroup
    ): View {
        // View for current item.
        val item = convertView ?: LayoutInflater.from(_context)
            .inflate(R.layout.shopping_list_item, parent, false)
        // Get current item info from HashMap.
        val item_info = getChild(position, exp_position)
        // Get food name and quantity views and set values.
        val food_name: TextView = item.findViewById(R.id.food_label)
        food_name.setText(item_info.name)
        val quantity: TextView = item.findViewById((R.id.item_counter))
        quantity.setText(item_info.quantity.toString())

        // Quantity increment and decrement buttons. See CustomExpandAdapter.
        val add_quantity: ImageButton = item.findViewById(R.id.decrement_counter)
        add_quantity.setOnClickListener() {
            co_scope.launch {
                withContext(Dispatchers.IO) {
                    db.listDao().modifyQuantity(item_info.quantity - 1, item_info.name)
                }
            }
        }
        val sub_quantity: ImageButton = item.findViewById(R.id.increment_counter)
        sub_quantity.setOnClickListener() {
            co_scope.launch {
                withContext(Dispatchers.IO) {
                    db.listDao().modifyQuantity(item_info.quantity + 1, item_info.name)
                }
            }
        }
        // Add to Pantry List Button. See CustomExpandAdapter.
        val add_pantry: Button = item.findViewById(R.id.item_add_button)
        add_pantry.setOnClickListener() {
            co_scope.launch {
                withContext(Dispatchers.IO) {
                    if(db.pantryDao().getExistence(item_info.name) == 0) {
                        // Get Item table id and create new Pantry Item Object, adding to the table.
                        val item_d = db.itemDao().getItem(item_info.name)
                        val exp = calculate_exp_date(item_d.expiration_date)
                        val item_s = Entities.Pantry(item_id = item_d.item_id,
                                                     quantity = db.listDao().getQuantity(item_info.name),
                                                     user_exp =exp,
                                                     default_exp = "01/01/2030" )
                        db.pantryDao().insertItems(listOf(item_s))
                    } else {
                        val pantry_item = db.pantryDao().getPantryItem(item_info.name)
                        val sl_quantity = db.listDao().getQuantity(item_info.name)
                        db.pantryDao().modifyQuantity(pantry_item.quantity+sl_quantity, item_info.name)
                    }
                }
            }
        }
        // Remove item from Shoppin List button.
        val remove: Button = item.findViewById(R.id.item_remove_button)
        remove.setOnClickListener() {
            co_scope.launch {
                withContext(Dispatchers.IO) {
                    db.listDao().removeListItemFromName(item_info.name)
                }
            }
        }
        // Hidden_view that contains all expanding buttons when list item is clicked.
        // Set visibility when list item is touched.
        val hidden_view: LinearLayout = item.findViewById(R.id.item_expand_1)
        item.setOnClickListener() {
            if(hidden_view.visibility == View.VISIBLE) {
                TransitionManager.beginDelayedTransition(item as ViewGroup?, AutoTransition())
                hidden_view.visibility = View.GONE
            } else {
                TransitionManager.beginDelayedTransition(item as ViewGroup?, AutoTransition())
                hidden_view.visibility = View.VISIBLE
            }
        }
        hidden_view.visibility = View.GONE
        // Return generated item.
        return item
    }
    override fun getChildrenCount(position: Int): Int {
        return this.catagories.get(this.titles.get(position))!!.size
    }
    override fun getGroup(position: Int): String {
        return this.titles.get(position)
    }
    override fun getGroupCount(): Int {
        return this.titles.size
    }
    override fun getGroupId(position: Int): Long {
        return position.toLong()
    }
    // Get category list item and generate its view. See CustomExpandAdapter.
    override fun getGroupView(position: Int, is_expanded: Boolean, convertView: View?, parent: ViewGroup): View {
        val title: String = getGroup(position)
        val item = convertView ?: LayoutInflater.from(parent.context)
            .inflate(R.layout.group_list_item, parent, false)
        val food_name: TextView = item.findViewById(R.id.group_label)
        food_name.setText(title)
        return item
    }
    override fun hasStableIds(): Boolean {
        return false
    }
    override fun isChildSelectable(position: Int, exp_position: Int): Boolean {
        return true
    }
}
