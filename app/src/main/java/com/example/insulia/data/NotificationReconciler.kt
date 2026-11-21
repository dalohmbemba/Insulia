package com.example.insulia.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Builds the derived AlertsScreen notifications from the patient's own records.
 *
 * The same rules are used by the ViewModel for immediate UI updates and by the
 * WorkManager reconciliation worker for durable Firestore updates, so the two
 * paths cannot drift apart.
 */
object NotificationReconciler {
    const val TYPE_GLUCOSE = "glucose"
    const val TYPE_MEDICATION = "medication"
    const val TYPE_APPOINTMENT = "appointment"

    const val GLUCOSE_NOTIFICATION_ID = "glucose_latest"
    const val MEDICATION_NOTIFICATION_PREFIX = "med_"
    const val APPOINTMENT_NOTIFICATION_PREFIX = "appt_"

    fun build(
        readings: List<GlucoseReading>,
        medications: List<Medication>,
        appointments: List<Appointment>,
        now: Long = System.currentTimeMillis()
    ): List<NotificationItem> = buildList {
        readings.maxByOrNull { it.recordedAt }?.let(::glucoseNotification)?.let(::add)
        addAll(medicationNotifications(medications, now))
        addAll(appointmentNotifications(appointments))
    }
        .distinctBy { it.type to it.relatedId }
        .sortedByDescending { it.timestamp }

    fun buildIds(
        readings: List<GlucoseReading>,
        medications: List<Medication>,
        appointments: List<Appointment>,
        now: Long = System.currentTimeMillis()
    ): Set<String> = build(readings, medications, appointments, now)
        .map { it.id }
        .toSet()

    private fun glucoseNotification(reading: GlucoseReading): NotificationItem {
        val status = when (GlucoseStatus.of(reading.valueMgDl)) {
            GlucoseStatus.HIGH -> "High"
            GlucoseStatus.LOW -> "Low"
            GlucoseStatus.IN_RANGE -> "In range"
        }
        return NotificationItem(
            id = GLUCOSE_NOTIFICATION_ID,
            title = "Latest glucose level",
            body = "${reading.valueMgDl} mg/dL - $status",
            timestamp = reading.recordedAt,
            type = TYPE_GLUCOSE,
            relatedId = reading.id.toString()
        )
    }

    private fun medicationNotifications(
        medications: List<Medication>,
        now: Long
    ): List<NotificationItem> = medications
        .filter { it.isActive }
        .mapNotNull { medication ->
            val daysLeft = TimeUnit.MILLISECONDS.toDays(medication.finishAt - now)
            val status = when {
                medication.finishAt < now -> "Overdue"
                daysLeft <= 1 -> "Due now"
                daysLeft <= 2 -> "Due soon"
                daysLeft <= 7 -> "Approaching"
                else -> return@mapNotNull null
            }

            NotificationItem(
                id = "$MEDICATION_NOTIFICATION_PREFIX${medication.id}",
                title = "Medication alert",
                body = "$status: ${medication.name} ${medication.dosage}",
                timestamp = now + medication.id % 1000,
                type = TYPE_MEDICATION,
                relatedId = medication.id.toString()
            )
        }

    private fun appointmentNotifications(
        appointments: List<Appointment>
    ): List<NotificationItem> = appointments.map { appointment ->
        val date = appointment.dateMillis?.let(::formatDate).orEmpty()
        val whenText = if (date.isNotEmpty()) " on $date" else ""
        NotificationItem(
            id = "$APPOINTMENT_NOTIFICATION_PREFIX${appointment.id}",
            title = if (appointment.isUpcoming) "Upcoming appointment" else "Appointment",
            body = if (appointment.isUpcoming) {
                "Your upcoming appointment is$whenText at ${appointment.clinicName}"
            } else {
                "Appointment$whenText at ${appointment.clinicName}"
            },
            timestamp = appointment.dateMillis ?: System.currentTimeMillis(),
            type = TYPE_APPOINTMENT,
            relatedId = appointment.id.toString()
        )
    }

    private fun formatDate(millis: Long): String =
        SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(Date(millis))
}
