package com.oqba26.barghkar.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.oqba26.barghkar.data.InventoryRepository
import com.oqba26.barghkar.data.local.AppDatabase
import com.oqba26.barghkar.data.local.entity.InventoryMaterialEntity
import com.oqba26.barghkar.security.InputValidators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class InventoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: InventoryRepository
    val allInventory: StateFlow<List<InventoryMaterialEntity>>

    private val _inventory = MutableStateFlow<List<InventoryMaterialEntity>>(emptyList())

    init {
        val inventoryDao = AppDatabase.getDatabase(application).inventoryDao()
        repository = InventoryRepository(inventoryDao)
        allInventory = _inventory

        viewModelScope.launch {
            repository.allInventory.collectLatest {
                _inventory.value = it
            }
        }
    }

    fun addInventoryItem(name: String, quantity: Double, unit: String) {
        val validationError = InputValidators.validateInventory(name, quantity, unit)
        if (validationError != null) {
            throw IllegalArgumentException(validationError)
        }

        viewModelScope.launch {
            repository.insertInventory(
                InventoryMaterialEntity(
                    name = name.trim(),
                    quantity = quantity,
                    unit = unit.trim()
                )
            )
        }
    }

    fun updateInventoryItem(item: InventoryMaterialEntity) {
        viewModelScope.launch {
            repository.updateInventory(item)
        }
    }

    fun deleteInventoryItem(item: InventoryMaterialEntity) {
        viewModelScope.launch {
            repository.deleteInventory(item)
        }
    }
}
