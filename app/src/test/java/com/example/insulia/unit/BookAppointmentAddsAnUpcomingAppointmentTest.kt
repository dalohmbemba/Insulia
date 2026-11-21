package com.example.insulia.unit

import com.example.insulia.data.InsuliaViewModel
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BookAppointmentAddsAnUpcomingAppointmentTest {
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
}
