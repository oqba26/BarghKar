package com.oqba26.barghkar.data.local.dao

import androidx.room.*
import com.oqba26.barghkar.data.local.entity.InventoryMaterialEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_materials")
    fun getAllInventory(): Flow<List<InventoryMaterialEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventory(material: InventoryMaterialEntity): Long

    @Update
    suspend fun updateInventory(material: InventoryMaterialEntity)

    @Delete
    suspend fun deleteInventory(material: InventoryMaterialEntity)

    @Query("SELECT * FROM inventory_materials WHERE id = :id")
    suspend fun getInventoryItemById(id: Long): InventoryMaterialEntity?

    @Query("SELECT * FROM inventory_materials WHERE remoteId = :remoteId")
    suspend fun getInventoryItemByRemoteId(remoteId: Long): InventoryMaterialEntity?

    @Query("SELECT * FROM inventory_materials WHERE name = :name AND userId = :userId LIMIT 1")
    suspend fun getInventoryItemByName(name: String, userId: String): InventoryMaterialEntity?

    @Query("SELECT * FROM inventory_materials WHERE isSynced = 0")
    suspend fun getUnsyncedInventory(): List<InventoryMaterialEntity>
}
