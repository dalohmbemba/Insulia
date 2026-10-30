package com.example.insulia.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.insulia.R
import com.example.insulia.data.repository.PatientRepository
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Holds every piece of app state.
 *
 * Everything now syncs with Firebase Firestore (with offline persistence) so
 * readings, food notes, appointments and medications persist across restarts.
 */
class InsuliaViewModel(
    private val repository: PatientRepository = PatientRepository()
) : ViewModel() {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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
            lastClinicVisit = "18 September 2026 (sample)",
            diabetesType = DiabetesType.TYPE_2
        )
    )
        private set

    var notifications by mutableStateOf<List<NotificationItem>>(emptyList())
        private set

    var medications by mutableStateOf<List<Medication>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var dataError by mutableStateOf<String?>(null)
        private set

    /** Keeps the timestamps of new records strictly increasing. */
    private var lastTimestamp: Long = System.currentTimeMillis()

    init {
        try {
            seedDemoData()
            if (repository.isAvailable) {
                viewModelScope.launch {
                    try {
                        // Enable offline persistence if possible
                        try {
                            val firestoreClass = Class.forName("com.google.firebase.firestore.FirebaseFirestore")
                            val getInstanceFs = firestoreClass.getMethod("getInstance")
                            val instance = getInstanceFs.invoke(null)
                            val settingsClass = Class.forName("com.google.firebase.firestore.FirebaseFirestoreSettings")
                            val builderClass = Class.forName("com.google.firebase.firestore.FirebaseFirestoreSettings\$Builder")
                            val builder = builderClass.getConstructor().newInstance()
                            builderClass.getMethod("setPersistenceEnabled", Boolean::class.java).invoke(builder, true)
                            val settings = builderClass.getMethod("build").invoke(builder)
                            firestoreClass.getMethod("setFirestoreSettings", settingsClass).invoke(instance, settings)
                        } catch (_: Throwable) {
                        }

                        try {
                            repository.observeProfile().collect { patient = it }
                        } catch (_: Throwable) {
                        }
                        try {
                            repository.observeReadings().collect { readings = it }
                        } catch (_: Throwable) {
                        }
                        try {
                            repository.observeFoodNotes().collect { foodNotes = it }
                        } catch (_: Throwable) {
                        }
                        try {
                            repository.observeAppointments().collect { appointments = it }
                        } catch (_: Throwable) {
                        }
                        try {
                            repository.observeMedications().collect { medications = it }
                        } catch (_: Throwable) {
                        }
                        try {
                            repository.observeNotifications().collect { notifications = it }
                        } catch (_: Throwable) {
                        }
                        try {
                            seedDemoDataIfNeeded()
                        } catch (_: Throwable) {
                        }
                    } catch (_: Throwable) {
                    }
                }
            }
        } catch (_: Throwable) {
            try {
                seedDemoData()
            } catch (_: Throwable) {
            }
        }
    }

    /**
     * Fallback for unit tests (in-memory only).
     */
    private fun seedDemoData() {
        val now = System.currentTimeMillis()

        val seedValues = listOf(120, 135, 128, 145, 138, 155, 145)
        readings = seedValues.mapIndexed { index, value ->
            val weeksAgo = seedValues.size - 1 - index
            GlucoseReading(
                id = now - weeksAgo,
                valueMgDl = value,
                recordedAt = now - TimeUnit.DAYS.toMillis((weeksAgo * 7).toLong()),
                context = ReadingContext.AFTER_BREAKFAST
            )
        }

        medications = listOf(
            Medication(
                id = now - 100,
                diabetesType = DiabetesType.TYPE_2,
                name = "Metformin",
                dosage = "500mg twice daily",
                startedAt = now - TimeUnit.DAYS.toMillis(45),
                finishAt = now + TimeUnit.DAYS.toMillis(30),
                isActive = true
            ),
            Medication(
                id = now - 101,
                diabetesType = DiabetesType.TYPE_2,
                name = "Glibenclamide",
                dosage = "5mg once daily (before breakfast)",
                startedAt = now - TimeUnit.DAYS.toMillis(30),
                finishAt = now + TimeUnit.DAYS.toMillis(15),
                isActive = true
            ),
            Medication(
                id = now - 102,
                diabetesType = DiabetesType.TYPE_1,
                name = "NPH insulin",
                dosage = "14 units subcutaneously at bedtime",
                startedAt = now - TimeUnit.DAYS.toMillis(60),
                finishAt = now + TimeUnit.DAYS.toMillis(7),
                isActive = true
            )
        )

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

    /** The most recent reading, or `null` when nothing has been recorded. */
    val latestReading: GlucoseReading?
        get() = readings.maxByOrNull { it.recordedAt }

    /** The readings shown on the trend chart, weekly (at least 4 times per month). */
    val recentReadings: List<GlucoseReading>
        get() {
            val calendar = java.util.Calendar.getInstance()
            val weekly = readings
                .groupBy { reading ->
                    calendar.timeInMillis = reading.recordedAt
                    calendar.set(java.util.Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                    calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                    calendar.set(java.util.Calendar.MINUTE, 0)
                    calendar.set(java.util.Calendar.SECOND, 0)
                    calendar.set(java.util.Calendar.MILLISECOND, 0)
                    calendar.timeInMillis
                }
                .map { (_, weekReadings) ->
                    weekReadings.maxByOrNull { it.recordedAt }!!
                }
                .sortedBy { it.recordedAt }
                .takeLast(MAX_TREND_POINTS)
            return if (weekly.isEmpty()) {
                readings.sortedBy { it.recordedAt }.takeLast(MAX_TREND_POINTS)
            } else {
                weekly
            }
        }

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
        val reading = GlucoseReading(
            id = nextId(),
            valueMgDl = valueMgDl,
            recordedAt = nextTimestamp(),
            context = context
        )
        readings = readings + reading
        try {
            scope.launch {
                repository.addReading(reading)
            }
        } catch (_: Throwable) {
        }
    }

    /** Removes a recorded reading. */
    fun deleteReading(id: Long) {
        readings = readings.filterNot { it.id == id }
        try {
            scope.launch {
                repository.deleteReading(id)
            }
        } catch (_: Throwable) {
        }
    }

    /** Saves a food note. */
    fun addFoodNote(meal: MealType, note: String) {
        val foodNote = FoodNote(
            id = nextId(),
            meal = meal,
            note = note,
            recordedAt = nextTimestamp()
        )
        foodNotes = foodNotes + foodNote
        try {
            scope.launch {
                repository.addFoodNote(foodNote)
            }
        } catch (_: Throwable) {
        }
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
        val updatedAppointments = appointments.map {
            if (it.isUpcoming) it.copy(isUpcoming = false) else it
        } + newAppointment
        appointments = updatedAppointments
        try {
            scope.launch {
                repository.saveAppointments(updatedAppointments)
            }
        } catch (_: Throwable) {
        }
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
        lastTimestamp = if (now > lastTimestamp) now else lastTimestamp + 1
        return lastTimestamp
    }

    /**
     * Seeds demo content the first time a user signs in (when nothing exists).
     */
    private suspend fun seedDemoDataIfNeeded() {
        try {
            val now = System.currentTimeMillis()

            var didSeed = false

            if (readings.isEmpty()) {
                val seedValues = listOf(120, 135, 128, 145, 138, 155, 145)
                val seededReadings = seedValues.mapIndexed { index, value ->
                    val weeksAgo = seedValues.size - 1 - index
                    GlucoseReading(
                        id = now - weeksAgo,
                        valueMgDl = value,
                        recordedAt = now - TimeUnit.DAYS.toMillis((weeksAgo * 7).toLong()),
                        context = ReadingContext.AFTER_BREAKFAST
                    )
                }
                readings = seededReadings
                try {
                    seededReadings.forEach { repository.addReading(it) }
                } catch (_: Throwable) {
                }
                didSeed = true
            }

            if (medications.isEmpty()) {
                val seededMedications = listOf(
                    Medication(
                        id = now - 100,
                        diabetesType = DiabetesType.TYPE_2,
                        name = "Metformin",
                        dosage = "500mg twice daily",
                        startedAt = now - TimeUnit.DAYS.toMillis(45),
                        finishAt = now + TimeUnit.DAYS.toMillis(30),
                        isActive = true
                    ),
                    Medication(
                        id = now - 101,
                        diabetesType = DiabetesType.TYPE_2,
                        name = "Glibenclamide",
                        dosage = "5mg once daily (before breakfast)",
                        startedAt = now - TimeUnit.DAYS.toMillis(30),
                        finishAt = now + TimeUnit.DAYS.toMillis(15),
                        isActive = true
                    ),
                    Medication(
                        id = now - 102,
                        diabetesType = DiabetesType.TYPE_1,
                        name = "NPH insulin",
                        dosage = "14 units subcutaneously at bedtime",
                        startedAt = now - TimeUnit.DAYS.toMillis(60),
                        finishAt = now + TimeUnit.DAYS.toMillis(7),
                        isActive = true
                    )
                )
                medications = seededMedications
                try {
                    repository.saveMedications(seededMedications)
                } catch (_: Throwable) {
                }
                didSeed = true
            }

            if (foodNotes.isEmpty()) {
                val seededFoodNotes = listOf(
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
                foodNotes = seededFoodNotes
                try {
                    seededFoodNotes.forEach { repository.addFoodNote(it) }
                } catch (_: Throwable) {
                }
                didSeed = true
            }

            if (appointments.isEmpty()) {
                val seededAppointments = listOf(
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
                appointments = seededAppointments
                try {
                    repository.saveAppointments(seededAppointments)
                } catch (_: Throwable) {
                }
                didSeed = true
            }

            if (notifications.isEmpty()) {
                val seededNotifications = listOf(
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
                notifications = seededNotifications
                try {
                    repository.saveNotifications(seededNotifications)
                } catch (_: Throwable) {
                }
                didSeed = true
            }

            val defaultProfile = PatientProfile(
                fullName = "Madalo Mbemba",
                shortName = "Madalo",
                dateOfBirth = "12 June 2002 (sample)",
                patientId = "INS-00125 (sample)",
                clinic = "Queen Elizabeth Central Hospital (sample)",
                lastClinicVisit = "18 September 2026 (sample)",
                diabetesType = DiabetesType.TYPE_2
            )
            try {
                repository.saveProfile(defaultProfile)
            } catch (_: Throwable) {
            }

            isLoading = false
        } catch (_: Throwable) {
            seedDemoData()
            isLoading = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }

    private companion object {
        const val MAX_TREND_POINTS = 8
    }
}
