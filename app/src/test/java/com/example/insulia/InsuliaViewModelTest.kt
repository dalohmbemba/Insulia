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
    }

    @Test
    fun deleteReading_removesTheReading() {
        val viewModel = InsuliaViewModel()
        val first = viewModel.readings.first()

        viewModel.deleteReading(first.id)

        assertEquals(6, viewModel.readings.size)
    }

    @Test
    fun averageReading_isCalculatedFromAllReadings() {
        val viewModel = InsuliaViewModel()

        val average = viewModel.averageReading

        assertNotNull(average)
        assertTrue(average!! > 0)
    }

    @Test
    fun averageReading_isNullWhenThereAreNoReadings() {
        val viewModel = InsuliaViewModel()
        viewModel.readings.forEach { viewModel.deleteReading(it.id) }

        assertNull(viewModel.averageReading)
    }

    @Test
    fun addFoodNote_savesTheNote() {
        val viewModel = InsuliaViewModel()

        viewModel.addFoodNote(MealType.DINNER, "Grilled chicken and vegetables")

        assertEquals(3, viewModel.foodNotes.size)
        assertEquals("Grilled chicken and vegetables", viewModel.latestFoodNote?.note)
    }

    @Test
    fun bookAppointment_addsAnUpcomingAppointment() {
        val viewModel = InsuliaViewModel()

        viewModel.bookAppointment(
            patientName = "Madalo Mbemba",
            clinicName = "Queen Elizabeth Central Hospital",
            dateMillis = 1_700_000_000_000L,
            lastVisit = "18 September 2026",
            isEmergency = true
        )

        assertNotNull(viewModel.upcomingAppointment)
        assertTrue(viewModel.upcomingAppointment?.isEmergency == true)
    }

    @Test
    fun bookingAnotherAppointment_movesTheOldOneToPrevious() {
        val viewModel = InsuliaViewModel()

        viewModel.bookAppointment(
            patientName = "Madalo Mbemba",
            clinicName = "Queen Elizabeth Central Hospital",
            dateMillis = 1_700_000_000_000L,
            lastVisit = "18 September 2026",
            isEmergency = false
        )

        val firstUpcomingId = viewModel.upcomingAppointment?.id

        viewModel.bookAppointment(
            patientName = "Madalo Mbemba",
            clinicName = "Queen Elizabeth Central Hospital",
            dateMillis = 1_800_000_000_000L,
            lastVisit = "18 September 2026",
            isEmergency = true
        )

        assertTrue(viewModel.previousAppointments.any { it.id == firstUpcomingId })
        assertTrue(viewModel.upcomingAppointment?.isEmergency == true)
    }

    @Test
    fun glucoseStatus_isClassifiedCorrectly() {
        assertEquals(GlucoseStatus.LOW, GlucoseStatus.of(65))
        assertEquals(GlucoseStatus.IN_RANGE, GlucoseStatus.of(100))
        assertEquals(GlucoseStatus.HIGH, GlucoseStatus.of(150))
    }

    @Test
    fun notifications_areSeeded() {
        val viewModel = InsuliaViewModel()

        assertTrue(viewModel.notifications.isNotEmpty())
    }
}
