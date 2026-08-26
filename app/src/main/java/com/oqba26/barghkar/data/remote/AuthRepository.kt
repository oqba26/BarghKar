package com.oqba26.barghkar.data.remote

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import com.oqba26.barghkar.data.model.UserProfile
import com.oqba26.barghkar.data.model.UserRole
import com.oqba26.barghkar.data.model.ApprenticePermission

class AuthRepository {
    private val auth = SupabaseClient.client.auth
    private val postgrest = SupabaseClient.client.postgrest

    suspend fun getUserProfile(): UserProfile? {
        val user = auth.currentUserOrNull() ?: return null
        return try {
            val profile = postgrest["profiles"].select {
                filter {
                    eq("id", user.id)
                }
            }.decodeSingleOrNull<UserProfile>()

            if (profile == null) {
                val newProfile = UserProfile(
                    id = user.id,
                    email = user.email,
                    role = UserRole.MASTER
                )
                postgrest["profiles"].insert(newProfile)
                newProfile
            } else {
                profile
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun addApprenticeByEmail(apprenticeEmail: String) {
        val apprenticeProfile = postgrest["profiles"].select {
            filter {
                eq("email", apprenticeEmail)
            }
        }.decodeSingleOrNull<UserProfile>() ?: throw Exception("کاربری با این ایمیل پیدا نشد")

        if ((apprenticeProfile.role == UserRole.APPRENTICE) && (apprenticeProfile.masterId != null)) {
            throw Exception("این کاربر در حال حاضر شاگرد شخص دیگری است")
        }

        val currentId = auth.currentUserOrNull()?.id ?: return
        
        postgrest["profiles"].update({
            UserProfile::masterId setTo currentId
            UserProfile::role setTo UserRole.APPRENTICE
        }) {
            filter {
                eq("id", apprenticeProfile.id)
            }
        }
    }

    suspend fun getApprentices(): List<UserProfile> {
        val currentId = auth.currentUserOrNull()?.id ?: return emptyList()
        return try {
            postgrest["profiles"].select {
                filter {
                    eq("master_id", currentId)
                }
            }.decodeList<UserProfile>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun updateApprenticePermissions(apprenticeId: String, permissions: List<ApprenticePermission>) {
        postgrest["profiles"].update({
            UserProfile::permissions setTo permissions
        }) {
            filter {
                eq("id", apprenticeId)
            }
        }
    }

    suspend fun removeApprentice(apprenticeId: String) {
        postgrest["profiles"].update({
            UserProfile::masterId setTo null
            UserProfile::role setTo UserRole.MASTER
            UserProfile::permissions setTo emptyList()
        }) {
            filter {
                eq("id", apprenticeId)
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
        
        // چک کردن تاییدیه ایمیل بلافاصله بعد از ورود
        val user = auth.currentUserOrNull()
        if (user != null && user.emailConfirmedAt == null) {
            auth.signOut()
            throw Exception("ایمیل شما هنوز تایید نشده است. لطفاً لینک ارسال شده به ایمیلتان را چک کنید.")
        }
    }

    fun isEmailConfirmed(): Boolean {
        return auth.currentUserOrNull()?.emailConfirmedAt != null
    }

    suspend fun signOut() {
        auth.signOut()
    }

    val sessionStatus = auth.sessionStatus
}
