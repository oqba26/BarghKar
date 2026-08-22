package com.oqba26.barghkar.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.oqba26.barghkar.data.local.dao.CustomerDao
import com.oqba26.barghkar.data.local.dao.InventoryDao
import com.oqba26.barghkar.data.local.dao.ProjectDao
import com.oqba26.barghkar.data.local.entity.*

@Database(
    entities = [
        ProjectEntity::class,
        MaterialEntity::class,
        CustomerEntity::class,
        InstallmentEntity::class,
        InventoryMaterialEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun customerDao(): CustomerDao
    abstract fun inventoryDao(): InventoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "barghkar_database",
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
