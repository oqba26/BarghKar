package com.oqba26.barghkar.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {
    @SerialName("master")
    MASTER,
    @SerialName("apprentice")
    APPRENTICE
}

@Serializable
enum class ApprenticePermission {
    MANAGE_INVENTORY,
    EDIT_PROJECTS,
    VIEW_FINANCE
}

@Serializable
data class UserProfile(
    val id: String,
    val email: String?,
    val role: UserRole = UserRole.MASTER,
    @SerialName("master_id")
    val masterId: String? = null,
    @SerialName("full_name")
    val fullName: String? = null,
    @SerialName("permissions")
    val permissions: List<ApprenticePermission> = emptyList()
)
