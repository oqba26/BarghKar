package com.oqba26.barghkar.data.remote

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

object SupabaseClient {
    private const val SUPABASE_URL = "https://vekpdpmbnsbhyubzlaam.supabase.co"
    private const val SUPABASE_KEY = "sb_publishable_uoO1V45_Ku-IVb5_0ffuzA_mW8V2mMn"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY,
    ) {
        install(Postgrest)
        install(Auth)
        install(Storage)
        install(Realtime)
    }
}
