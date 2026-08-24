package com.oqba26.barghkar.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "inventory_materials")
@Serializable
data class InventoryMaterialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val quantity: Double, // Using Double for cases like 0.5 roll
    val unit: String,
    val remoteId: String? = null,
    val isSynced: Boolean = false
)
