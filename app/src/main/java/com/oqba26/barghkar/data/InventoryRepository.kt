package com.oqba26.barghkar.data

import android.util.Log
import com.oqba26.barghkar.data.local.dao.InventoryDao
import com.oqba26.barghkar.data.local.entity.InventoryMaterialEntity
import com.oqba26.barghkar.data.model.InventoryRemote
import com.oqba26.barghkar.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow

class InventoryRepository(private val inventoryDao: InventoryDao) {
    private val postgrest get() = SupabaseClient.client.postgrest

    val allInventory: Flow<List<InventoryMaterialEntity>> = inventoryDao.getAllInventory()

    suspend fun syncRemoteToLocal(ownerId: String) {
        try {
            val remoteInventory = postgrest["inventory_materials"].select {
                filter { eq("user_id", ownerId) }
            }.decodeList<InventoryRemote>()
            
            remoteInventory.forEach { remoteItem ->
                val remoteId = remoteItem.id ?: return@forEach
                val localItem = inventoryDao.getInventoryItemByRemoteId(remoteId)
                
                val entity = InventoryMaterialEntity(
                    userId = remoteItem.userId,
                    name = remoteItem.name,
                    quantity = remoteItem.quantity,
                    unit = remoteItem.unit,
                    remoteId = remoteId,
                    isSynced = true
                )

                if (localItem == null) {
                    inventoryDao.insertInventory(entity)
                } else if (localItem.name != entity.name || localItem.quantity != entity.quantity || localItem.unit != entity.unit) {
                    inventoryDao.updateInventory(entity.copy(id = localItem.id))
                }
            }
        } catch (e: Exception) {
            Log.e("InventoryRepository", "Error syncing inventory", e)
        }
    }

    suspend fun addInventoryItem(item: InventoryMaterialEntity): Long {
        return inventoryDao.insertInventory(item.copy(isSynced = false))
    }

    suspend fun deleteInventoryItem(item: InventoryMaterialEntity) {
        inventoryDao.deleteInventory(item)
    }

    suspend fun getInventoryItemById(id: Long) = inventoryDao.getInventoryItemById(id)
}
