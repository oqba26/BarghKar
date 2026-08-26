package com.oqba26.barghkar.data.remote

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
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
        val normalizedEmail = apprenticeEmail.trim()
        val validationError = validateApprenticeEmail(normalizedEmail)
        if (validationError != null) throw IllegalArgumentException(validationError)

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
            throw IllegalArgumentException("کاربری با این ایمیل پیدا نشد")
        }

        val apprenticeProfile = apprenticeProfiles.first()
        if (apprenticeProfile.id == currentUser.id) {
            throw IllegalArgumentException("شما نمی‌توانید خودتان را به عنوان شاگرد اضافه کنید")
        }

        if (apprenticeProfile.role == UserRole.APPRENTICE && apprenticeProfile.masterId != null && apprenticeProfile.masterId != currentUser.id) {
            throw IllegalArgumentException("این کاربر در حال حاضر شاگرد شخص دیگری است")
        }

        postgrest["profiles"].update({
            UserProfile::masterId setTo currentUser.id
            UserProfile::role setTo UserRole.APPRENTICE
            UserProfile::permissions setTo emptyList()
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
        val error = validateCredentials(email, password)
        if (error != null) throw IllegalArgumentException(error)

        auth.signUpWith(Email) {
            this.email = email.trim()
            this.password = password.trim()
        }
    }

    suspend fun signIn(email: String, password: String) {
        val error = validateCredentials(email, password)
        if (error != null) throw IllegalArgumentException(error)

        auth.signInWith(Email) {
            this.email = email.trim()
            this.password = password.trim()
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

    val sessionStatus get() = auth.sessionStatus
}
