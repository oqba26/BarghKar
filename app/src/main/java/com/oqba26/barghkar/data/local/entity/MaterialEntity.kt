package com.oqba26.barghkar.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.oqba26.barghkar.data.model.RecordStatus
import kotlinx.serialization.Serializable

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
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val name: String,
    val quantity: Int,
    val unit: String,
    val pricePerUnit: Long = 0L,
    val remoteId: String? = null,
    val isSynced: Boolean = false,
    val status: RecordStatus = RecordStatus.APPROVED
)
