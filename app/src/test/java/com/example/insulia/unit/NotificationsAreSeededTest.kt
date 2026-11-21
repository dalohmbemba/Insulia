package com.example.insulia.unit

import com.example.insulia.data.InsuliaViewModel
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationsAreSeededTest {
    @Test
    fun notifications_areSeeded() {
        val viewModel = InsuliaViewModel()

        assertTrue(viewModel.notifications.isNotEmpty())
    }
}
