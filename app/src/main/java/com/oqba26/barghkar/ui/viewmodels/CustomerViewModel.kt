package com.oqba26.barghkar.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.oqba26.barghkar.data.CustomerRepository
import com.oqba26.barghkar.data.local.AppDatabase
import com.oqba26.barghkar.data.local.entity.CustomerEntity
import com.oqba26.barghkar.data.local.entity.ProjectEntity
import com.oqba26.barghkar.security.InputValidators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CustomerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: CustomerRepository
    val allCustomers: StateFlow<List<CustomerEntity>>

    private val _customers = MutableStateFlow<List<CustomerEntity>>(emptyList())

    init {
        val customerDao = AppDatabase.getDatabase(application).customerDao()
        repository = CustomerRepository(customerDao)
        allCustomers = _customers

        viewModelScope.launch {
            repository.allCustomers.collectLatest {
                _customers.value = it
            }
        }
    }

    fun addCustomer(name: String, phoneNumber: String, address: String) {
        val validationError = InputValidators.validateCustomer(name, phoneNumber, address)
        if (validationError != null) {
            throw IllegalArgumentException(validationError)
        }

        viewModelScope.launch {
            repository.insertCustomer(
                CustomerEntity(
                    name = name.trim(),
                    phoneNumber = phoneNumber.trim(),
                    address = address.trim()
                )
            )
        }
    }

    fun deleteCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
        }
    }

    fun getProjectsForCustomer(customerId: Long): StateFlow<List<ProjectEntity>> {
        val projects = MutableStateFlow<List<ProjectEntity>>(emptyList())
        viewModelScope.launch {
            repository.getProjectsForCustomer(customerId).collectLatest {
                projects.value = it
            }
        }
        return projects
    }
}
