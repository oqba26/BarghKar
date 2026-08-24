package com.oqba26.barghkar.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "projects",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["customerId"])]
)
@Serializable
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val customerId: Long? = null,
    val totalWage: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val remoteId: String? = null,
    val isSynced: Boolean = false,
    
    // فیلدهای جدید اضافه شده
    val infrastructureArea: Double = 0.0, // زیربنا
    val pricePerFixture: Long = 0L,       // قیمت هر شعله
    val pricePerMeter: Long = 0L,         // قیمت هر متر مربع
    val firstPayment: Long = 0L,          // پرداخت مرحله اول
    val secondPayment: Long = 0L,         // پرداخت مرحله دوم
    val thirdPayment: Long = 0L           // پرداخت مرحله سوم
)
