package com.example.insulia.unit

import com.example.insulia.data.DiabetesType
import com.example.insulia.data.Medication
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MedicationRefillReminderStatusTest {
    @Test
    fun medicationRefillReminderStatus_dueAndFuture() {
        val now = System.currentTimeMillis()
        val dueSoon = Medication(
            id = 1,
            diabetesType = DiabetesType.TYPE_2,
            name = "Metformin",
            dosage = "500mg",
            startedAt = now - 100000,
            finishAt = now + 1000L * 60 * 60 * 24 * 2,
            isActive = true
        )
        val future = Medication(
            id = 2,
            diabetesType = DiabetesType.TYPE_1,
            name = "Insulin",
            dosage = "10 units",
            startedAt = now,
            finishAt = now + 1000L * 60 * 60 * 24 * 30,
            isActive = true
        )
        assertTrue(dueSoon.finishAt - now < 1000L * 60 * 60 * 24 * 3)
        assertFalse(future.finishAt - now < 1000L * 60 * 60 * 24 * 3)
    }
}
