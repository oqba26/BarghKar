package com.oqba26.barghkar.data.remote

import android.content.Context
import com.oqba26.barghkar.BuildConfig
import com.oqba26.barghkar.security.AppConfigValidator
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

object SupabaseClient {
    private var _client: SupabaseClient? = null
    val client: SupabaseClient
        get() = _client ?: throw IllegalStateException("SupabaseClient must be initialized in Application.onCreate")

    fun validateConfig(url: String, key: String): String? = AppConfigValidator.validateSupabase(url, key)

    fun initialize(context: Context) {
        if (_client != null) return

        val configError = validateConfig(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_KEY)
        if (configError != null) {
            throw IllegalStateException(configError)
        }

        _client = createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL.trim(),
            supabaseKey = BuildConfig.SUPABASE_KEY.trim(),
        ) {
            install(Postgrest)
            install(Auth) {
                sessionManager = EncryptedSessionManager(context)
            }
            install(Storage)
            install(Realtime)
        }
    }
}
