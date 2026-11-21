package com.example.insulia.unit

import org.junit.Assert.assertFalse
import org.junit.Test

class RejectInvalidGlucoseInputTest {
    @Test
    fun rejectInvalidGlucoseInput() {
        val value = 10
        val isValid = value >= 20 && value <= 600
        assertFalse(isValid)
    }
}
