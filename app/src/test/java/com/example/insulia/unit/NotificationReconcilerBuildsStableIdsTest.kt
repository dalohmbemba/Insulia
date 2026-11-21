package com.example.insulia.unit

import com.example.insulia.data.Appointment
import com.example.insulia.data.DiabetesType
import com.example.insulia.data.GlucoseReading
import com.example.insulia.data.Medication
import com.example.insulia.data.NotificationReconciler
import com.example.insulia.data.ReadingContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationReconcilerBuildsStableIdsTest {
    @Test
    fun derived_notifications_use_stable_ids_and_match_buildIds() {
        val now = 1_000_000L
        val readings = listOf(
            GlucoseReading(1, 90, now - 10, ReadingContext.BEFORE_BREAKFAST),
            GlucoseReading(2, 180, now, ReadingContext.AFTER_MEAL)
        )
        val medications = listOf(
            Medication(
                id = 1,
                diabetesType = DiabetesType.TYPE_2,
                name = "Metformin",
                dosage = "500mg",
                startedAt = now - 100,
                finishAt = now + 86_400_000,
                isActive = true
            ),
            Medication(
                id = 2,
                diabetesType = DiabetesType.TYPE_2,
                name = "Old medicine",
                dosage = "1 tablet",
                startedAt = now - 100,
                finishAt = now + 86_400_000,
                isActive = false
            )
        )
        val appointments = listOf(
            Appointment(
                id = 1,
                patientName = "Test Patient",
                clinicName = "Test Clinic",
                dateMillis = now + 200_000,
                lastVisit = "",
                isEmergency = false,
                isUpcoming = true,
                commentRes = 0
            )
        )

        val notifications = NotificationReconciler.build(readings, medications, appointments, now)
        val ids = notifications.map { it.id }.toSet()

        assertEquals(
            setOf(
                NotificationReconciler.GLUCOSE_NOTIFICATION_ID,
                "${NotificationReconciler.MEDICATION_NOTIFICATION_PREFIX}1",
                "${NotificationReconciler.APPOINTMENT_NOTIFICATION_PREFIX}1"
            ),
            ids
        )
        assertFalse(ids.contains("${NotificationReconciler.MEDICATION_NOTIFICATION_PREFIX}2"))
        assertEquals(ids, NotificationReconciler.buildIds(readings, medications, appointments, now))
        assertTrue(notifications.any { it.body?.contains("180 mg/dL") == true })
    }
}
