package com.oqba26.barghkar.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.oqba26.barghkar.data.model.RecordStatus
import com.oqba26.barghkar.data.model.SupabaseTimestampSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

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
    @PrimaryKey(autoGenerate = true)
    @Transient
    val id: Long = 0,
    @SerialName("user_id")
    val userId: String = "",
    @SerialName("project_id")
    val projectId: Long,
    val amount: Long,
    @SerialName("due_date")
    @Serializable(with = SupabaseTimestampSerializer::class)
    val dueDate: Long,
    @SerialName("is_paid")
    val isPaid: Boolean = false,
    @Transient
    val remoteId: Long? = null,
    @Transient
    val isSynced: Boolean = false,
    val status: RecordStatus = RecordStatus.APPROVED
)
