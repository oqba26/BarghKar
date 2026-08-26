package com.oqba26.barghkar

import com.oqba26.barghkar.data.remote.SupabaseClient
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SupabaseClientValidationTest {
    @Test
    fun validateConfig_rejectsBlankValues() {
        assertNotNull(SupabaseClient.validateConfig("", "key"))
        assertNotNull(SupabaseClient.validateConfig("https://example.supabase.co", ""))
    }

    @Test
    fun validateConfig_rejectsNonHttpsValues() {
        assertNotNull(SupabaseClient.validateConfig("http://example.supabase.co", "key"))
    }

    @Test
    fun validateConfig_acceptsValidHttpsValues() {
        assertNull(SupabaseClient.validateConfig("https://example.supabase.co", "key"))
    }
}
