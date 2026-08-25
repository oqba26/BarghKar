package com.oqba26.barghkar.data.remote

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class EncryptedSessionManager(context: Context) : SessionManager {

    private val json = Json { ignoreUnknownKeys = true }
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "supabase_session_secure",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override suspend fun saveSession(session: UserSession) {
        sharedPreferences.edit {
            putString("session", json.encodeToString(session))
        }
    }

    override suspend fun loadSession(): UserSession? {
        val sessionJson = sharedPreferences.getString("session", null)
        return sessionJson?.let {
            try {
                json.decodeFromString<UserSession>(it)
            } catch (_: Exception) {
                null
            }
        }
    }

    override suspend fun deleteSession() {
        sharedPreferences.edit {
            remove("session")
        }
    }
}
