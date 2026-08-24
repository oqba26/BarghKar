package com.oqba26.barghkar.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {
    MASTER,
    APPRENTICE
}

@Serializable
data class UserProfile(
    val id: String,
    val email: String?,
    val role: UserRole = UserRole.MASTER,
    val masterId: String? = null,
    val fullName: String? = null,
)
