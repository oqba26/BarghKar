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
    version = 7,
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

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add userId column to all tables
                db.execSQL("ALTER TABLE customers ADD COLUMN userId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE projects ADD COLUMN userId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE materials ADD COLUMN userId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE installments ADD COLUMN userId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE inventory_materials ADD COLUMN userId TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. customers
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `customers_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `phoneNumber` TEXT NOT NULL,
                        `address` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `remoteId` INTEGER,
                        `isSynced` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO customers_new (id, userId, name, phoneNumber, address, createdAt, remoteId, isSynced)
                    SELECT id, userId, name, phoneNumber, address, createdAt, CAST(remoteId AS INTEGER), isSynced
                    FROM customers
                """.trimIndent())
                db.execSQL("DROP TABLE customers")
                db.execSQL("ALTER TABLE customers_new RENAME TO customers")

                // 2. projects
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `projects_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `customerId` INTEGER,
                        `totalWage` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `remoteId` INTEGER,
                        `isSynced` INTEGER NOT NULL,
                        `infrastructureArea` REAL NOT NULL,
                        `pricePerFixture` INTEGER NOT NULL,
                        `pricePerMeter` INTEGER NOT NULL,
                        `firstPayment` INTEGER NOT NULL,
                        `secondPayment` INTEGER NOT NULL,
                        `thirdPayment` INTEGER NOT NULL,
                        FOREIGN KEY(`customerId`) REFERENCES `customers`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO projects_new (id, userId, name, description, customerId, totalWage, createdAt, remoteId, isSynced, infrastructureArea, pricePerFixture, pricePerMeter, firstPayment, secondPayment, thirdPayment)
                    SELECT id, userId, name, description, customerId, totalWage, createdAt, CAST(remoteId AS INTEGER), isSynced, infrastructureArea, pricePerFixture, pricePerMeter, firstPayment, secondPayment, thirdPayment
                    FROM projects
                """.trimIndent())
                db.execSQL("DROP TABLE projects")
                db.execSQL("ALTER TABLE projects_new RENAME TO projects")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_projects_customerId` ON `projects` (`customerId`)")

                // 3. materials
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `materials_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `projectId` INTEGER NOT NULL,
                        `name` TEXT NOT NULL,
                        `quantity` INTEGER NOT NULL,
                        `unit` TEXT NOT NULL,
                        `pricePerUnit` INTEGER NOT NULL,
                        `remoteId` INTEGER,
                        `isSynced` INTEGER NOT NULL,
                        `status` TEXT NOT NULL,
                        FOREIGN KEY(`projectId`) REFERENCES `projects`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO materials_new (id, userId, projectId, name, quantity, unit, pricePerUnit, remoteId, isSynced, status)
                    SELECT id, userId, projectId, name, quantity, unit, pricePerUnit, CAST(remoteId AS INTEGER), isSynced, status
                    FROM materials
                """.trimIndent())
                db.execSQL("DROP TABLE materials")
                db.execSQL("ALTER TABLE materials_new RENAME TO materials")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_materials_projectId` ON `materials` (`projectId`)")

                // 4. installments
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `installments_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `projectId` INTEGER NOT NULL,
                        `amount` INTEGER NOT NULL,
                        `dueDate` INTEGER NOT NULL,
                        `isPaid` INTEGER NOT NULL,
                        `remoteId` INTEGER,
                        `isSynced` INTEGER NOT NULL,
                        `status` TEXT NOT NULL,
                        FOREIGN KEY(`projectId`) REFERENCES `projects`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO installments_new (id, userId, projectId, amount, dueDate, isPaid, remoteId, isSynced, status)
                    SELECT id, userId, projectId, amount, dueDate, isPaid, CAST(remoteId AS INTEGER), isSynced, status
                    FROM installments
                """.trimIndent())
                db.execSQL("DROP TABLE installments")
                db.execSQL("ALTER TABLE installments_new RENAME TO installments")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_installments_projectId` ON `installments` (`projectId`)")

                // 5. inventory_materials
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `inventory_materials_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `quantity` REAL NOT NULL,
                        `unit` TEXT NOT NULL,
                        `remoteId` INTEGER,
                        `isSynced` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO inventory_materials_new (id, userId, name, quantity, unit, remoteId, isSynced)
                    SELECT id, userId, name, quantity, unit, CAST(remoteId AS INTEGER), isSynced
                    FROM inventory_materials
                """.trimIndent())
                db.execSQL("DROP TABLE inventory_materials")
                db.execSQL("ALTER TABLE inventory_materials_new RENAME TO inventory_materials")
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
