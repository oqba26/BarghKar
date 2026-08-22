package com.oqba26.barghkar.data

import com.oqba26.barghkar.data.local.dao.InventoryDao
import com.oqba26.barghkar.data.local.entity.InventoryMaterialEntity
import kotlinx.coroutines.flow.Flow

class InventoryRepository(private val inventoryDao: InventoryDao) {
    val allInventory: Flow<List<InventoryMaterialEntity>> = inventoryDao.getAllInventory()

    suspend fun insertInventory(material: InventoryMaterialEntity) = inventoryDao.insertInventory(material)
    suspend fun updateInventory(material: InventoryMaterialEntity) = inventoryDao.updateInventory(material)
    suspend fun deleteInventory(material: InventoryMaterialEntity) = inventoryDao.deleteInventory(material)
}
