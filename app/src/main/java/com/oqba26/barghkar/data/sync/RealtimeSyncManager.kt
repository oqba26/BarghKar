package com.oqba26.barghkar.data.sync

import android.annotation.SuppressLint
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.oqba26.barghkar.MainActivity
import com.oqba26.barghkar.R
import com.oqba26.barghkar.data.local.AppDatabase
import com.oqba26.barghkar.data.local.entity.CustomerEntity
import com.oqba26.barghkar.data.local.entity.InstallmentEntity
import com.oqba26.barghkar.data.local.entity.InventoryMaterialEntity
import com.oqba26.barghkar.data.local.entity.MaterialEntity
import com.oqba26.barghkar.data.local.entity.ProjectEntity
import com.oqba26.barghkar.data.model.CustomerRemote
import com.oqba26.barghkar.data.model.InstallmentRemote
import com.oqba26.barghkar.data.model.InventoryRemote
import com.oqba26.barghkar.data.model.MaterialRemote
import com.oqba26.barghkar.data.model.ProjectRemote
import com.oqba26.barghkar.data.model.RecordStatus
import com.oqba26.barghkar.data.model.UserProfile
import com.oqba26.barghkar.data.remote.SupabaseClient
import com.oqba26.barghkar.ui.viewmodels.AuthViewModel
import io.github.jan.supabase.realtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class RealtimeSyncManager(
    private val context: Context,
    private val authViewModel: AuthViewModel,
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val database = AppDatabase.getDatabase(context)
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "barghkar_sync",
                "اطلاعیه‌های برق‌کار",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "اطلاع‌رسانی تغییرات توسط شاگرد"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun startListening() {
        val channel = SupabaseClient.client.channel("public-changes")
        
        // ۱. شنیدن تغییرات جدول مصالح
        channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "materials"
        }.onEach { action ->
            handleMaterialAction(action)
        }.launchIn(scope)

        // ۲. شنیدن تغییرات جدول اقساط
        channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "installments"
        }.onEach { action ->
            handleInstallmentAction(action)
        }.launchIn(scope)

        // ۳. شنیدن تغییرات جدول پروژه‌ها
        channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "projects"
        }.onEach { action ->
            handleProjectAction(action)
        }.launchIn(scope)

        // ۴. شنیدن تغییرات جدول مشتریان
        channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "customers"
        }.onEach { action ->
            handleCustomerAction(action)
        }.launchIn(scope)

        // ۵. شنیدن تغییرات جدول انبار
        channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "inventory_materials"
        }.onEach { action ->
            handleInventoryAction(action)
        }.launchIn(scope)

        // ۶. شنیدن تغییرات جدول پروفایل (برای دسترسی‌های شاگرد)
        channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "profiles"
        }.onEach { action ->
            android.util.Log.d("RealtimeSync", "Received profile update action: $action")
            if (action is PostgresAction.Update) {
                val updatedProfile = action.decodeRecord<UserProfile>()
                android.util.Log.d("RealtimeSync", "Updated profile ID: ${updatedProfile.id}, Current ID: ${authViewModel.userProfile.value?.id}")
                if (updatedProfile.id == authViewModel.userProfile.value?.id) {
                    authViewModel.refreshProfile()
                }
            }
        }.launchIn(scope)

        scope.launch {
            channel.subscribe()
        }
    }

    private suspend fun handleCustomerAction(action: PostgresAction) {
        android.util.Log.d("RealtimeSync", "Customer action received: $action")
        val currentOwnerId = authViewModel.getOwnerId()
        android.util.Log.d("RealtimeSync", "Current owner ID: $currentOwnerId")
        
        if (currentOwnerId == null) return
        
        when (action) {
            is PostgresAction.Insert, is PostgresAction.Update -> {
                val remote = action.decodeRecord<CustomerRemote>()
                if (remote.userId == currentOwnerId && remote.id != null) {
                    val existingByRemoteId = database.customerDao().getCustomerByRemoteId(remote.id)
                    
                    val entity = CustomerEntity(
                        userId = remote.userId,
                        name = remote.name,
                        phoneNumber = remote.phoneNumber,
                        address = remote.address,
                        createdAt = remote.createdAt,
                        remoteId = remote.id,
                        isSynced = true
                    )

                    if (existingByRemoteId != null) {
                        database.customerDao().updateCustomer(entity.copy(id = existingByRemoteId.id))
                        android.util.Log.d("RealtimeSync", "Updated existing customer by remoteId: ${remote.name}")
                    } else {
                        // چک کردن برای رکوردهای محلی که هنوز remoteId نگرفته‌اند (جلوگیری از تکرار در گوشی اوستا)
                        val unsynced = database.customerDao().getCustomerByNameAndPhone(remote.name, remote.phoneNumber)
                        if (unsynced != null && unsynced.remoteId == null) {
                            database.customerDao().updateCustomer(entity.copy(id = unsynced.id))
                            android.util.Log.d("RealtimeSync", "Matched unsynced local customer and updated: ${remote.name}")
                        } else {
                            database.customerDao().insertCustomer(entity)
                            android.util.Log.d("RealtimeSync", "Inserted new customer from remote: ${remote.name}")
                        }
                    }
                }
            }
            is PostgresAction.Delete -> {
                // منطق حذف در صورت نیاز
            }
            else -> {}
        }
    }

    private suspend fun handleInventoryAction(action: PostgresAction) {
        val currentOwnerId = authViewModel.getOwnerId() ?: return
        when (action) {
            is PostgresAction.Insert, is PostgresAction.Update -> {
                val remote = action.decodeRecord<InventoryRemote>()
                if (remote.userId == currentOwnerId && remote.id != null) {
                    val existingByRemoteId = database.inventoryDao().getInventoryItemByRemoteId(remote.id)
                    val entity = InventoryMaterialEntity(
                        userId = remote.userId,
                        name = remote.name,
                        quantity = remote.quantity,
                        unit = remote.unit,
                        remoteId = remote.id,
                        isSynced = true
                    )
                    if (existingByRemoteId != null) {
                        database.inventoryDao().updateInventory(entity.copy(id = existingByRemoteId.id))
                    } else {
                        val unsynced = database.inventoryDao().getInventoryItemByName(remote.name, remote.userId)
                        if (unsynced != null && unsynced.remoteId == null) {
                            database.inventoryDao().updateInventory(entity.copy(id = unsynced.id))
                        } else {
                            database.inventoryDao().insertInventory(entity)
                        }
                    }
                }
            }
            else -> {}
        }
    }

    private suspend fun handleMaterialAction(action: PostgresAction) {
        val currentOwnerId = authViewModel.getOwnerId() ?: return
        when (action) {
            is PostgresAction.Insert, is PostgresAction.Update -> {
                val remote = action.decodeRecord<MaterialRemote>()
                if (remote.userId == currentOwnerId && remote.id != null) {
                    val existingByRemoteId = database.projectDao().getMaterialByRemoteId(remote.id)
                    val localProjectId = database.projectDao().getProjectByRemoteId(remote.projectId)?.id ?: return
                    
                    val entity = MaterialEntity(
                        userId = remote.userId,
                        projectId = localProjectId,
                        name = remote.name,
                        quantity = remote.quantity,
                        unit = remote.unit,
                        pricePerUnit = remote.pricePerUnit,
                        remoteId = remote.id,
                        isSynced = true,
                        status = remote.status
                    )
                    
                    if (existingByRemoteId != null) {
                        database.projectDao().updateMaterial(entity.copy(id = existingByRemoteId.id))
                    } else {
                        val unsynced = database.projectDao().getMaterialByName(remote.name, localProjectId)
                        if (unsynced != null && unsynced.remoteId == null) {
                            database.projectDao().updateMaterial(entity.copy(id = unsynced.id))
                        } else {
                            database.projectDao().insertMaterial(entity)
                        }
                    }

                    if (action is PostgresAction.Insert && authViewModel.isMaster() && (remote.status == RecordStatus.PENDING)) {
                        showNotification("متریال جدید", "شاگرد یک مورد جدید ثبت کرد: ${remote.name}")
                    } else if (action is PostgresAction.Update && !authViewModel.isMaster() && (remote.status == RecordStatus.APPROVED)) {
                        showNotification("تایید متریال", "اوستا مورد ${remote.name} را تایید کرد.")
                    }
                }
            }
            is PostgresAction.Delete -> {
                // منطق حذف در صورت نیاز
            }
            else -> {}
        }
    }

    private suspend fun handleInstallmentAction(action: PostgresAction) {
        val currentOwnerId = authViewModel.getOwnerId() ?: return
        when (action) {
            is PostgresAction.Insert, is PostgresAction.Update -> {
                val remote = action.decodeRecord<InstallmentRemote>()
                if (remote.userId == currentOwnerId && remote.id != null) {
                    val existingByRemoteId = database.projectDao().getInstallmentByRemoteId(remote.id)
                    val localProjectId = database.projectDao().getProjectByRemoteId(remote.projectId)?.id ?: return
                    
                    val entity = InstallmentEntity(
                        userId = remote.userId,
                        projectId = localProjectId,
                        amount = remote.amount,
                        dueDate = remote.dueDate,
                        isPaid = remote.isPaid,
                        remoteId = remote.id,
                        isSynced = true,
                        status = remote.status
                    )
                    
                    if (existingByRemoteId != null) {
                        database.projectDao().updateInstallment(entity.copy(id = existingByRemoteId.id))
                    } else {
                        val unsynced = database.projectDao().getInstallmentByAmountAndDate(remote.amount, remote.dueDate, localProjectId)
                        if (unsynced != null && unsynced.remoteId == null) {
                            database.projectDao().updateInstallment(entity.copy(id = unsynced.id))
                        } else {
                            database.projectDao().insertInstallment(entity)
                        }
                    }

                    if (action is PostgresAction.Insert && authViewModel.isMaster() && (remote.status == RecordStatus.PENDING)) {
                        showNotification("قسط جدید", "شاگرد یک قسط جدید ثبت کرد.")
                    } else if (action is PostgresAction.Update && !authViewModel.isMaster() && (remote.status == RecordStatus.APPROVED)) {
                        showNotification("تایید قسط", "اوستا یک قسط را تایید کرد.")
                    }
                }
            }
            else -> {}
        }
    }

    private suspend fun handleProjectAction(action: PostgresAction) {
        android.util.Log.d("RealtimeSync", "Project action received: $action")
        val currentOwnerId = authViewModel.getOwnerId() ?: return
        
        when (action) {
            is PostgresAction.Insert, is PostgresAction.Update -> {
                val remote = action.decodeRecord<ProjectRemote>()
                if (remote.userId == currentOwnerId && remote.id != null) {
                    val existingByRemoteId = database.projectDao().getProjectByRemoteId(remote.id)
                    val localCustomerId = remote.customerId?.let { database.customerDao().getCustomerByRemoteId(it)?.id }
                    
                    val entity = ProjectEntity(
                        userId = remote.userId,
                        name = remote.name,
                        description = remote.description,
                        customerId = localCustomerId,
                        totalWage = remote.totalWage,
                        createdAt = remote.createdAt,
                        remoteId = remote.id,
                        isSynced = true,
                        infrastructureArea = remote.infrastructureArea,
                        pricePerFixture = remote.pricePerFixture,
                        pricePerMeter = remote.pricePerMeter,
                        firstPayment = remote.firstPayment,
                        secondPayment = remote.secondPayment,
                        thirdPayment = remote.thirdPayment
                    )
                    
                    if (existingByRemoteId != null) {
                        database.projectDao().updateProject(entity.copy(id = existingByRemoteId.id))
                    } else {
                        val unsynced = database.projectDao().getProjectByName(remote.name, remote.userId)
                        if (unsynced != null && unsynced.remoteId == null) {
                            database.projectDao().updateProject(entity.copy(id = unsynced.id))
                        } else {
                            database.projectDao().insertProject(entity)
                        }
                        
                        if (action is PostgresAction.Insert && authViewModel.isMaster()) {
                            showNotification("پروژه جدید", "یک پروژه جدید ثبت شد: ${remote.name}")
                        }
                    }
                }
            }
            else -> {}
        }
    }

    @SuppressLint("MissingPermission")
    private fun showNotification(title: String, message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, "barghkar_sync")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 200, 500))

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
