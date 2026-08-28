package com.oqba26.barghkar.data.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object SupabaseTimestampSerializer : KSerializer<Long> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("SupabaseTimestamp", PrimitiveKind.STRING)
    
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override fun serialize(encoder: Encoder, value: Long) {
        val isoString = isoFormat.format(Date(value))
        encoder.encodeString(isoString)
    }

    override fun deserialize(decoder: Decoder): Long {
        val isoString = decoder.decodeString()
        return try {
            isoFormat.parse(isoString)?.time ?: System.currentTimeMillis()
        } catch (_: Exception) {
            isoString.toLongOrNull() ?: System.currentTimeMillis()
        }
    }
}

@Serializable
data class CustomerRemote(
    val id: Long? = null,
    @SerialName("user_id")
    val userId: String,
    val name: String,
    @SerialName("phone_number")
    val phoneNumber: String,
    val address: String,
    @SerialName("created_at")
    @Serializable(with = SupabaseTimestampSerializer::class)
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class InventoryRemote(
    val id: Long? = null,
    @SerialName("user_id")
    val userId: String,
    val name: String,
    val quantity: Double,
    val unit: String
)

@Serializable
data class ProjectRemote(
    val id: Long? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("customer_id")
    val customerId: Long? = null,
    val name: String,
    val description: String,
    @SerialName("total_wage")
    val totalWage: Long = 0L,
    @SerialName("created_at")
    @Serializable(with = SupabaseTimestampSerializer::class)
    val createdAt: Long = System.currentTimeMillis(),
    @SerialName("infrastructure_area")
    val infrastructureArea: Double = 0.0,
    @SerialName("price_per_fixture")
    val pricePerFixture: Long = 0L,
    @SerialName("price_per_meter")
    val pricePerMeter: Long = 0L,
    @SerialName("first_payment")
    val firstPayment: Long = 0L,
    @SerialName("second_payment")
    val secondPayment: Long = 0L,
    @SerialName("third_payment")
    val thirdPayment: Long = 0L
)

@Serializable
data class MaterialRemote(
    val id: Long? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("project_id")
    val projectId: Long,
    val name: String,
    val quantity: Int,
    val unit: String,
    @SerialName("price_per_unit")
    val pricePerUnit: Long = 0L,
    val status: RecordStatus = RecordStatus.APPROVED
)

@Serializable
data class InstallmentRemote(
    val id: Long? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("project_id")
    val projectId: Long,
    val amount: Long,
    @SerialName("due_date")
    @Serializable(with = SupabaseTimestampSerializer::class)
    val dueDate: Long,
    @SerialName("is_paid")
    val isPaid: Boolean = false,
    val status: RecordStatus = RecordStatus.APPROVED
)
