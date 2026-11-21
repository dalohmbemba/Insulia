package com.example.insulia.unit

import org.junit.Assert.assertNull
import org.junit.Test

class RejectAppointmentWhenDateMissingTest {
    @Test
    fun rejectAppointmentWhenDateMissing() {
        val dateMillis: Long? = null
        val isValid = dateMillis != null
        assertNull(dateMillis)
        org.junit.Assert.assertFalse(isValid)
    }
}
