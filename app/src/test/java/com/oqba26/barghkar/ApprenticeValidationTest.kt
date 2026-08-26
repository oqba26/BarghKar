package com.oqba26.barghkar

import com.oqba26.barghkar.data.remote.AuthRepository
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ApprenticeValidationTest {
    private val repository = AuthRepository()

    @Test
    fun validateApprenticeEmail_rejectsBlankAndInvalidInput() {
        assertNotNull(repository.validateApprenticeEmail(""))
        assertNotNull(repository.validateApprenticeEmail("not-an-email"))
    }

    @Test
    fun validateApprenticeEmail_acceptsValidEmail() {
        assertNull(repository.validateApprenticeEmail(" apprentice@example.com "))
    }
}
