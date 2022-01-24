package com.example.pantryapp.db
import androidx.room.*

// SQL Entity classes for storing/retreiving from the database.
class Entities {

    // Item table stores item profiles with type, name, and expiration period.
    @Entity(tableName = "item")
    data class Item(
        @PrimaryKey(autoGenerate = true) val item_id: Int = 0,
        @ColumnInfo(name = "item_type") val item_type: String,
        @ColumnInfo(name = "name") val name: String,
        @ColumnInfo(name = "expiration_date") val expiration_date: Int
    )

    // Pantry table class stores pantry items with reference to a specific item id from item table.
    @Entity(tableName = "pantry")
    data class Pantry(
        @PrimaryKey(autoGenerate = true) val p_id: Int = 0,
        @ColumnInfo(name = "item_id") val item_id: Int,
        @ColumnInfo(name = "quantity") val quantity: Int,
        @ColumnInfo(name = "user_exp") val user_exp: String?,
        @ColumnInfo(name = "default_exp") val default_exp: String
    )

    // Shopping list table class stores shopping items with reference to a specific item id from item table.
    @Entity(tableName = "shopping_list")
    data class ShoppingList(
        @PrimaryKey(autoGenerate = true) val sl_id: Int = 0,
        @ColumnInfo(name = "item_id") val item_id: Int,
        @ColumnInfo(name = "quantity") val quantity: Int
    )

    // Setting table class stores settings choices by user.
    @Entity(tableName = "settings")
    data class Settings(
        @PrimaryKey(autoGenerate = true) val s_id: Int = 0,
        @ColumnInfo(name = "use_user_exp") val use_user_exp: Int = 0,
        @ColumnInfo(name = "notify_time") val notify_time: Int,
        @ColumnInfo(name = "auto_populate") val auto_populate: Int
    )
}
