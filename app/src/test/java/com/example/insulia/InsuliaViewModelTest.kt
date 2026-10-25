package com.example.insulia

import com.example.insulia.data.GlucoseStatus
import com.example.insulia.data.InsuliaViewModel
import com.example.insulia.data.MealType
import com.example.insulia.data.ReadingContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for the shared state of the app.
 */
class InsuliaViewModelTest {

    @Test
    fun seededReadings_areAvailableAndLatestIs145() {
        val viewModel = InsuliaViewModel()

        assertEquals(7, viewModel.readings.size)
        assertEquals(145, viewModel.latestReading?.valueMgDl)
    }

    @Test
    fun addReading_addsReadingAndMakesItTheLatest() {
        val viewModel = InsuliaViewModel()

        viewModel.addReading(123, ReadingContext.BEFORE_MEAL)

        assertEquals(8, viewModel.readings.size)
        assertEquals(123, viewModel.latestReading?.valueMgDl)
        assertEquals(ReadingContext.BEFORE_MEAL, viewModel.latestReading?.context)
    }

    @Test
    fun deleteReading_removesTheReading() {
        val viewModel = InsuliaViewModel()
        val readingId = viewModel.readings.first().id

        viewModel.deleteReading(readingId)

        assertEquals(6, viewModel.readings.size)
        assertTrue(viewModel.readings.none { it.id == readingId })
    }

    @Test
    fun averageReading_isCalculatedFromAllReadings() {
        val viewModel = InsuliaViewModel()

        val expected = viewModel.readings.sumOf { it.valueMgDl } / viewModel.readings.size

        assertEquals(expected, viewModel.averageReading)
    }

    @Test
    fun addFoodNote_savesTheNote() {
        val viewModel = InsuliaViewModel()
        val before = viewModel.foodNotes.size

        viewModel.addFoodNote(MealType.LUNCH, "Nsima and beans")

        assertEquals(before + 1, viewModel.foodNotes.size)
        assertEquals("Nsima and beans", viewModel.latestFoodNote?.note)
        assertEquals(MealType.LUNCH, viewModel.latestFoodNote?.meal)
    }

    @Test
    fun bookAppointment_addsAnUpcomingAppointment() {
        val viewModel = InsuliaViewModel()

        viewModel.bookAppointment(
            patientName = "Madalo Mbemba",
            clinicName = "Queen Elizabeth Central Hospital",
            dateMillis = 1_700_000_000_000L,
            lastVisit = "18 September 2026",
            isEmergency = false
        )

        val upcoming = viewModel.upcomingAppointment
        assertNotNull(upcoming)
        assertEquals("Queen Elizabeth Central Hospital", upcoming?.clinicName)
        assertEquals(1_700_000_000_000L, upcoming?.dateMillis)
        assertEquals(com.example.insulia.R.string.appointments_normal_comment, upcoming?.commentRes)
    }

    @Test
    fun bookingAnotherAppointment_movesTheOldOneToPrevious() {
        val viewModel = InsuliaViewModel()

        viewModel.bookAppointment(
            patientName = "Madalo Mbemba",
            clinicName = "Clinic A",
            dateMillis = 1_700_000_000_000L,
            lastVisit = "",
            isEmergency = false
        )
        val previousCount = viewModel.previousAppointments.size

        viewModel.bookAppointment(
            patientName = "Madalo Mbemba",
            clinicName = "Clinic B",
            dateMillis = 1_800_000_000_000L,
            lastVisit = "",
            isEmergency = true
        )

        assertEquals("Clinic B", viewModel.upcomingAppointment?.clinicName)
        assertEquals(previousCount + 1, viewModel.previousAppointments.size)
        assertEquals(com.example.insulia.R.string.appointments_emergency_comment, viewModel.upcomingAppointment?.commentRes)
    }

    @Test
    fun glucoseStatus_usesTheTargetRange() {
        assertEquals(GlucoseStatus.LOW, GlucoseStatus.of(69))
        assertEquals(GlucoseStatus.IN_RANGE, GlucoseStatus.of(70))
        assertEquals(GlucoseStatus.IN_RANGE, GlucoseStatus.of(140))
        assertEquals(GlucoseStatus.HIGH, GlucoseStatus.of(141))
    }

    @Test
    fun notifications_areSeeded() {
        val viewModel = InsuliaViewModel()

        assertEquals(12, viewModel.notifications.size)
        assertNotNull(viewModel.notifications.first().titleRes)
    }

    @Test
    fun averageReading_isNullWhenThereAreNoReadings() {
        val viewModel = InsuliaViewModel()

        viewModel.readings.forEach { viewModel.deleteReading(it.id) }

        assertNull(viewModel.averageReading)
        assertNull(viewModel.latestReading)
    }
}
