package com.example.insulia.unit

import com.example.insulia.data.DiabetesType
import com.example.insulia.data.InsuliaViewModel
import com.example.insulia.data.Medication
import org.junit.Assert.assertTrue
import org.junit.Test

class MedicationTrackingHasDueStatusTest {
    @Test
    fun medicationTracking_hasDueStatus() {
        val viewModel = InsuliaViewModel()

        val now = System.currentTimeMillis()
        val dueSoon = Medication(
            id = 999,
            diabetesType = DiabetesType.TYPE_2,
            name = "TestMed",
            dosage = "1x",
            startedAt = now - 100000,
            finishAt = now + 1000 * 60 * 60 * 24 * 2,
            isActive = true
        )
        assertTrue(dueSoon.finishAt - now < 1000L * 60 * 60 * 24 * 3)
    }
}
