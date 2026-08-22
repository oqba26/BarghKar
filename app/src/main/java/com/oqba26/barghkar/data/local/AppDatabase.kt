package com.oqba26.barghkar.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add columns to projects
                db.execSQL("ALTER TABLE projects ADD COLUMN customerId INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE projects ADD COLUMN totalWage INTEGER NOT NULL DEFAULT 0")

                // Add column to materials
                db.execSQL("ALTER TABLE materials ADD COLUMN pricePerUnit INTEGER NOT NULL DEFAULT 0")

                // Create customers table
                db.execSQL("CREATE TABLE IF NOT EXISTS `customers` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `phoneNumber` TEXT NOT NULL, `address` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)")

                // Create installments table
                db.execSQL("CREATE TABLE IF NOT EXISTS `installments` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `projectId` INTEGER NOT NULL, `amount` INTEGER NOT NULL, `dueDate` INTEGER NOT NULL, `isPaid` INTEGER NOT NULL, FOREIGN KEY(`projectId`) REFERENCES `projects`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")

                // Create inventory_materials table
                db.execSQL("CREATE TABLE IF NOT EXISTS `inventory_materials` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `quantity` REAL NOT NULL, `unit` TEXT NOT NULL)")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "barghkar_database",
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
