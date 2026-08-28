package com.oqba26.barghkar.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Entity(tableName = "inventory_materials")
@Serializable
data class InventoryMaterialEntity(
    @PrimaryKey(autoGenerate = true)
    @Transient
    val id: Long = 0,
    @SerialName("user_id")
    val userId: String = "",
    val name: String,
    val quantity: Double, // Using Double for cases like 0.5 roll
    val unit: String,
    @Transient
    val remoteId: Long? = null,
    @Transient
    val isSynced: Boolean = false
)
