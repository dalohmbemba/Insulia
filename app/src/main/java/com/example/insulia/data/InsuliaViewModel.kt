package com.example.insulia.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.insulia.R
import com.example.insulia.data.repository.PatientRepository
import com.example.insulia.data.work.InsuliaWorkScheduler
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Holds every piece of app state.
 *
 * Data is read from and written to Firebase Firestore. The offline cache is
 * enabled in [com.example.insulia.InsuliaApplication], so records created
 * without a connection are queued and synchronised automatically.
 *
 * Demo content is only used as an in-memory fallback when Firebase cannot be
 * reached at all (for example in unit tests). It is never written to a real
 * patient's Firestore account.
 */
class InsuliaViewModel(
    private val repository: PatientRepository = PatientRepository()
) : ViewModel() {

    var readings by mutableStateOf<List<GlucoseReading>>(emptyList())
        private set

    var foodNotes by mutableStateOf<List<FoodNote>>(emptyList())
        private set

    var appointments by mutableStateOf<List<Appointment>>(emptyList())
        private set

    var patient by mutableStateOf(emptyPatient())
        private set

    var notifications by mutableStateOf<List<NotificationItem>>(emptyList())
        private set

    var medications by mutableStateOf<List<Medication>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    /** A message shown to the patient when something could not be saved. */
    var dataError by mutableStateOf<String?>(null)
        private set

    /** A short confirmation shown after a successful save. */
    var dataMessage by mutableStateOf<String?>(null)
        private set

    // Account state -------------------------------------------------------

    var accountEmail by mutableStateOf<String?>(null)
        private set

    var isAnonymousAccount by mutableStateOf(true)
        private set

    var authBusy by mutableStateOf(false)
        private set

    /** True once Firebase has restored or established the current session. */
    var isAuthenticated by mutableStateOf(false)
        private set

    /** Authentication errors stay on the welcome screen so they can be retried. */
    var authError by mutableStateOf<String?>(null)
        private set

    var authFormError by mutableStateOf<String?>(null)
        private set

    fun clearAuthFormError() {
        authFormError = null
    }

    /** Keeps generated timestamps strictly increasing. */
    private var lastTimestamp: Long = System.currentTimeMillis()

    /** Keeps generated document ids unique even within the same millisecond. */
    private var lastGeneratedId: Long = 0L

    /** Jobs that listen to Firestore, restarted after an account switch. */
    private val syncJobs = mutableListOf<Job>()

    /** Collections that have delivered their first snapshot in this session. */
    private val loadedNotificationTypes = mutableSetOf<String>()

    /** Incremented when the listeners are pointed at a different account. */
    private var syncGeneration = 0

    /**
     * Guards against running overlapping Patient ID transactions. The patient
     * ID is created and reserved in Firestore, so concurrent attempts for the
     * same account would only contend with each other.
     */
    private var patientIdRequested = false

    /** The ID this account has already confirmed and reserved in Firestore. */
    private var ensuredPatientId: String? = null

    /** Avoids queuing the same startup reconciliation for every first snapshot. */
    private var initialNotificationReconcileEnqueued = false

    init {
        refreshAccountState()
        if (repository.isAuthAvailable && repository.isSignedIn) {
            // Firebase restores persisted sessions itself. Reuse that session
            // rather than creating another anonymous account on every launch.
            isAuthenticated = true
            startSync()
        } else if (!repository.isAvailable) {
            // Unit tests and builds without Firebase configuration retain the
            // in-memory fixture data, but do not silently bypass sign-in.
            seedDemoData()
        }
    }

    /** Starts anonymous authentication after the patient chooses Get Started. */
    fun authenticateForOnboarding() {
        if (authBusy || isAuthenticated) return
        authBusy = true
        authError = null
        viewModelScope.launch {
            try {
                repository.ensureSignedIn()
                refreshAccountState()
                isAuthenticated = true
                startSync()
            } catch (t: Throwable) {
                authError = t.message?.takeIf { it.isNotBlank() }
                    ?: "Could not sign in. Check your connection and try again."
            } finally {
                authBusy = false
            }
        }
    }

    fun clearAuthError() {
        authError = null
    }

    // ---------------------------------------------------------------------
    // Synchronisation
    // ---------------------------------------------------------------------

    private fun startSync() {
        observe(repository.observeProfile(), "profile") { profile ->
            patient = profile ?: emptyPatient()
            if (profile != null) {
                // A blank ID is assigned. A profile that already has an ID is
                // checked once so an older ID gets a reservation and cannot be
                // claimed by another patient.
                requestPatientId(profile.patientId)
            }
        }

        observe(repository.observeReadings(), "readings") { remote ->
            readings = remote
            onNotificationSourceLoaded(NotificationReconciler.TYPE_GLUCOSE)
        }

        observe(repository.observeFoodNotes(), "food notes") { remote ->
            foodNotes = remote
        }

        observe(repository.observeMedications(), "medications") { remote ->
            medications = remote
            onNotificationSourceLoaded(NotificationReconciler.TYPE_MEDICATION)
        }

        observe(repository.observeAppointments(), "appointments") { remote ->
            appointments = remote
            onNotificationSourceLoaded(NotificationReconciler.TYPE_APPOINTMENT)
        }

        observe(repository.observeNotifications(), "notifications") { remote ->
            notifications = remote
        }
    }

    private fun onNotificationSourceLoaded(type: String) {
        loadedNotificationTypes += type
        // First snapshots only seed the UI. One durable reconciliation is
        // queued after all source collections have arrived.
        regenerateNotifications(enqueueWork = false)
        maybeEnqueueInitialNotificationReconcile()
    }

    private fun allNotificationSourcesLoaded(): Boolean = loadedNotificationTypes.containsAll(
        listOf(
            NotificationReconciler.TYPE_GLUCOSE,
            NotificationReconciler.TYPE_MEDICATION,
            NotificationReconciler.TYPE_APPOINTMENT
        )
    )

    private fun maybeEnqueueInitialNotificationReconcile() {
        if (initialNotificationReconcileEnqueued || !allNotificationSourcesLoaded()) return
        initialNotificationReconcileEnqueued = true
        enqueueNotificationReconcile()
    }

    /**
     * Collects one Firestore flow on its own coroutine.
     *
     * Each flow is collected separately because a Firestore snapshot listener
     * never completes; collecting them sequentially would stop after the first
     * collection.
     */
    private fun <T> observe(flow: Flow<T>, label: String, onValue: (T) -> Unit) {
        val generation = syncGeneration
        val job = viewModelScope.launch {
            try {
                flow.collect {
                    // Ignore a late delivery from the previous account after
                    // the patient signed in to a different one.
                    if (generation == syncGeneration) {
                        onValue(it)
                    }
                }
            } catch (t: CancellationException) {
                throw t
            } catch (t: Throwable) {
                reportError("Could not load $label", t)
            }
        }
        syncJobs += job
    }

    private fun restartSync() {
        syncGeneration++
        syncJobs.forEach { it.cancel() }
        syncJobs.clear()
        loadedNotificationTypes.clear()
        initialNotificationReconcileEnqueued = false
        patientIdRequested = false
        ensuredPatientId = null
        startSync()
    }

    private fun enqueuePatientIdWork() {
        if (!repository.isPatientIdAvailable) return
        val uid = repository.accountUid ?: return
        InsuliaWorkScheduler.enqueueEnsurePatientId(uid)
    }

    private fun enqueueNotificationReconcile() {
        if (!repository.isAvailable) return
        val uid = repository.accountUid ?: return
        InsuliaWorkScheduler.enqueueReconcileNotifications(uid)
    }

    // ---------------------------------------------------------------------
    // Account management
    // ---------------------------------------------------------------------

    private fun refreshAccountState() {
        accountEmail = repository.accountEmail
        isAnonymousAccount = repository.isAnonymousAccount
    }

    fun signOut() {
        syncGeneration++
        syncJobs.forEach { it.cancel() }
        syncJobs.clear()
        repository.signOut()
        isAuthenticated = false
        accountEmail = null
        isAnonymousAccount = true
        patient = emptyPatient()
        readings = emptyList()
        foodNotes = emptyList()
        appointments = emptyList()
        medications = emptyList()
        notifications = emptyList()
    }

    fun linkAccount(email: String, password: String) {
        val validation = validateCredentials(email, password)
        if (validation != null) {
            authFormError = validation
            return
        }
        if (authBusy) return
        authBusy = true
        viewModelScope.launch {
            try {
                repository.linkAccount(email.trim(), password).fold(
                    onSuccess = {
                        refreshAccountState()
                        isAuthenticated = true
                        restartSync()
                        dataMessage = "Account created. Your records are safe."
                    },
                    onFailure = { authFormError = it.message ?: "Could not create the account" }
                )
            } finally {
                authBusy = false
            }
        }
    }

    fun signInToAccount(email: String, password: String) {
        val validation = validateCredentials(email, password)
        if (validation != null) {
            authFormError = validation
            return
        }
        if (authBusy) return
        authBusy = true
        viewModelScope.launch {
            try {
                repository.signInToAccount(email.trim(), password).fold(
                    onSuccess = {
                        refreshAccountState()
                        isAuthenticated = true
                        // A different account has a different UID, so the
                        // listeners must be pointed at the new document tree.
                        patient = emptyPatient()
                        readings = emptyList()
                        foodNotes = emptyList()
                        appointments = emptyList()
                        medications = emptyList()
                        notifications = emptyList()
                        restartSync()
                        dataMessage = "Signed in. Loading your records."
                    },
                    onFailure = { authFormError = it.message ?: "Could not sign in" }
                )
            } finally {
                authBusy = false
            }
        }
    }

    fun sendPasswordReset(email: String) {
        if (email.isBlank() || !email.contains("@")) {
            authFormError = "Enter a valid email address"
            return
        }
        if (authBusy) return
        authBusy = true
        viewModelScope.launch {
            try {
                repository.sendPasswordReset(email.trim()).fold(
                    onSuccess = {
                        authFormError = null
                        dataMessage = "Password reset email sent"
                    },
                    onFailure = { authFormError = it.message ?: "Could not send the reset email" }
                )
            } finally {
                authBusy = false
            }
        }
    }

    private fun validateCredentials(email: String, password: String): String? = when {
        email.isBlank() || !email.contains("@") -> "Enter a valid email address"
        password.length < 6 -> "Choose a password with at least 6 characters"
        else -> null
    }

    // ---------------------------------------------------------------------
    // Profile
    // ---------------------------------------------------------------------

    fun updateProfile(profile: PatientProfile) {
        // Patient IDs are generated by the app and immutable once assigned.
        // Ignore any value the caller supplies and keep the one already on
        // record (including an ID that was just reserved but has not reached
        // the profile listener yet).
        val safeProfile = profile.copy(
            patientId = patient.patientId.ifBlank { ensuredPatientId.orEmpty() }
        )
        try {
            patient = safeProfile
            viewModelScope.launch {
                try {
                    repository.saveProfile(safeProfile)
                    dataMessage = "Profile updated"
                    if (safeProfile.patientId.isBlank()) {
                        requestPatientId(safeProfile.patientId)
                    }
                } catch (t: Throwable) {
                    reportError("Could not save your profile", t)
                }
            }
        } catch (t: Throwable) {
            reportError("Could not save your profile", t)
        }
    }

    /**
     * Queues Patient ID assignment/reservation with WorkManager.
     *
     * The transaction itself still runs in
     * [com.example.insulia.data.remote.FirestoreDataSource], but retries are now
     * owned by WorkManager so they survive process death. A profile with an
     * existing ID is queued once so legacy IDs get reservations too.
     */
    private fun requestPatientId(profileId: String) {
        if (!repository.isPatientIdAvailable) return

        if (profileId.isNotBlank()) {
            if (profileId == ensuredPatientId) return
            ensuredPatientId = profileId
            enqueuePatientIdWork()
            return
        }

        // A stale blank snapshot can arrive after a successful assignment.
        if (patientIdRequested || ensuredPatientId != null) return
        patientIdRequested = true
        enqueuePatientIdWork()
    }

    // ---------------------------------------------------------------------
    // Readings
    // ---------------------------------------------------------------------

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

    /** Average of all recorded readings. */
    val averageReading: Int?
        get() = if (readings.isEmpty()) {
            null
        } else {
            readings.sumOf { it.valueMgDl } / readings.size
        }

    /** Records a new glucose reading and adds it to the trend. */
    fun addReading(valueMgDl: Int, context: ReadingContext) {
        if (valueMgDl < MIN_READING || valueMgDl > MAX_READING) {
            reportError("Enter a glucose value between $MIN_READING and $MAX_READING mg/dL")
            return
        }
        val now = System.currentTimeMillis()
        val duplicate = readings.any {
            it.valueMgDl == valueMgDl && now - it.recordedAt in 0..DUPLICATE_WINDOW_MS
        }
        if (duplicate) {
            reportError("That reading has already been saved")
            return
        }
        try {
            val reading = GlucoseReading(
                id = nextId(),
                valueMgDl = valueMgDl,
                recordedAt = nextTimestamp(),
                context = context
            )
            readings = readings + reading
            viewModelScope.launch {
                try {
                    repository.addReading(reading)
                    dataMessage = "Reading saved"
                    regenerateNotifications()
                } catch (t: Throwable) {
                    reportError("Could not save the reading", t)
                }
            }
        } catch (t: Throwable) {
            reportError("Could not save the reading", t)
        }
    }

    /** Removes a recorded reading. */
    fun deleteReading(id: Long) {
        try {
            readings = readings.filterNot { it.id == id }
            viewModelScope.launch {
                try {
                    repository.deleteReading(id)
                    dataMessage = "Reading deleted"
                    regenerateNotifications()
                    if (readings.isEmpty()) {
                        repository.deleteNotification(NotificationReconciler.GLUCOSE_NOTIFICATION_ID)
                    }
                } catch (t: Throwable) {
                    reportError("Could not delete the reading", t)
                }
            }
        } catch (t: Throwable) {
            reportError("Could not delete the reading", t)
        }
    }

    // ---------------------------------------------------------------------
    // Food notes
    // ---------------------------------------------------------------------

    /** The most recent food note, or `null` when none has been added. */
    val latestFoodNote: FoodNote?
        get() = foodNotes.maxByOrNull { it.recordedAt }

    /** Saves a food note. */
    fun addFoodNote(meal: MealType, note: String) {
        if (!isValidFoodNote(note)) {
            reportError("Enter a short description of the food you ate")
            return
        }
        val trimmed = note.trim()
        val now = System.currentTimeMillis()
        val duplicate = foodNotes.any {
            it.meal == meal && it.note.equals(trimmed, ignoreCase = true) &&
                now - it.recordedAt in 0..DUPLICATE_WINDOW_MS
        }
        if (duplicate) {
            reportError("That food note has already been saved")
            return
        }
        try {
            val foodNote = FoodNote(
                id = nextId(),
                meal = meal,
                note = trimmed,
                recordedAt = nextTimestamp()
            )
            foodNotes = foodNotes + foodNote
            viewModelScope.launch {
                try {
                    repository.addFoodNote(foodNote)
                    dataMessage = "Food note saved"
                } catch (t: Throwable) {
                    reportError("Could not save the food note", t)
                }
            }
        } catch (t: Throwable) {
            reportError("Could not save the food note", t)
        }
    }

    /** Removes a food note from both the screen and Firestore. */
    fun deleteFoodNote(id: Long) {
        try {
            foodNotes = foodNotes.filterNot { it.id == id }
            viewModelScope.launch {
                try {
                    repository.deleteFoodNote(id)
                    dataMessage = "Food note deleted"
                } catch (t: Throwable) {
                    reportError("Could not delete the food note", t)
                }
            }
        } catch (t: Throwable) {
            reportError("Could not delete the food note", t)
        }
    }

    private fun isValidFoodNote(text: String): Boolean {
        val trimmed = text.trim()
        return trimmed.isNotEmpty() && trimmed.any { it.isLetter() }
    }

    // ---------------------------------------------------------------------
    // Medications
    // ---------------------------------------------------------------------

    fun addOrUpdateMedication(medication: Medication) {
        val normalized = medication.copy(
            name = medication.name.trim(),
            dosage = medication.dosage.trim()
        )
        if (normalized.name.isEmpty() || normalized.dosage.isEmpty()) {
            reportError("Enter the medication name and dosage")
            return
        }
        val existing = medications.firstOrNull { it.id == normalized.id }
        if (existing == null) {
            val duplicate = medications.any {
                it.name.equals(normalized.name, ignoreCase = true) &&
                    it.dosage.equals(normalized.dosage, ignoreCase = true) &&
                    it.startedAt == normalized.startedAt &&
                    it.finishAt == normalized.finishAt
            }
            if (duplicate) {
                reportError("That medication has already been saved")
                return
            }
        }
        val updated = if (existing == null) {
            medications + normalized
        } else {
            medications.map { if (it.id == normalized.id) normalized else it }
        }
        medications = updated
        viewModelScope.launch {
            try {
                repository.saveMedication(normalized)
                dataMessage = if (existing == null) "Medication added" else "Medication updated"
                regenerateNotifications()
                if (!normalized.isActive) {
                    repository.deleteNotification("${NotificationReconciler.MEDICATION_NOTIFICATION_PREFIX}${normalized.id}")
                }
            } catch (t: Throwable) {
                reportError("Could not save the medication", t)
            }
        }
    }

    fun setMedicationActive(id: Long, active: Boolean) {
        val medication = medications.firstOrNull { it.id == id } ?: return
        addOrUpdateMedication(medication.copy(isActive = active))
    }

    fun deleteMedication(id: Long) {
        try {
            medications = medications.filterNot { it.id == id }
            viewModelScope.launch {
                try {
                    repository.deleteMedication(id)
                    repository.deleteNotification("${NotificationReconciler.MEDICATION_NOTIFICATION_PREFIX}$id")
                    dataMessage = "Medication deleted"
                    regenerateNotifications()
                } catch (t: Throwable) {
                    reportError("Could not delete the medication", t)
                }
            }
        } catch (t: Throwable) {
            reportError("Could not delete the medication", t)
        }
    }

    /** Id used when the patient adds a medication from the records screen. */
    fun newMedication(): Medication {
        val now = System.currentTimeMillis()
        return Medication(
            id = nextId(),
            diabetesType = patient.diabetesType,
            name = "",
            dosage = "",
            startedAt = now,
            finishAt = now + TimeUnit.DAYS.toMillis(30),
            isActive = true
        )
    }

    // ---------------------------------------------------------------------
    // Appointments
    // ---------------------------------------------------------------------

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

    /** Books a new appointment and makes it the upcoming one. */
    fun bookAppointment(
        patientName: String,
        clinicName: String,
        dateMillis: Long?,
        lastVisit: String,
        isEmergency: Boolean
    ) {
        if (dateMillis == null) {
            reportError("Choose an appointment date")
            return
        }
        if (patientName.isBlank() || clinicName.isBlank()) {
            reportError("Enter the patient name and clinic name")
            return
        }
        val duplicate = appointments.any {
            it.patientName.equals(patientName.trim(), ignoreCase = true) &&
                it.clinicName.equals(clinicName.trim(), ignoreCase = true) &&
                it.dateMillis == dateMillis
        }
        if (duplicate) {
            reportError("That appointment has already been booked")
            return
        }
        try {
            val newAppointment = Appointment(
                id = nextId(),
                patientName = patientName.trim(),
                clinicName = clinicName.trim(),
                dateMillis = dateMillis,
                lastVisit = lastVisit.trim(),
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

            viewModelScope.launch {
                try {
                    repository.saveAppointments(updatedAppointments)
                    dataMessage = "Appointment booked"
                    regenerateNotifications()
                } catch (t: Throwable) {
                    reportError("Could not save the appointment", t)
                }
            }
        } catch (t: Throwable) {
            reportError("Could not book the appointment", t)
        }
    }

    /** Changes the date or details of an existing appointment. */
    fun rescheduleAppointment(
        id: Long,
        patientName: String,
        clinicName: String,
        dateMillis: Long?,
        lastVisit: String,
        isEmergency: Boolean
    ) {
        val existing = appointments.firstOrNull { it.id == id }
        if (existing == null) {
            reportError("That appointment no longer exists")
            return
        }
        if (dateMillis == null) {
            reportError("Choose an appointment date")
            return
        }
        val updated = existing.copy(
            patientName = patientName.trim(),
            clinicName = clinicName.trim(),
            dateMillis = dateMillis,
            lastVisit = lastVisit.trim(),
            isEmergency = isEmergency,
            commentRes = if (isEmergency) {
                R.string.appointments_emergency_comment
            } else {
                R.string.appointments_normal_comment
            }
        )
        val updatedAppointments = appointments.map {
            when {
                it.id == id -> updated.copy(isUpcoming = true)
                else -> it.copy(isUpcoming = false)
            }
        }
        appointments = updatedAppointments
        viewModelScope.launch {
            try {
                repository.saveAppointments(updatedAppointments)
                dataMessage = "Appointment updated"
                regenerateNotifications()
            } catch (t: Throwable) {
                reportError("Could not update the appointment", t)
            }
        }
    }

    fun deleteAppointment(id: Long) {
        try {
            appointments = appointments.filterNot { it.id == id }
            viewModelScope.launch {
                try {
                    repository.deleteAppointment(id)
                    repository.deleteNotification("${NotificationReconciler.APPOINTMENT_NOTIFICATION_PREFIX}$id")
                    dataMessage = "Appointment deleted"
                    regenerateNotifications()
                } catch (t: Throwable) {
                    reportError("Could not delete the appointment", t)
                }
            }
        } catch (t: Throwable) {
            reportError("Could not delete the appointment", t)
        }
    }

    // ---------------------------------------------------------------------
    // Notifications (derived from the patient's own records)
    // ---------------------------------------------------------------------

    /**
     * Recomputes the alerts shown in the UI and optionally queues a durable
     * reconciliation. Firestore writes are handled by WorkManager so they
     * survive process death.
     */
    private fun regenerateNotifications(enqueueWork: Boolean = true) {
        try {
            val readStates = notifications.associate { it.id to it.isRead }
            notifications = NotificationReconciler.build(
                readings = readings,
                medications = medications,
                appointments = appointments
            ).map { item -> item.copy(isRead = readStates[item.id] ?: item.isRead) }
            if (enqueueWork) enqueueNotificationReconcile()
        } catch (t: Throwable) {
            reportError("Could not update notifications", t)
        }
    }

    fun markNotificationRead(id: String) {
        val previous = notifications.firstOrNull { it.id == id } ?: return
        if (previous.isRead) return
        val updated = previous.copy(isRead = true)
        notifications = notifications.map { if (it.id == id) updated else it }

        viewModelScope.launch {
            try {
                repository.saveNotifications(listOf(updated))
            } catch (t: Throwable) {
                notifications = notifications.map {
                    if (it.id == id && it.isRead == updated.isRead) previous else it
                }
                reportError("Could not update notification status", t)
            }
        }
    }

    // ---------------------------------------------------------------------
    // Messages
    // ---------------------------------------------------------------------

    fun clearDataError() {
        dataError = null
    }

    fun clearDataMessage() {
        dataMessage = null
    }

    private fun reportError(message: String, throwable: Throwable? = null) {
        // Keep the first error so a failing listener cannot overwrite a more
        // specific message from the save that just failed.
        if (dataError == null) {
            dataError = message
        }
        if (throwable != null) {
            try {
                android.util.Log.w("Insulia", message, throwable)
            } catch (_: Throwable) {
                // Logging is unavailable in plain JVM tests.
            }
        }
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private fun nextId(): Long {
        val now = System.currentTimeMillis()
        lastGeneratedId = if (now > lastGeneratedId) now else lastGeneratedId + 1
        return lastGeneratedId
    }

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
     * In-memory fallback used only when Firebase is unavailable.
     *
     * This content is never uploaded to Firestore and never replaces an
     * existing patient profile.
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

        // Notifications are derived from the seeded records instead of being
        // hardcoded, and they stay in memory only.
        notifications = NotificationReconciler.build(
            readings = readings,
            medications = medications,
            appointments = appointments,
            now = now
        )

        // The seeded records use `now`, so new records must start after it.
        // Otherwise a record created in the same millisecond would share the
        // id of a seeded record and could be ordered as the older one.
        lastGeneratedId = now
        lastTimestamp = now
    }

    private fun emptyPatient() = PatientProfile(
        fullName = "",
        shortName = "",
        dateOfBirth = "",
        patientId = "",
        clinic = "",
        lastClinicVisit = "",
        diabetesType = DiabetesType.TYPE_2
    )

    private companion object {
        const val MAX_TREND_POINTS = 8
        const val MIN_READING = 20
        const val MAX_READING = 600
        const val DUPLICATE_WINDOW_MS = 10_000L
    }
}
