package com.oqba26.barghkar.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import net.sqlcipher.database.SupportFactory
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
    version = 5,
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

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add sync columns to projects
                db.execSQL("ALTER TABLE projects ADD COLUMN remoteId TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE projects ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")

                // Add sync columns to customers
                db.execSQL("ALTER TABLE customers ADD COLUMN remoteId TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE customers ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")

                // Add sync columns to materials
                db.execSQL("ALTER TABLE materials ADD COLUMN remoteId TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE materials ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")

                // Add sync columns to installments
                db.execSQL("ALTER TABLE installments ADD COLUMN remoteId TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE installments ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")

                // Add sync columns to inventory_materials
                db.execSQL("ALTER TABLE inventory_materials ADD COLUMN remoteId TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE inventory_materials ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add status column to materials and installments
                db.execSQL("ALTER TABLE materials ADD COLUMN status TEXT NOT NULL DEFAULT 'APPROVED'")
                db.execSQL("ALTER TABLE installments ADD COLUMN status TEXT NOT NULL DEFAULT 'APPROVED'")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add new project detail columns
                db.execSQL("ALTER TABLE projects ADD COLUMN infrastructureArea REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE projects ADD COLUMN pricePerFixture INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE projects ADD COLUMN pricePerMeter INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE projects ADD COLUMN firstPayment INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE projects ADD COLUMN secondPayment INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE projects ADD COLUMN thirdPayment INTEGER NOT NULL DEFAULT 0")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val passphrase = DatabaseSecurity.getPassphrase(context)
                val factory = SupportFactory(passphrase)
                
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "barghkar_database",
                )
                    .openHelperFactory(factory)
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
