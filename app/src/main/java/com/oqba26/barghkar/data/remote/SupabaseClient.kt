package com.oqba26.barghkar.data.remote

import android.content.Context
import com.oqba26.barghkar.BuildConfig
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

    fun initialize(context: Context) {
        if (_client != null) return

        _client = createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_KEY,
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
