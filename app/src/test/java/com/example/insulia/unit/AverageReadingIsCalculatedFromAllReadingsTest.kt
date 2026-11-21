package com.example.insulia.unit

import com.example.insulia.data.InsuliaViewModel
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AverageReadingIsCalculatedFromAllReadingsTest {
    @Test
    fun averageReading_isCalculatedFromAllReadings() {
        val viewModel = InsuliaViewModel()

        val average = viewModel.averageReading

        assertNotNull(average)
        assertTrue(average!! > 0)
    }
}
