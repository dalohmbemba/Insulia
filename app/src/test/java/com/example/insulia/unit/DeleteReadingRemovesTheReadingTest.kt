package com.example.insulia.unit

import com.example.insulia.data.InsuliaViewModel
import org.junit.Assert.assertEquals
import org.junit.Test

class DeleteReadingRemovesTheReadingTest {
    @Test
    fun deleteReading_removesTheReading() {
        val viewModel = InsuliaViewModel()
        val first = viewModel.readings.first()

        viewModel.deleteReading(first.id)

        assertEquals(6, viewModel.readings.size)
    }
}
