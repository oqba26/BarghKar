package com.oqba26.barghkar.data.remote

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import com.oqba26.barghkar.data.model.UserProfile
import com.oqba26.barghkar.data.model.UserRole

class AuthRepository {
    private val auth = SupabaseClient.client.auth
    private val postgrest = SupabaseClient.client.postgrest

    suspend fun getUserProfile(): UserProfile? {
        val id = auth.currentUserOrNull()?.id ?: return null
        return try {
            postgrest["profiles"].select {
                filter {
                    eq("id", id)
                }
            }.decodeSingleOrNull<UserProfile>()
        } catch (_: Exception) {
            null
        }
    }

    suspend fun linkMaster(masterEmail: String) {
        val masterProfile = postgrest["profiles"].select {
            filter {
                eq("email", masterEmail)
            }
        }.decodeSingleOrNull<UserProfile>() ?: throw Exception("اوستایی با این ایمیل پیدا نشد")

        val currentId = auth.currentUserOrNull()?.id ?: return
        
        postgrest["profiles"].update({
            UserProfile::masterId setTo masterProfile.id
            UserProfile::role setTo UserRole.APPRENTICE
        }) {
            filter {
                eq("id", currentId)
            }
        }
    }

    suspend fun signUp(email: String, password: String) {
        auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signIn(email: String, password: String) {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signOut() {
        auth.signOut()
    }

    val sessionStatus = auth.sessionStatus
}
