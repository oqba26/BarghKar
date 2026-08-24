package com.oqba26.barghkar.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class RecordStatus {
    PENDING,
    APPROVED,
    REJECTED
}
