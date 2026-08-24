package com.oqba26.barghkar.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.oqba26.barghkar.data.local.AppDatabase
import com.oqba26.barghkar.data.remote.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.postgrest

class SyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val projectDao = database.projectDao()
        val customerDao = database.customerDao()
        val inventoryDao = database.inventoryDao()
        
        // چک کردن وضعیت لاگین قبل از سینک
        if (SupabaseClient.client.auth.sessionStatus.value !is SessionStatus.Authenticated) {
            return Result.retry()
        }
        
        return try {
            // ۱. همگام‌سازی مشتریان
            val unsyncedCustomers = customerDao.getUnsyncedCustomers()
            for (customer in unsyncedCustomers) {
                SupabaseClient.client.postgrest["customers"].upsert(customer)
                customerDao.updateCustomer(customer.copy(isSynced = true))
            }

            // ۲. همگام‌سازی پروژه‌ها
            val unsyncedProjects = projectDao.getUnsyncedProjects()
            for (project in unsyncedProjects) {
                SupabaseClient.client.postgrest["projects"].upsert(project)
                projectDao.updateProject(project.copy(isSynced = true))
            }

            // ۳. همگام‌سازی مصالح (Materials)
            val unsyncedMaterials = projectDao.getUnsyncedMaterials()
            for (material in unsyncedMaterials) {
                SupabaseClient.client.postgrest["materials"].upsert(material)
                projectDao.updateMaterial(material.copy(isSynced = true))
            }

            // ۴. همگام‌سازی اقساط (Installments)
            val unsyncedInstallments = projectDao.getUnsyncedInstallments()
            for (installment in unsyncedInstallments) {
                SupabaseClient.client.postgrest["installments"].upsert(installment)
                projectDao.updateInstallment(installment.copy(isSynced = true))
            }

            // ۵. همگام‌سازی انبار (Inventory)
            val unsyncedInventory = inventoryDao.getUnsyncedInventory()
            for (item in unsyncedInventory) {
                SupabaseClient.client.postgrest["inventory_materials"].upsert(item)
                inventoryDao.updateInventory(item.copy(isSynced = true))
            }
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
