package com.example.insulia.unit

import com.example.insulia.data.InsuliaViewModel
import com.example.insulia.data.ReadingContext
import org.junit.Assert.assertEquals
import org.junit.Test

class AddReadingAddsReadingAndMakesItTheLatestTest {
    @Test
    fun addReading_addsReadingAndMakesItTheLatest() {
        val viewModel = InsuliaViewModel()

        viewModel.addReading(123, ReadingContext.BEFORE_MEAL)

        assertEquals(8, viewModel.readings.size)
        assertEquals(123, viewModel.latestReading?.valueMgDl)
    }
}
