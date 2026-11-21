package com.example.insulia.unit

import com.example.insulia.data.InsuliaViewModel
import org.junit.Assert.assertNull
import org.junit.Test

class AverageReadingIsNullWhenThereAreNoReadingsTest {
    @Test
    fun averageReading_isNullWhenThereAreNoReadings() {
        val viewModel = InsuliaViewModel()
        viewModel.readings.forEach { viewModel.deleteReading(it.id) }

        assertNull(viewModel.averageReading)
    }
}
