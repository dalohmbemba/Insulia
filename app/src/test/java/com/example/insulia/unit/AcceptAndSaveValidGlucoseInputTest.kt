package com.example.insulia.unit

import com.example.insulia.data.InsuliaViewModel
import com.example.insulia.data.ReadingContext
import org.junit.Assert.assertEquals
import org.junit.Test

class AcceptAndSaveValidGlucoseInputTest {
    @Test
    fun acceptAndSaveValidGlucoseInput() {
        val viewModel = InsuliaViewModel()
        val value = 123
        val isValid = value >= 20 && value <= 600
        org.junit.Assert.assertTrue(isValid)
        viewModel.addReading(value, ReadingContext.AFTER_BREAKFAST)
        assertEquals(123, viewModel.latestReading?.valueMgDl)
    }
}
