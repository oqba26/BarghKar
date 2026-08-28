package com.oqba26.barghkar.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.oqba26.barghkar.data.InventoryRepository
import com.oqba26.barghkar.data.local.AppDatabase
import com.oqba26.barghkar.data.local.entity.InventoryMaterialEntity
import com.oqba26.barghkar.security.InputValidators
import kotlinx.coroutines.flow.StateFlow
import com.oqba26.barghkar.data.remote.AuthRepository
import com.oqba26.barghkar.data.sync.SyncManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class InventoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: InventoryRepository
    private val authRepository = AuthRepository()
    
    val allInventory: StateFlow<List<InventoryMaterialEntity>>

    init {
        val inventoryDao = AppDatabase.getDatabase(application).inventoryDao()
        repository = InventoryRepository(inventoryDao)
        allInventory = repository.allInventory.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList(),
        )

        viewModelScope.launch {
            authRepository.userProfileFlow.collect { profile ->
                val ownerId = if (profile?.role == com.oqba26.barghkar.data.model.UserRole.APPRENTICE) {
                    profile.masterId ?: profile.id
                } else {
                    profile?.id ?: ""
                }
                if (ownerId.isNotEmpty()) {
                    repository.syncRemoteToLocal(ownerId)
                }
            }
        }
    }

    fun addInventoryItemRemote(name: String, quantity: Double, unit: String) {
        InputValidators.validateInventory(name, quantity, unit)?.let { throw IllegalArgumentException(it) }

        viewModelScope.launch {
            val profile = authRepository.userProfileFlow.first()
            val ownerId = if (profile?.role == com.oqba26.barghkar.data.model.UserRole.APPRENTICE) {
                profile.masterId ?: profile.id
            } else {
                profile?.id ?: ""
            }
            
            if (ownerId.isNotEmpty()) {
                repository.addInventoryItem(
                    InventoryMaterialEntity(
                        userId = ownerId,
                        name = name.trim(),
                        quantity = quantity,
                        unit = unit.trim(),
                    ),
                )
                SyncManager.triggerImmediateSync(getApplication())
            }
        }
    }

    fun deleteInventoryItemRemote(itemId: Long) {
        viewModelScope.launch {
            val item = repository.getInventoryItemById(itemId)
            item?.let { 
                repository.deleteInventoryItem(it)
                SyncManager.triggerImmediateSync(getApplication())
            }
        }
    }
}
