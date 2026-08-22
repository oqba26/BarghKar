package com.oqba26.barghkar.data.local.dao

import androidx.room.*
import com.oqba26.barghkar.data.local.entity.CustomerEntity
import com.oqba26.barghkar.data.local.entity.ProjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY createdAt DESC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Long): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)

    @Query("SELECT * FROM projects WHERE customerId = :customerId")
    fun getProjectsForCustomer(customerId: Long): Flow<List<ProjectEntity>>
}
