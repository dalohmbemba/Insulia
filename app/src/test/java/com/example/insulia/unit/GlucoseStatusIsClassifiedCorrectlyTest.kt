package com.example.insulia.unit

import com.example.insulia.data.GlucoseStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class GlucoseStatusIsClassifiedCorrectlyTest {
    @Test
    fun glucoseStatus_isClassifiedCorrectly() {
        assertEquals(GlucoseStatus.LOW, GlucoseStatus.of(65))
        assertEquals(GlucoseStatus.IN_RANGE, GlucoseStatus.of(100))
        assertEquals(GlucoseStatus.HIGH, GlucoseStatus.of(150))
    }
}
