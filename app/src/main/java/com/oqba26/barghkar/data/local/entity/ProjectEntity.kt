package com.oqba26.barghkar.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val customerId: Long? = null,
    val totalWage: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)
