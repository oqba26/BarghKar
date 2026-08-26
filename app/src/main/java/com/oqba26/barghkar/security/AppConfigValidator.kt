package com.oqba26.barghkar.security

import java.net.URI

object AppConfigValidator {
    fun validateSupabase(url: String, key: String): String? {
        val normalizedUrl = url.trim()
        val normalizedKey = key.trim()

        if (normalizedUrl.isBlank()) {
            return "Supabase URL is missing. Configure it before app startup."
        }
        if (normalizedKey.isBlank()) {
            return "Supabase anon key is missing. Configure it before app startup."
        }
        if (!isSafeHttpsUrl(normalizedUrl)) {
            return "Supabase URL must be a valid HTTPS URL."
        }
        return null
    }

    fun isSafeHttpsUrl(url: String): Boolean {
        return try {
            val uri = URI(url.trim())
            val host = uri.host ?: return false
            uri.scheme == "https" && host.isNotBlank() && !host.contains("localhost", ignoreCase = true) && !host.contains("127.0.0.1", ignoreCase = true)
        } catch (_: Exception) {
            false
        }
    }
}
