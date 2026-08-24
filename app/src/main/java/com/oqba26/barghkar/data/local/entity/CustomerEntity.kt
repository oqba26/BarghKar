package com.oqba26.barghkar.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "customers")
@Serializable
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val address: String,
    val createdAt: Long = System.currentTimeMillis(),
    val remoteId: String? = null,
    val isSynced: Boolean = false
)
