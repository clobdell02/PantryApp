package com.example.pantryapp.db
import androidx.room.*
import android.content.Context

// Room database class.
class AppDB {
    @Database(entities = [Entities.Item::class, Entities.Pantry::class,
                          Entities.ShoppingList::class, Entities.Settings::class], version = 1)
    abstract class AppDatabase : RoomDatabase() {
        // DAO functions represent each table interface. See Dao.kt.
        abstract fun itemDao(): ItemDao
        abstract fun pantryDao(): PantryDao
        abstract fun listDao(): ListDao
        abstract fun settingsDao(): SettingsDao

        // singleton design pattern, one DB but multiple pointers to the DB
        companion object {
            private var instance: AppDatabase? = null
            fun getInstance(context: Context): AppDatabase {
                // if there is no instance then create one
                if (instance == null) {
                    // prevent calls from more than one thread
                    synchronized(this) {
                        // TODO: get rid of the allowMainThreadQueries, it's for testing and bad practice
                        instance = Room.databaseBuilder(context, AppDatabase::class.java, "app_db").build()//.allowMainThreadQueries().build()
                    }
                }
                // return the instance, if it's null (it shouldn't be) throw an error
                return instance!!
            }
        }
    }
}