package com.example.insulia.unit

import com.example.insulia.data.InsuliaViewModel
import org.junit.Assert.assertTrue
import org.junit.Test

class BookingAnotherAppointmentMovesTheOldOneToPreviousTest {
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
}
