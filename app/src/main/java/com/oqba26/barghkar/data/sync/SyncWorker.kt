package com.oqba26.barghkar.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.oqba26.barghkar.data.local.AppDatabase
import com.oqba26.barghkar.data.remote.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

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
                try {
                    val remoteId = if (customer.remoteId == null) {
                        val response = SupabaseClient.client.postgrest["customers"].insert(customer) { select() }
                        response.decodeSingle<JsonObject>()["id"]?.jsonPrimitive?.long
                    } else {
                        SupabaseClient.client.postgrest["customers"].update(customer) {
                            filter { eq("id", customer.remoteId) }
                        }
                        customer.remoteId
                    }
                    customerDao.updateCustomer(customer.copy(isSynced = true, remoteId = remoteId))
                } catch (e: Exception) {
                    Log.e("SyncWorker", "Error syncing customer ${customer.id}", e)
                }
            }

            // ۲. همگام‌سازی پروژه‌ها
            val unsyncedProjects = projectDao.getUnsyncedProjects()
            for (project in unsyncedProjects) {
                try {
                    val customer = project.customerId?.let { customerDao.getCustomerById(it) }
                    if (project.customerId != null && (customer?.remoteId == null)) {
                        Log.w("SyncWorker", "Skipping project ${project.id} because customer is not synced")
                        continue
                    }

                    val projectJson = Json.encodeToJsonElement(project).jsonObject.toMutableMap()
                    projectJson["customer_id"] = JsonPrimitive(customer?.remoteId)
                    
                    val remoteId = if (project.remoteId == null) {
                        val response = SupabaseClient.client.postgrest["projects"].insert(projectJson) { select() }
                        response.decodeSingle<JsonObject>()["id"]?.jsonPrimitive?.long
                    } else {
                        SupabaseClient.client.postgrest["projects"].update(projectJson) {
                            filter { eq("id", project.remoteId) }
                        }
                        project.remoteId
                    }
                    projectDao.updateProject(project.copy(isSynced = true, remoteId = remoteId))
                } catch (e: Exception) {
                    Log.e("SyncWorker", "Error syncing project ${project.id}", e)
                }
            }

            // ۳. همگام‌سازی مصالح (Materials)
            val unsyncedMaterials = projectDao.getUnsyncedMaterials()
            for (material in unsyncedMaterials) {
                try {
                    val project = projectDao.getProjectById(material.projectId)
                    if (project?.remoteId == null) {
                        Log.w("SyncWorker", "Skipping material ${material.id} because project is not synced")
                        continue
                    }

                    val materialJson = Json.encodeToJsonElement(material).jsonObject.toMutableMap()
                    materialJson["project_id"] = JsonPrimitive(project.remoteId)

                    val remoteId = if (material.remoteId == null) {
                        val response = SupabaseClient.client.postgrest["materials"].insert(materialJson) { select() }
                        response.decodeSingle<JsonObject>()["id"]?.jsonPrimitive?.long
                    } else {
                        SupabaseClient.client.postgrest["materials"].update(materialJson) {
                            filter { eq("id", material.remoteId) }
                        }
                        material.remoteId
                    }
                    projectDao.updateMaterial(material.copy(isSynced = true, remoteId = remoteId))
                } catch (e: Exception) {
                    Log.e("SyncWorker", "Error syncing material ${material.id}", e)
                }
            }

            // ۴. همگام‌سازی اقساط (Installments)
            val unsyncedInstallments = projectDao.getUnsyncedInstallments()
            for (installment in unsyncedInstallments) {
                try {
                    val project = projectDao.getProjectById(installment.projectId)
                    if (project?.remoteId == null) {
                        Log.w("SyncWorker", "Skipping installment ${installment.id} because project is not synced")
                        continue
                    }

                    val installmentJson = Json.encodeToJsonElement(installment).jsonObject.toMutableMap()
                    installmentJson["project_id"] = JsonPrimitive(project.remoteId)

                    val remoteId = if (installment.remoteId == null) {
                        val response = SupabaseClient.client.postgrest["installments"].insert(installmentJson) { select() }
                        response.decodeSingle<JsonObject>()["id"]?.jsonPrimitive?.long
                    } else {
                        SupabaseClient.client.postgrest["installments"].update(installmentJson) {
                            filter { eq("id", installment.remoteId) }
                        }
                        installment.remoteId
                    }
                    projectDao.updateInstallment(installment.copy(isSynced = true, remoteId = remoteId))
                } catch (e: Exception) {
                    Log.e("SyncWorker", "Error syncing installment ${installment.id}", e)
                }
            }

            // ۵. همگام‌سازی انبار (Inventory)
            val unsyncedInventory = inventoryDao.getUnsyncedInventory()
            for (item in unsyncedInventory) {
                try {
                    val remoteId = if (item.remoteId == null) {
                        val response = SupabaseClient.client.postgrest["inventory_materials"].insert(item) { select() }
                        response.decodeSingle<JsonObject>()["id"]?.jsonPrimitive?.long
                    } else {
                        SupabaseClient.client.postgrest["inventory_materials"].update(item) {
                            filter { eq("id", item.remoteId) }
                        }
                        item.remoteId
                    }
                    inventoryDao.updateInventory(item.copy(isSynced = true, remoteId = remoteId))
                } catch (e: Exception) {
                    Log.e("SyncWorker", "Error syncing inventory item ${item.id}", e)
                }
            }
            
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Major sync error", e)
            Result.retry()
        }
    }
}
