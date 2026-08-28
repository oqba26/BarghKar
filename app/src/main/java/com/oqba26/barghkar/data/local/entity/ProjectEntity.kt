package com.oqba26.barghkar.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.oqba26.barghkar.data.model.SupabaseTimestampSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

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
    @PrimaryKey(autoGenerate = true)
    @Transient
    val id: Long = 0,
    @SerialName("user_id")
    val userId: String,
    val name: String,
    val description: String,
    @SerialName("customer_id")
    val customerId: Long? = null,
    @SerialName("total_wage")
    val totalWage: Long = 0L,
    @SerialName("created_at")
    @Serializable(with = SupabaseTimestampSerializer::class)
    val createdAt: Long = System.currentTimeMillis(),
    @Transient
    val remoteId: Long? = null,
    @Transient
    val isSynced: Boolean = false,
    
    // فیلدهای جدید اضافه شده
    @SerialName("infrastructure_area")
    val infrastructureArea: Double = 0.0, // زیربنا
    @SerialName("price_per_fixture")
    val pricePerFixture: Long = 0L,       // قیمت هر شعله
    @SerialName("price_per_meter")
    val pricePerMeter: Long = 0L,         // قیمت هر متر مربع
    @SerialName("first_payment")
    val firstPayment: Long = 0L,          // پرداخت مرحله اول
    @SerialName("second_payment")
    val secondPayment: Long = 0L,         // پرداخت مرحله دوم
    @SerialName("third_payment")
    val thirdPayment: Long = 0L           // پرداخت مرحله سوم
)
