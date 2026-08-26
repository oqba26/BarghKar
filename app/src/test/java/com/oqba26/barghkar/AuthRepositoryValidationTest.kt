package com.oqba26.barghkar

import com.oqba26.barghkar.data.remote.AuthRepository
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AuthRepositoryValidationTest {
    private val repository = AuthRepository()

    @Test
    fun validateCredentials_rejectsEmptyEmail() {
        assertNotNull(repository.validateCredentials("", "12345678"))
    }

    @Test
    fun validateCredentials_rejectsInvalidEmailFormat() {
        assertNotNull(repository.validateCredentials("invalid-email", "12345678"))
    }

    @Test
    fun validateCredentials_rejectsShortPassword() {
        assertNotNull(repository.validateCredentials("user@example.com", "1234567"))
    }

    @Test
    fun validateCredentials_acceptsValidCredentials() {
        assertNull(repository.validateCredentials("user@example.com", "12345678"))
    }
}
