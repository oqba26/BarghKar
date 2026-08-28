package com.oqba26.barghkar.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.oqba26.barghkar.data.CustomerRepository
import com.oqba26.barghkar.data.local.AppDatabase
import com.oqba26.barghkar.data.local.entity.CustomerEntity
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
class CustomerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: CustomerRepository
    private val authRepository = AuthRepository()

    val allCustomers: StateFlow<List<CustomerEntity>>

    init {
        val customerDao = AppDatabase.getDatabase(application).customerDao()
        repository = CustomerRepository(customerDao)
        allCustomers = repository.allCustomers.stateIn(
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
                android.util.Log.d("CustomerViewModel", "Profile changed: ${profile?.role}, ownerId: $ownerId")
                if (ownerId.isNotEmpty()) {
                    repository.syncRemoteToLocal(ownerId)
                }
            }
        }
    }

    fun addCustomerRemote(name: String, phoneNumber: String, address: String) {
        InputValidators.validateCustomer(name, phoneNumber, address)?.let { throw IllegalArgumentException(it) }

        viewModelScope.launch {
            val profile = authRepository.userProfileFlow.first()
            val ownerId = if (profile?.role == com.oqba26.barghkar.data.model.UserRole.APPRENTICE) {
                profile.masterId ?: profile.id
            } else {
                profile?.id ?: ""
            }
            android.util.Log.d("CustomerViewModel", "Adding customer, ownerId: $ownerId")
            
            if (ownerId.isNotEmpty()) {
                repository.addCustomer(
                    CustomerEntity(
                        userId = ownerId,
                        name = name.trim(),
                        phoneNumber = phoneNumber.trim(),
                        address = address.trim(),
                    ),
                )
                SyncManager.triggerImmediateSync(getApplication())
            }
        }
    }

    fun deleteCustomerRemote(customerId: Long) {
        viewModelScope.launch {
            val customer = repository.getCustomerById(customerId)
            customer?.let { 
                repository.deleteCustomer(it)
                SyncManager.triggerImmediateSync(getApplication())
            }
        }
    }
}
