package com.oqba26.barghkar.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.oqba26.barghkar.data.model.SupabaseTimestampSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Entity(tableName = "customers")
@Serializable
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true)
    @Transient
    val id: Long = 0,
    @SerialName("user_id")
    val userId: String,
    val name: String,
    @SerialName("phone_number")
    val phoneNumber: String,
    val address: String,
    @SerialName("created_at")
    @Serializable(with = SupabaseTimestampSerializer::class)
    val createdAt: Long = System.currentTimeMillis(),
    @Transient
    val remoteId: Long? = null,
    @Transient
    val isSynced: Boolean = false
)
