package com.oqba26.barghkar.data.remote

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import com.oqba26.barghkar.data.model.UserProfile
import com.oqba26.barghkar.data.model.UserRole
import com.oqba26.barghkar.data.model.ApprenticePermission

class AuthRepository {
    private val auth get() = SupabaseClient.client.auth
    private val postgrest get() = SupabaseClient.client.postgrest
    private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validateCredentials(email: String, password: String): String? {
        val normalizedEmail = email.trim()
        val normalizedPassword = password.trim()

        if (normalizedEmail.isBlank()) return "ایمیل نمی‌تواند خالی باشد"
        if (!emailRegex.matches(normalizedEmail)) return "فرمت ایمیل معتبر نیست"
        if (normalizedPassword.length < 8) return "رمز عبور باید حداقل 8 کاراکتر باشد"
        return null
    }

    fun validateApprenticeEmail(email: String): String? {
        val normalizedEmail = email.trim()
        if (normalizedEmail.isBlank()) return "ایمیل شاگرد نمی‌تواند خالی باشد"
        if (!emailRegex.matches(normalizedEmail)) return "فرمت ایمیل شاگرد معتبر نیست"
        return null
    }

    suspend fun getUserProfile(): UserProfile? {
        val user = auth.currentUserOrNull() ?: return null
        return try {
            val response = postgrest["profiles"].select {
                filter {
                    eq("id", user.id)
                }
            }
            android.util.Log.d("AuthRepository", "Raw Response: ${response.data}")
            val profile = response.decodeSingleOrNull<UserProfile>()

            if (profile == null) {
                android.util.Log.d("AuthRepository", "Profile not found, creating new one for: ${user.email}")
                val newProfile = UserProfile(
                    id = user.id,
                    email = user.email,
                    role = UserRole.MASTER,
                )
                postgrest["profiles"].insert(newProfile)
                newProfile
            } else {
                android.util.Log.d("AuthRepository", "Profile loaded: ${profile.role}")
                profile
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Error fetching profile", e)
            null
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val userProfileFlow: Flow<UserProfile?> = auth.sessionStatus.flatMapLatest { status ->
        if (status is SessionStatus.Authenticated) {
            val channel = SupabaseClient.client.channel("profile_changes")
            channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "profiles"
            }.map {
                getUserProfile()
            }.onStart {
                emit(getUserProfile())
            }
        } else {
            emptyFlow()
        }
    }

    suspend fun addApprenticeByEmail(apprenticeEmail: String) {
        val normalizedEmail = apprenticeEmail.trim()
        validateApprenticeEmail(normalizedEmail)?.let { throw IllegalArgumentException(it) }

        val currentUser = auth.currentUserOrNull() ?: throw IllegalStateException("کاربر واردشده‌ای پیدا نشد")
        val currentUserProfile = postgrest["profiles"].select {
            filter {
                eq("id", currentUser.id)
            }
        }.decodeSingleOrNull<UserProfile>()

        if (currentUserProfile?.role == UserRole.APPRENTICE) {
            throw IllegalStateException("کاربر شاگرد نمی‌تواند شاگرد جدید اضافه کند")
        }

        if (currentUser.email?.trim()?.equals(normalizedEmail, ignoreCase = true) == true) {
            throw IllegalArgumentException("شما نمی‌توانید خودتان را به عنوان شاگرد اضافه کنید")
        }

        val apprenticeProfiles = postgrest["profiles"].select {
            filter {
                eq("email", normalizedEmail)
            }
        }.decodeList<UserProfile>()

        if (apprenticeProfiles.isEmpty()) {
            throw IllegalArgumentException("این ایمیل هنوز در برنامه ثبت‌نام نکرده است")
        }

        val apprenticeProfile = apprenticeProfiles.first()
        if (apprenticeProfile.id == currentUser.id) {
            throw IllegalArgumentException("شما نمی‌توانید خودتان را به عنوان شاگرد اضافه کنید")
        }

        if ((apprenticeProfile.role == UserRole.APPRENTICE) && 
            (apprenticeProfile.masterId != null) && 
            (apprenticeProfile.masterId != currentUser.id)) {
            throw IllegalArgumentException("این کاربر در حال حاضر شاگرد شخص دیگری است")
        }

        try {
            postgrest["profiles"].update(
                {
                    UserProfile::masterId setTo currentUser.id
                    UserProfile::role setTo UserRole.APPRENTICE
                    UserProfile::permissions setTo emptyList()
                },
            ) {
                filter {
                    eq("id", apprenticeProfile.id)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Error adding apprentice: ${e.message}", e)
            throw e
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
            android.util.Log.e("AuthRepository", "Error fetching apprentices: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun updateApprenticePermissions(apprenticeId: String, permissions: List<ApprenticePermission>) {
        try {
            postgrest["profiles"].update(
                {
                    UserProfile::permissions setTo permissions
                },
            ) {
                filter {
                    eq("id", apprenticeId)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Error updating permissions: ${e.message}", e)
            throw e
        }
    }

    suspend fun removeApprentice(apprenticeId: String) {
        try {
            postgrest["profiles"].update(
                {
                    UserProfile::masterId setTo null
                    UserProfile::role setTo UserRole.MASTER
                    UserProfile::permissions setTo emptyList()
                },
            ) {
                filter {
                    eq("id", apprenticeId)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Error removing apprentice: ${e.message}", e)
            throw e
        }
    }

    suspend fun signUp(email: String, password: String) {
        validateCredentials(email, password)?.let { throw IllegalArgumentException(it) }

        auth.signUpWith(Email) {
            this.email = email.trim()
            this.password = password.trim()
        }
    }

    suspend fun signIn(email: String, password: String) {
        validateCredentials(email, password)?.let { throw IllegalArgumentException(it) }

        auth.signInWith(Email) {
            this.email = email.trim()
            this.password = password.trim()
        }
    }

    suspend fun signOut() {
        auth.signOut()
    }

    val sessionStatus get() = auth.sessionStatus
}
