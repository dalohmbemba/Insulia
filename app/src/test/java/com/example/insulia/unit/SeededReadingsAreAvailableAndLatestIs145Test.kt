package com.example.insulia.unit

import com.example.insulia.data.InsuliaViewModel
import org.junit.Assert.assertEquals
import org.junit.Test

class SeededReadingsAreAvailableAndLatestIs145Test {
    @Test
    fun seededReadings_areAvailableAndLatestIs145() {
        val viewModel = InsuliaViewModel()

        assertEquals(7, viewModel.readings.size)
        assertEquals(145, viewModel.latestReading?.valueMgDl)
    }
}
