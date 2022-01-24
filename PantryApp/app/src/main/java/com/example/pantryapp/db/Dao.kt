package com.example.pantryapp.db
import androidx.lifecycle.LiveData
import androidx.room.*

// define a data class that holds all relevant backend info for items held in pantry
data class ItemInfo(
    @ColumnInfo(name = "item_type") val item_type: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "quantity") val quantity: Int,
    @ColumnInfo(name = "user_exp") val user_exp: String?,
    @ColumnInfo(name = "default_exp") val default_exp: String
)

// the same as above but we don't care about expiration dates here
data class ShoppingInfo(
    @ColumnInfo(name = "item_type") val item_type: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "quantity") val quantity: Int,
)

// the item dao should not have an insert or delete; this is hardcoded
@Dao
interface ItemDao {
    // Retreive string list of all item categories associated with items in the item table.
    @Query("SELECT DISTINCT i.item_type " +
            "FROM item i")
    fun getAllCategories(): List<String>
    // Get the item id (primary key) associated with a specific item in the table.
    @Query("SELECT i.item_id " +
            "FROM item i WHERE name=:name")
    fun getItemId(name: String): Int
    // Get Item Entity of the item with given name.
    @Query("SELECT DISTINCT * FROM item WHERE name=:name")
    fun getItem(name: String): Entities.Item
    // Insert an item entity into the table.
    @Insert
    fun insertItems(items: List<Entities.Item>)
    // Check if an item exists in the item table with given name.
    @Query("SELECT EXISTS(SELECT DISTINCT * FROM item WHERE name=:name)")
    fun getExistence(name: String): Int
    // Get all item category strings associated with a given category sub-string.
    @Query("SELECT DISTINCT i.item_type " +
            "FROM item i WHERE i.item_type LIKE :category")
    fun getSpecificCategory(category: String): List<String>
    // Get all item category strings associated with a given name sub-string.
    @Query("SELECT DISTINCT i.item_type " +
            "FROM item i WHERE name LIKE :name")
    fun getCategoryFromName(name: String): List<String>
    // Update item in table iwth new expiration period.
    @Query("UPDATE item " +
            "SET expiration_date=:expire_per " +
            "WHERE name=:name")
    fun modifyExpire(expire_per: Int, name: String)
    // Reset/remove all items in the item table. (for development purposes)
    @Query("DELETE " + "FROM item")
    fun resetItem(): Void
    // Delete a specific item from the item table given the item name.
    @Query("DELETE " +
            "FROM item " +
            "WHERE name=:name")
    fun removeItemFromName(name: String)

}

// SQL Interface for the Pantry Table
// Each Pantry item references an Item table item through primary ID.
// To get a full list of item we join these table as can be seen below.
@Dao
interface PantryDao {
    // Get a list of each pantry item in the form of the entity ItemInfo seen above.
    @Query("SELECT i.item_type, i.name, p.quantity, p.user_exp, p.default_exp " +
            "FROM pantry p JOIN item i USING(item_id)")
    fun getAllPantryItems(): List<ItemInfo>
    // Get a list of each pantry item in the the entity Pantry seen in Entities.kt
    @Query("SELECT * FROM pantry")
    fun getAll(): List<Entities.Pantry>
    // Get list of Item entities given the category string.
    @Query("SELECT i.item_id, i.item_type, i.name, i.expiration_date " +
            "FROM pantry p JOIN item i USING(item_id) " +
            "WHERE i.item_type=:category")
    fun getItemsFromCategory(category: String): List<Entities.Item>
    // Get list of Pantry ItemInfo entities given the name of the item.
    @Query("SELECT i.item_type, i.name, p.quantity, p.user_exp, p.default_exp " +
            "FROM pantry p JOIN item i USING(item_id) " +
            "WHERE i.name LIKE :name")
    fun getItemsFromName(name: String): List<ItemInfo>
    // Get list of Pantry ItemInfo entities given a date string.
    @Query("SELECT i.item_type, i.name, p.quantity, p.user_exp, p.default_exp " +
            "FROM pantry p JOIN item i USING(item_id) " +
            "WHERE p.user_exp=:date")
    fun getPantryItemsByDate(date: String): List<ItemInfo>
    // Get list of categories associated with items of the given name.
    @Query("SELECT i.item_type " +
            "FROM item i JOIN pantry p " +
            "WHERE i.name=:name")
    fun getCategoryFromName(name: String): List<String>
    // Get all names of item in the Pantry table.
    @Query("SELECT DISTINCT i.name " +
            "FROM pantry p JOIN item i USING(item_id)")
    fun getAllPantryItemNames(): List<String>
    // Get LiveData list of all names of items in the pantry list.
    // This serves to signal an observer that the Pantry table has changed.
    @Query("SELECT DISTINCT i.name " +
           "FROM pantry p join item i USING(item_id)")
    fun getAllLive(): LiveData<List<String>>
    // Insert new Pantry entity into the Pantry table.
    @Insert
    fun insertItems(items: List<Entities.Pantry>)
    // Get a specific Pantry ItemInfo entity given the item name.
    @Query("SELECT i.item_type, i.name, p.quantity, p.user_exp, p.default_exp "
                 + "FROM pantry p JOIN item i USING(item_id) WHERE i.name=:name")
    fun getPantryItem(name: String): ItemInfo
    // Return 0 or 1 depending on if a Pantry item with given name exists in the Pantry table.
    @Query("SELECT EXISTS(SELECT DISTINCT * FROM pantry JOIN item i USING(item_id) WHERE i.name=:name)")
    fun getExistence(name: String): Int
    // Remove all items from the pantry table (For development purposes)
    @Query("DELETE " + "FROM pantry")
    fun resetPantry(): Void
    @Query("SELECT p.quantity FROM pantry p JOIN item i WHERE i.name=:name")
    fun getQuantityFromName(name: String): Int
    @Query("UPDATE pantry " +
            "SET quantity=:numItems " +
            "WHERE item_id IN (SELECT i.item_id " +
            "FROM item i JOIN pantry p USING(item_id) " +
            "WHERE i.name=:name)")
    fun modifyQuantity(numItems: Int, name: String): Void
    // Update the user_exp field for a Pantry item with the given name.
    @Query("UPDATE pantry " +
                 "SET user_exp=:newExpire " +
                 "WHERE item_id IN (SELECT i.item_id " +
                 "FROM item i JOIN pantry p USING(item_id) " +
                 "WHERE i.name=:name)")
    fun modifyDate(newExpire: String, name: String): Void
    // Remove an Pantry item from the table with the given name.
    @Query("DELETE " +
            "FROM pantry " +
            "WHERE item_id IN (SELECT i.item_id " +
            "FROM item i JOIN pantry p USING(item_id) " +
            "WHERE i.name=:name)")
    fun deletePantryItemFromName(name: String): Void
}

// SQL Interface for the ShoppingList Table
// Each ShoppingList item references an Item table item through primary ID.
// To get a full list of items we join these table as can be seen below.
@Dao
interface ListDao {
    // Get list of all Shopping items as ShoppingInfo entities.
    @Query("SELECT i.item_type, i.name, sl.quantity " +
            "FROM item i JOIN shopping_list sl USING(item_id)")
    fun getAllShoppingListItems(): List<ShoppingInfo>
    @Query("SELECT i.item_type, i.name, sl.quantity " +
            "FROM item i JOIN shopping_list sl USING(item_id)" +
            "WHERE i.name LIKE :name")
    fun getSLItemsFromName(name: String): List<ShoppingInfo>
    // Get list of Shopping Items in as ShoppingList entities.
    @Query("SELECT * FROM shopping_list")
    fun getAll(): LiveData<List<Entities.ShoppingList>>
    // Insert a new ShoppingList entity into the shopping list table.
    @Insert
    fun insertItems(items: List<Entities.ShoppingList>)
    // Check that a shopping list item exists with given item name.
    @Query("SELECT EXISTS(SELECT DISTINCT * FROM shopping_list JOIN item i USING(item_id) WHERE i.name=:name)")
    fun getExistence(name: String): Int
    // Delete all items from the shopping table (for development purposes)
    @Query("DELETE " + "FROM shopping_list")
    fun resetShoppingList(): Void
    // Modify the quantity field for a shopping list item with given name.
    @Query("UPDATE shopping_list " +
            "SET quantity=:numItems " +
            "WHERE item_id IN (SELECT i.item_id " +
            "FROM item i JOIN shopping_list p USING(item_id) " +
            "WHERE i.name=:name)")
    fun modifyQuantity(numItems: Int, name: String): Void
    @Query("SELECT sl.quantity " +
            "FROM shopping_list sl JOIN item i USING(item_id) " +
            "WHERE i.name=:name")
    fun getQuantity(name: String): Int
    // Remove a specific item from the shopping table given the item name.
    @Query("DELETE " +
            "FROM shopping_list " +
            "WHERE item_id IN (SELECT i.item_id " +
            "FROM item i JOIN shopping_list sl USING(item_id)" +
            "WHERE i.name=:name)")
    fun removeListItemFromName(name: String)
}

// SQL Interface for the ShoppingList Table
// Holds persistant settings info set by the user.
@Dao
interface SettingsDao {
    // Get all settings from the table returning  Setting entity in list.
    @Query("SELECT * " +
            "FROM settings s")
    fun getSettings(): List<Entities.Settings>
    // Get the size of the settings table.
    @Query("SELECT COUNT(*) FROM settings")
    fun getTableSize(): Int
    // Insert a new setting entity into the table.
    @Insert
    fun insertItems(items: List<Entities.Settings>)
    // Update the settings table with new value for use_user_exp toggle.
    @Query("UPDATE settings SET use_user_exp=:toggle")
    fun toggleUserExp(toggle: Int): Void
    // Get the stored state of the use_user_exp toggle.
    @Query("SELECT s.use_user_exp FROM settings s")
    fun getToggleState(): Int
    // Set the stored value for user desires pre-notification time.
    @Query("UPDATE settings SET notify_time=:days")
    fun setNotifTime(days: Int): Void
    // Get the notification time from table.
    @Query("SELECT s.notify_time FROM settings s")
    fun getNotifTime(): Int
    // Set the autopopulate switch values stored in db to true.
    @Query("UPDATE settings SET auto_populate=1 WHERE auto_populate=0")
    fun setSwitchTrue(): Void
    // Set it to false.
    @Query("UPDATE settings SET auto_populate=0 WHERE auto_populate=1")
    fun setSwitchFalse()
    // Retreive the autopopulate switch stored value.
    @Query("SELECT s.auto_populate FROM settings s")
    fun getSwitchState(): Int
}

