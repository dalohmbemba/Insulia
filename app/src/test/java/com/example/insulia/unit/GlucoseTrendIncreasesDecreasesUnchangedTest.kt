package com.example.insulia.unit

import org.junit.Assert.assertEquals
import org.junit.Test

class GlucoseTrendIncreasesDecreasesUnchangedTest {
    @Test
    fun glucoseTrend_increasesDecreasesUnchanged() {
        val increasing = listOf(100, 110, 120)
        val decreasing = listOf(120, 110, 100)
        val unchanged = listOf(110, 110, 110)

        assertEquals(true, increasing.last() > increasing.first())
        assertEquals(true, decreasing.last() < decreasing.first())
        assertEquals(true, unchanged.last() == unchanged.first())
    }
}
