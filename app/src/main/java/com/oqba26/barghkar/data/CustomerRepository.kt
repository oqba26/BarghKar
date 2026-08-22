package com.oqba26.barghkar.data

import com.oqba26.barghkar.data.local.dao.CustomerDao
import com.oqba26.barghkar.data.local.entity.CustomerEntity
import com.oqba26.barghkar.data.local.entity.ProjectEntity
import kotlinx.coroutines.flow.Flow

class CustomerRepository(private val customerDao: CustomerDao) {
    val allCustomers: Flow<List<CustomerEntity>> = customerDao.getAllCustomers()

    suspend fun insertCustomer(customer: CustomerEntity): Long = customerDao.insertCustomer(customer)
    suspend fun deleteCustomer(customer: CustomerEntity) = customerDao.deleteCustomer(customer)
    suspend fun getCustomerById(id: Long) = customerDao.getCustomerById(id)
    fun getProjectsForCustomer(customerId: Long): Flow<List<ProjectEntity>> = 
        customerDao.getProjectsForCustomer(customerId)
}
