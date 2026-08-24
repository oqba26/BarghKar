package com.oqba26.barghkar.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.oqba26.barghkar.data.model.RecordStatus
import kotlinx.serialization.Serializable

@Entity(
    tableName = "installments",
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
data class InstallmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val amount: Long,
    val dueDate: Long,
    val isPaid: Boolean = false,
    val remoteId: String? = null,
    val isSynced: Boolean = false,
    val status: RecordStatus = RecordStatus.APPROVED
)
