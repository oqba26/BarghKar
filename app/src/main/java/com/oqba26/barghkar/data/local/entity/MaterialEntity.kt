package com.oqba26.barghkar.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.oqba26.barghkar.data.model.RecordStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Entity(
    tableName = "materials",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId"])]
)
@Serializable
data class MaterialEntity(
    @PrimaryKey(autoGenerate = true)
    @Transient
    val id: Long = 0,
    @SerialName("user_id")
    val userId: String = "",
    @SerialName("project_id")
    val projectId: Long,
    val name: String,
    val quantity: Int,
    val unit: String,
    @SerialName("price_per_unit")
    val pricePerUnit: Long = 0L,
    @Transient
    val remoteId: Long? = null,
    @Transient
    val isSynced: Boolean = false,
    val status: RecordStatus = RecordStatus.APPROVED
)
