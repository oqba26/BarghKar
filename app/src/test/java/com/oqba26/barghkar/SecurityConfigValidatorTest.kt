package com.oqba26.barghkar

import com.oqba26.barghkar.security.AppConfigValidator
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityConfigValidatorTest {
    @Test
    fun validateSupabase_acceptsValidHttpsUrl() {
        assertNull(AppConfigValidator.validateSupabase("https://project.supabase.co", "anon-key"))
    }

    @Test
    fun validateSupabase_rejectsMissingValue() {
        assertNotNull(AppConfigValidator.validateSupabase("", "anon-key"))
        assertNotNull(AppConfigValidator.validateSupabase("https://project.supabase.co", ""))
    }

    @Test
    fun isSafeHttpsUrl_rejectsNonHttpsAndLocalhost() {
        assertTrue(AppConfigValidator.isSafeHttpsUrl("https://project.supabase.co"))
        assertTrue(!AppConfigValidator.isSafeHttpsUrl("http://project.supabase.co"))
        assertTrue(!AppConfigValidator.isSafeHttpsUrl("https://localhost/test"))
    }
}
