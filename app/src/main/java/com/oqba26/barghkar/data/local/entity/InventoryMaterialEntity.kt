package com.oqba26.barghkar.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_materials")
data class InventoryMaterialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val quantity: Double, // Using Double for cases like 0.5 roll
    val unit: String
)
