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
import com.oqba26.barghkar.data.model.RecordStatus
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
                NotificationManager.IMPORTANCE_HIGH
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

        scope.launch {
            channel.subscribe()
        }
    }

    private suspend fun handleCustomerAction(action: PostgresAction) {
        when (action) {
            is PostgresAction.Insert -> {
                val customer = action.decodeRecord<CustomerEntity>()
                database.customerDao().insertCustomer(customer.copy(isSynced = true))
            }
            is PostgresAction.Update -> {
                val customer = action.decodeRecord<CustomerEntity>()
                database.customerDao().updateCustomer(customer.copy(isSynced = true))
            }
            else -> {}
        }
    }

    private suspend fun handleInventoryAction(action: PostgresAction) {
        when (action) {
            is PostgresAction.Insert -> {
                val item = action.decodeRecord<InventoryMaterialEntity>()
                database.inventoryDao().insertInventory(item.copy(isSynced = true))
            }
            is PostgresAction.Update -> {
                val item = action.decodeRecord<InventoryMaterialEntity>()
                database.inventoryDao().updateInventory(item.copy(isSynced = true))
            }
            else -> {}
        }
    }

    private suspend fun handleMaterialAction(action: PostgresAction) {
        when (action) {
            is PostgresAction.Insert -> {
                val material = action.decodeRecord<MaterialEntity>()
                database.projectDao().insertMaterial(material.copy(isSynced = true))
                if (authViewModel.isMaster() && (material.status == RecordStatus.PENDING)) {
                    showNotification("متریال جدید", "شاگرد یک مورد جدید ثبت کرد: ${material.name}")
                }
            }
            is PostgresAction.Update -> {
                val material = action.decodeRecord<MaterialEntity>()
                database.projectDao().updateMaterial(material.copy(isSynced = true))
                if (!authViewModel.isMaster() && (material.status == RecordStatus.APPROVED)) {
                    showNotification("تایید متریال", "اوستا مورد ${material.name} را تایید کرد.")
                }
            }
            is PostgresAction.Delete -> {
                // منطق حذف در صورت نیاز
            }
            else -> {}
        }
    }

    private suspend fun handleInstallmentAction(action: PostgresAction) {
        when (action) {
            is PostgresAction.Insert -> {
                val installment = action.decodeRecord<InstallmentEntity>()
                database.projectDao().insertInstallment(installment.copy(isSynced = true))
                if (authViewModel.isMaster() && (installment.status == RecordStatus.PENDING)) {
                    showNotification("قسط جدید", "شاگرد یک قسط جدید ثبت کرد.")
                }
            }
            is PostgresAction.Update -> {
                val installment = action.decodeRecord<InstallmentEntity>()
                database.projectDao().updateInstallment(installment.copy(isSynced = true))
                if (!authViewModel.isMaster() && (installment.status == RecordStatus.APPROVED)) {
                    showNotification("تایید قسط", "اوستا یک قسط را تایید کرد.")
                }
            }
            else -> {}
        }
    }

    private suspend fun handleProjectAction(action: PostgresAction) {
        when (action) {
            is PostgresAction.Insert -> {
                val project = action.decodeRecord<ProjectEntity>()
                database.projectDao().updateProject(project.copy(isSynced = true))
                if (authViewModel.isMaster()) {
                    showNotification("پروژه جدید", "یک پروژه جدید ثبت شد: ${project.name}")
                }
            }
            is PostgresAction.Update -> {
                val project = action.decodeRecord<ProjectEntity>()
                database.projectDao().updateProject(project.copy(isSynced = true))
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
