package com.example.insulia.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.insulia.R
import java.util.concurrent.TimeUnit

/**
 * Holds every piece of app state.
 *
 * Previously each screen hard-coded its own values, so a reading logged on one
 * screen never appeared on another. Everything now lives here, which means the
 * home card, the trend chart, the records screen and the appointments screen
 * always show the same data.
 */
class InsuliaViewModel : ViewModel() {

    var readings by mutableStateOf<List<GlucoseReading>>(emptyList())
        private set

    var foodNotes by mutableStateOf<List<FoodNote>>(emptyList())
        private set

    var appointments by mutableStateOf<List<Appointment>>(emptyList())
        private set

    var patient by mutableStateOf(
        PatientProfile(
            fullName = "Madalo Mbemba",
            shortName = "Madalo",
            dateOfBirth = "12 June 2002 (sample)",
            patientId = "INS-00125 (sample)",
            clinic = "Queen Elizabeth Central Hospital (sample)",
            lastClinicVisit = "18 September 2026 (sample)"
        )
    )
        private set

    var notifications by mutableStateOf<List<NotificationItem>>(emptyList())
        private set

    /** Keeps the timestamps of new records strictly increasing. */
    private var lastTimestamp: Long = System.currentTimeMillis()

    init {
        seedDemoData()
    }

    /** The most recent reading, or `null` when nothing has been recorded. */
    val latestReading: GlucoseReading?
        get() = readings.maxByOrNull { it.recordedAt }

    /** The readings shown on the trend chart, newest last. */
    val recentReadings: List<GlucoseReading>
        get() = readings.sortedBy { it.recordedAt }.takeLast(MAX_TREND_POINTS)

    /** The next appointment that has been booked. */
    val upcomingAppointment: Appointment?
        get() = appointments
            .filter { it.isUpcoming }
            .minByOrNull { it.dateMillis ?: Long.MAX_VALUE }

    /** Appointments that have already taken place, newest first. */
    val previousAppointments: List<Appointment>
        get() = appointments
            .filterNot { it.isUpcoming }
            .sortedByDescending { it.dateMillis ?: Long.MAX_VALUE }

    /** The most recent food note, or `null` when none has been added. */
    val latestFoodNote: FoodNote?
        get() = foodNotes.maxByOrNull { it.recordedAt }

    /** Average of all recorded readings. */
    val averageReading: Int?
        get() = if (readings.isEmpty()) {
            null
        } else {
            readings.sumOf { it.valueMgDl } / readings.size
        }

    /** Records a new glucose reading and adds it to the trend. */
    fun addReading(valueMgDl: Int, context: ReadingContext) {
        readings = readings + GlucoseReading(
            id = nextId(),
            valueMgDl = valueMgDl,
            recordedAt = nextTimestamp(),
            context = context
        )
    }

    /** Removes a recorded reading. */
    fun deleteReading(id: Long) {
        readings = readings.filterNot { it.id == id }
    }

    /** Saves a food note. */
    fun addFoodNote(meal: MealType, note: String) {
        foodNotes = foodNotes + FoodNote(
            id = nextId(),
            meal = meal,
            note = note,
            recordedAt = nextTimestamp()
        )
    }

    /** Removes a food note. */
    fun deleteFoodNote(id: Long) {
        foodNotes = foodNotes.filterNot { it.id == id }
    }

    /** Books a new appointment and makes it the upcoming one. */
    fun bookAppointment(
        patientName: String,
        clinicName: String,
        dateMillis: Long?,
        lastVisit: String,
        isEmergency: Boolean
    ) {
        val newAppointment = Appointment(
            id = nextId(),
            patientName = patientName,
            clinicName = clinicName,
            dateMillis = dateMillis,
            lastVisit = lastVisit,
            isEmergency = isEmergency,
            isUpcoming = true,
            commentRes = if (isEmergency) {
                R.string.appointments_emergency_comment
            } else {
                R.string.appointments_normal_comment
            }
        )

        // Only one appointment can be upcoming at a time.
        appointments = appointments.map {
            if (it.isUpcoming) it.copy(isUpcoming = false) else it
        } + newAppointment
    }

    private fun nextId(): Long = System.currentTimeMillis()

    /**
     * Returns a timestamp that is always newer than the previous one.
     *
     * Two records created in the same millisecond would otherwise be ordered
     * arbitrarily, and the newest reading could be the wrong one.
     */
    private fun nextTimestamp(): Long {
        val now = System.currentTimeMillis()
        lastTimestamp = maxOf(now, lastTimestamp + 1)
        return lastTimestamp
    }

    /**
     * Loads the demo content the prototype ships with.
     *
     * Everything here is example data and is replaced by real records as soon
     * as the patient starts logging readings.
     */
    private fun seedDemoData() {
        val now = System.currentTimeMillis()

        val seedValues = listOf(120, 135, 128, 145, 138, 155, 145)
        readings = seedValues.mapIndexed { index, value ->
            val daysAgo = seedValues.size - 1 - index
            GlucoseReading(
                id = now - daysAgo,
                valueMgDl = value,
                recordedAt = now - TimeUnit.DAYS.toMillis(daysAgo.toLong()),
                context = ReadingContext.AFTER_BREAKFAST
            )
        }

        foodNotes = listOf(
            FoodNote(
                id = now - 1,
                meal = MealType.BREAKFAST,
                note = "Oats with an egg and a cup of tea",
                recordedAt = now - TimeUnit.HOURS.toMillis(5)
            ),
            FoodNote(
                id = now - 2,
                meal = MealType.LUNCH,
                note = "Nsima, vegetables and beans",
                recordedAt = now - TimeUnit.HOURS.toMillis(9)
            )
        )

        appointments = listOf(
            Appointment(
                id = now - 3,
                patientName = "Madalo Mbemba",
                clinicName = "Queen Elizabeth Central Hospital",
                dateMillis = now - TimeUnit.DAYS.toMillis(12),
                lastVisit = "18 September 2026",
                isEmergency = false,
                isUpcoming = false,
                commentRes = R.string.appointments_previous_visit_comment
            ),
            Appointment(
                id = now - 4,
                patientName = "Madalo Mbemba",
                clinicName = "Queen Elizabeth Central Hospital",
                dateMillis = now - TimeUnit.DAYS.toMillis(46),
                lastVisit = "14 August 2026",
                isEmergency = false,
                isUpcoming = false,
                commentRes = R.string.appointments_previous_visit_comment
            ),
            Appointment(
                id = now - 5,
                patientName = "Madalo Mbemba",
                clinicName = "Queen Elizabeth Central Hospital",
                dateMillis = now - TimeUnit.DAYS.toMillis(80),
                lastVisit = "11 July 2026",
                isEmergency = false,
                isUpcoming = false,
                commentRes = R.string.appointments_previous_visit_comment
            )
        )

        notifications = listOf(
            NotificationItem(R.string.notification_glucose_title, R.string.notification_glucose_body),
            NotificationItem(R.string.notification_medication_title, R.string.notification_medication_body),
            NotificationItem(R.string.notification_appointment_title, R.string.notification_appointment_body),
            NotificationItem(R.string.notification_tip_title, R.string.notification_tip_body),
            NotificationItem(R.string.notification_meal_title, R.string.notification_meal_body),
            NotificationItem(R.string.notification_record_title, R.string.notification_record_body),
            NotificationItem(R.string.notification_foot_title, R.string.notification_foot_body),
            NotificationItem(R.string.notification_followup_title, R.string.notification_followup_body),
            NotificationItem(R.string.notification_learning_title, R.string.notification_learning_body),
            NotificationItem(R.string.notification_weekly_title, R.string.notification_weekly_body),
            NotificationItem(R.string.notification_routine_title, R.string.notification_routine_body),
            NotificationItem(R.string.notification_prepare_title, R.string.notification_prepare_body)
        )
    }

    private companion object {
        const val MAX_TREND_POINTS = 7
    }
}
