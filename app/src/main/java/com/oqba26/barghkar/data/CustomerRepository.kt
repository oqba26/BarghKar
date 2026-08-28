package com.oqba26.barghkar.data

import android.util.Log
import com.oqba26.barghkar.data.local.dao.CustomerDao
import com.oqba26.barghkar.data.local.entity.CustomerEntity
import com.oqba26.barghkar.data.model.CustomerRemote
import com.oqba26.barghkar.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow

class CustomerRepository(private val customerDao: CustomerDao) {
    private val postgrest get() = SupabaseClient.client.postgrest

    val allCustomers: Flow<List<CustomerEntity>> = customerDao.getAllCustomers()

    suspend fun syncRemoteToLocal(ownerId: String) {
        Log.d("CustomerRepository", "Syncing customers for ownerId: $ownerId")
        try {
            val remoteCustomers = postgrest["customers"].select {
                filter {
                    eq("user_id", ownerId)
                }
            }.decodeList<CustomerRemote>()
            Log.d("CustomerRepository", "Fetched ${remoteCustomers.size} customers from remote")
            
            remoteCustomers.forEach { remoteCustomer ->
                val remoteId = remoteCustomer.id ?: return@forEach
                val localCustomer = customerDao.getCustomerByRemoteId(remoteId)
                
                val customerEntity = CustomerEntity(
                    userId = remoteCustomer.userId,
                    name = remoteCustomer.name,
                    phoneNumber = remoteCustomer.phoneNumber,
                    address = remoteCustomer.address,
                    createdAt = remoteCustomer.createdAt,
                    remoteId = remoteId,
                    isSynced = true,
                )

                if (localCustomer == null) {
                    customerDao.insertCustomer(customerEntity)
                } else if (localCustomer.name != customerEntity.name || 
                           localCustomer.phoneNumber != customerEntity.phoneNumber || 
                           localCustomer.address != customerEntity.address) {
                    customerDao.updateCustomer(customerEntity.copy(id = localCustomer.id))
                }
            }
        } catch (e: Exception) {
            Log.e("CustomerRepository", "Error syncing customers", e)
        }
    }

    suspend fun addCustomer(customer: CustomerEntity): Long {
        return customerDao.insertCustomer(customer)
    }

    suspend fun deleteCustomer(customer: CustomerEntity) {
        customerDao.deleteCustomer(customer)
        if (customer.remoteId != null) {
            try {
                postgrest["customers"].delete {
                    filter {
                        eq("id", customer.remoteId)
                    }
                }
            } catch (e: Exception) {
                Log.e("CustomerRepository", "Error deleting remote customer", e)
            }
        }
    }

    suspend fun getCustomerById(id: Long) = customerDao.getCustomerById(id)
}
