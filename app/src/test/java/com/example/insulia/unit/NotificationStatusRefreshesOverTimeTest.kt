package com.example.insulia.unit

import com.example.insulia.data.DiabetesType
import com.example.insulia.data.Medication
import com.example.insulia.data.NotificationReconciler
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationStatusRefreshesOverTimeTest {
    private val now = 1_000_000_000L

    private fun medication(finishAt: Long) = Medication(
        id = 1,
        diabetesType = DiabetesType.TYPE_2,
        name = "Metformin",
        dosage = "500mg",
        startedAt = now - TimeUnit.DAYS.toMillis(1),
        finishAt = finishAt,
        isActive = true
    )

    @Test
    fun medication_status_text_changes_when_time_passes() {
        val finishAt = now + TimeUnit.DAYS.toMillis(2)
        val first = NotificationReconciler.build(
            readings = emptyList(),
            medications = listOf(medication(finishAt)),
            appointments = emptyList(),
            now = now
        ).single()
        val later = NotificationReconciler.build(
            readings = emptyList(),
            medications = listOf(medication(finishAt)),
            appointments = emptyList(),
            now = now + TimeUnit.DAYS.toMillis(3)
        ).single()

        assertTrue(first.body.orEmpty().contains("Due soon"))
        assertTrue(later.body.orEmpty().contains("Overdue"))
    }
}
