package com.example.insulia.unit

import com.example.insulia.data.NotificationItem
import com.example.insulia.data.remote.NotificationItemDto
import com.example.insulia.data.remote.toDto
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationReadStateIsPreservedTest {
    @Test
    fun read_state_survives_notification_dto_round_trip() {
        val readNotification = NotificationItem(id = "glucose_latest", title = "Glucose", isRead = true)
        val unreadNotification = NotificationItem(id = "med_1", title = "Medication")

        assertTrue(readNotification.toDto().toModel("fallback").isRead)
        assertFalse(unreadNotification.toDto().toModel("fallback").isRead)
    }

    @Test
    fun legacy_notification_dto_defaults_to_unread() {
        assertFalse(NotificationItemDto(id = "legacy").toModel("fallback").isRead)
    }
}
