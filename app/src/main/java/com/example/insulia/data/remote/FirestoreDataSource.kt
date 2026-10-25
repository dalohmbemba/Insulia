package com.example.insulia.data.remote

import com.example.insulia.data.Appointment
import com.example.insulia.data.DiabetesType
import com.example.insulia.data.FoodNote
import com.example.insulia.data.GlucoseReading
import com.example.insulia.data.MealType
import com.example.insulia.data.Medication
import com.example.insulia.data.NotificationItem
import com.example.insulia.data.PatientProfile
import com.example.insulia.data.ReadingContext
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await

/**
 * Low level Firestore operations for the patient's data.
 *
 * All collections are stored under `/users/{uid}/...` so that multiple
 * accounts can share the same app without leaking data between them. Anonymous
 * authentication is used so the patient does not have to sign in explicitly.
 */
class FirestoreDataSource(
    private val firestore: FirebaseFirestore? = try { FirebaseFirestore.getInstance() } catch (_: Throwable) { null },
    private val auth: FirebaseAuth? = try { FirebaseAuth.getInstance() } catch (_: Throwable) { null }
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val firestoreInstance: FirebaseFirestore
        get() = firestore ?: throw IllegalStateException("Firebase not initialized")
    private val authInstance: FirebaseAuth
        get() = auth ?: throw IllegalStateException("Firebase not initialized")

    private val uidLock = kotlinx.coroutines.sync.Mutex()
    private var cachedUid: String? = null

    private suspend fun uid(): String {
        cachedUid?.let { return it }
        return uidLock.withLock {
            cachedUid?.let { return@withLock it }
            val authInst = authInstance
            val current = authInst.currentUser
            val uid = if (current != null) {
                current.uid
            } else {
                val result = authInst.signInAnonymously().await()
                result.user?.uid ?: throw IllegalStateException("Failed to authenticate anonymously")
            }
            cachedUid = uid
            uid
        }
    }

    private suspend fun userDoc() = firestoreInstance.collection("users").document(uid())

    private suspend fun readingsCollection() = userDoc().collection("readings")
    private suspend fun foodNotesCollection() = userDoc().collection("foodNotes")
    private suspend fun appointmentsCollection() = userDoc().collection("appointments")
    private suspend fun medicationsCollection() = userDoc().collection("medications")
    private suspend fun notificationsCollection() = userDoc().collection("notifications")

    fun observeProfile(): Flow<PatientProfile> = callbackFlow {
        var registration: ListenerRegistration? = null
        val job = scope.launch {
            try {
                val doc = userDoc()
                registration = doc.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(
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
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val profile = snapshot.toObject<PatientProfileDto>()?.toModel() ?: PatientProfile(
                            fullName = "Madalo Mbemba",
                            shortName = "Madalo",
                            dateOfBirth = "12 June 2002 (sample)",
                            patientId = "INS-00125 (sample)",
                            clinic = "Queen Elizabeth Central Hospital (sample)",
                            lastClinicVisit = "18 September 2026 (sample)",
                            diabetesType = DiabetesType.TYPE_2
                        )
                        trySend(profile)
                    } else {
                        trySend(
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
                    }
                }
            } catch (_: Throwable) {
                trySend(
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
            }
        }
        awaitClose {
            registration?.remove()
            job.cancel()
        }
    }

    suspend fun saveProfile(profile: PatientProfile) {
        val doc = userDoc()
        doc.set(profile.toDto(), SetOptions.merge()).await()
    }

    fun observeReadings(): Flow<List<GlucoseReading>> = callbackFlow {
        var registration: ListenerRegistration? = null
        val job = scope.launch {
            try {
                registration = readingsCollection().addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val readings = snapshot?.documents?.mapNotNull { it.toObject<GlucoseReadingDto>()?.toModel(it.id) }
                        .orEmpty()
                    trySend(readings)
                }
            } catch (_: Throwable) {
                trySend(emptyList())
            }
        }
        awaitClose {
            registration?.remove()
            job.cancel()
        }
    }

    suspend fun addReading(reading: GlucoseReading) {
        val dto = reading.toDto()
        readingsCollection().document(reading.id.toString()).set(dto).await()
    }

    suspend fun deleteReading(id: Long) {
        readingsCollection().document(id.toString()).delete().await()
    }

    fun observeFoodNotes(): Flow<List<FoodNote>> = callbackFlow {
        var registration: ListenerRegistration? = null
        val job = scope.launch {
            try {
                registration = foodNotesCollection().addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val notes = snapshot?.documents?.mapNotNull { it.toObject<FoodNoteDto>()?.toModel(it.id) }
                        .orEmpty()
                    trySend(notes)
                }
            } catch (_: Throwable) {
                trySend(emptyList())
            }
        }
        awaitClose {
            registration?.remove()
            job.cancel()
        }
    }

    suspend fun addFoodNote(note: FoodNote) {
        foodNotesCollection().document(note.id.toString()).set(note.toDto()).await()
    }

    fun observeAppointments(): Flow<List<Appointment>> = callbackFlow {
        var registration: ListenerRegistration? = null
        val job = scope.launch {
            try {
                registration = appointmentsCollection().addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val appointments = snapshot?.documents?.mapNotNull { it.toObject<AppointmentDto>()?.toModel(it.id) }
                        .orEmpty()
                    trySend(appointments)
                }
            } catch (_: Throwable) {
                trySend(emptyList())
            }
        }
        awaitClose {
            registration?.remove()
            job.cancel()
        }
    }

    suspend fun saveAppointments(appointments: List<Appointment>) {
        val batch = firestoreInstance.batch()
        appointments.forEach { appointment ->
            val ref = appointmentsCollection().document(appointment.id.toString())
            batch.set(ref, appointment.toDto(), SetOptions.merge())
        }
        batch.commit().await()
    }

    fun observeMedications(): Flow<List<Medication>> = callbackFlow {
        var registration: ListenerRegistration? = null
        val job = scope.launch {
            try {
                registration = medicationsCollection().addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val medications = snapshot?.documents?.mapNotNull { it.toObject<MedicationDto>()?.toModel(it.id) }
                        .orEmpty()
                    trySend(medications)
                }
            } catch (_: Throwable) {
                trySend(emptyList())
            }
        }
        awaitClose {
            registration?.remove()
            job.cancel()
        }
    }

    suspend fun saveMedications(medications: List<Medication>) {
        val batch = firestoreInstance.batch()
        medications.forEach { medication ->
            val ref = medicationsCollection().document(medication.id.toString())
            batch.set(ref, medication.toDto(), SetOptions.merge())
        }
        batch.commit().await()
    }

    fun observeNotifications(): Flow<List<NotificationItem>> = callbackFlow {
        var registration: ListenerRegistration? = null
        val job = scope.launch {
            try {
                registration = notificationsCollection().addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val notifications = snapshot?.documents?.mapNotNull { it.toObject<NotificationItemDto>()?.toModel() }
                        .orEmpty()
                    trySend(notifications)
                }
            } catch (_: Throwable) {
                trySend(emptyList())
            }
        }
        awaitClose {
            registration?.remove()
            job.cancel()
        }
    }

    suspend fun saveNotifications(notifications: List<NotificationItem>) {
        try {
            val batch = firestoreInstance.batch()
            notifications.forEachIndexed { index, notification ->
                val ref = notificationsCollection().document(index.toString())
                batch.set(ref, notification.toDto(), SetOptions.merge())
            }
            batch.commit().await()
        } catch (_: Throwable) {
        }
    }

    val isAvailable: Boolean
        get() = firestore != null && auth != null
}

// DTOs for Firestore serialization
data class PatientProfileDto(
    @get:PropertyName("fullName") @set:PropertyName("fullName") var fullName: String = "",
    @get:PropertyName("shortName") @set:PropertyName("shortName") var shortName: String = "",
    @get:PropertyName("dateOfBirth") @set:PropertyName("dateOfBirth") var dateOfBirth: String = "",
    @get:PropertyName("patientId") @set:PropertyName("patientId") var patientId: String = "",
    @get:PropertyName("clinic") @set:PropertyName("clinic") var clinic: String = "",
    @get:PropertyName("lastClinicVisit") @set:PropertyName("lastClinicVisit") var lastClinicVisit: String = "",
    @get:PropertyName("diabetesType") @set:PropertyName("diabetesType") var diabetesType: String = "TYPE_2"
) {
    constructor() : this("", "", "", "", "", "", "TYPE_2")

    fun toModel() = PatientProfile(
        fullName = fullName,
        shortName = shortName,
        dateOfBirth = dateOfBirth,
        patientId = patientId,
        clinic = clinic,
        lastClinicVisit = lastClinicVisit,
        diabetesType = try { DiabetesType.valueOf(diabetesType) } catch (_: Throwable) { DiabetesType.TYPE_2 }
    )
}

fun PatientProfile.toDto() = PatientProfileDto(
    fullName = fullName,
    shortName = shortName,
    dateOfBirth = dateOfBirth,
    patientId = patientId,
    clinic = clinic,
    lastClinicVisit = lastClinicVisit,
    diabetesType = diabetesType.name
)

data class GlucoseReadingDto(
    @get:PropertyName("valueMgDl") @set:PropertyName("valueMgDl") var valueMgDl: Int = 0,
    @get:PropertyName("recordedAt") @set:PropertyName("recordedAt") var recordedAt: Long = 0,
    @get:PropertyName("context") @set:PropertyName("context") var context: String = ReadingContext.AFTER_BREAKFAST.name
) {
    constructor() : this(0, 0, ReadingContext.AFTER_BREAKFAST.name)

    fun toModel(id: String) = GlucoseReading(
        id = id.toLongOrNull() ?: 0L,
        valueMgDl = valueMgDl,
        recordedAt = recordedAt,
        context = try { ReadingContext.valueOf(context) } catch (_: Throwable) { ReadingContext.AFTER_BREAKFAST }
    )
}

fun GlucoseReading.toDto() = GlucoseReadingDto(
    valueMgDl = valueMgDl,
    recordedAt = recordedAt,
    context = context.name
)

data class FoodNoteDto(
    @get:PropertyName("meal") @set:PropertyName("meal") var meal: String = MealType.BREAKFAST.name,
    @get:PropertyName("note") @set:PropertyName("note") var note: String = "",
    @get:PropertyName("recordedAt") @set:PropertyName("recordedAt") var recordedAt: Long = 0
) {
    constructor() : this(MealType.BREAKFAST.name, "", 0)

    fun toModel(id: String) = FoodNote(
        id = id.toLongOrNull() ?: 0L,
        meal = try { MealType.valueOf(meal) } catch (_: Throwable) { MealType.BREAKFAST },
        note = note,
        recordedAt = recordedAt
    )
}

fun FoodNote.toDto() = FoodNoteDto(
    meal = meal.name,
    note = note,
    recordedAt = recordedAt
)

data class AppointmentDto(
    @get:PropertyName("patientName") @set:PropertyName("patientName") var patientName: String = "",
    @get:PropertyName("clinicName") @set:PropertyName("clinicName") var clinicName: String = "",
    @get:PropertyName("dateMillis") @set:PropertyName("dateMillis") var dateMillis: Long? = null,
    @get:PropertyName("lastVisit") @set:PropertyName("lastVisit") var lastVisit: String = "",
    @get:PropertyName("isEmergency") @set:PropertyName("isEmergency") var emergency: Boolean = false,
    @get:PropertyName("isUpcoming") @set:PropertyName("isUpcoming") var upcoming: Boolean = false,
    @get:PropertyName("commentRes") @set:PropertyName("commentRes") var commentRes: Int = 0
) {
    constructor() : this("", "", null, "", false, false, 0)

    fun toModel(id: String) = Appointment(
        id = id.toLongOrNull() ?: 0L,
        patientName = patientName,
        clinicName = clinicName,
        dateMillis = dateMillis,
        lastVisit = lastVisit,
        isEmergency = emergency,
        isUpcoming = upcoming,
        commentRes = commentRes
    )
}

fun Appointment.toDto() = AppointmentDto(
    patientName = patientName,
    clinicName = clinicName,
    dateMillis = dateMillis,
    lastVisit = lastVisit,
    emergency = isEmergency,
    upcoming = isUpcoming,
    commentRes = commentRes
)

data class MedicationDto(
    @get:PropertyName("diabetesType") @set:PropertyName("diabetesType") var diabetesType: String = DiabetesType.TYPE_2.name,
    @get:PropertyName("name") @set:PropertyName("name") var name: String = "",
    @get:PropertyName("dosage") @set:PropertyName("dosage") var dosage: String = "",
    @get:PropertyName("startedAt") @set:PropertyName("startedAt") var startedAt: Long = 0,
    @get:PropertyName("finishAt") @set:PropertyName("finishAt") var finishAt: Long = 0,
    @get:PropertyName("isActive") @set:PropertyName("isActive") var active: Boolean = true
) {
    constructor() : this(DiabetesType.TYPE_2.name, "", "", 0, 0, true)

    fun toModel(id: String) = Medication(
        id = id.toLongOrNull() ?: 0L,
        diabetesType = DiabetesType.valueOf(diabetesType),
        name = name,
        dosage = dosage,
        startedAt = startedAt,
        finishAt = finishAt,
        isActive = active
    )
}

fun Medication.toDto() = MedicationDto(
    diabetesType = diabetesType.name,
    name = name,
    dosage = dosage,
    startedAt = startedAt,
    finishAt = finishAt,
    active = isActive
)

data class NotificationItemDto(
    @get:PropertyName("titleRes") @set:PropertyName("titleRes") var titleRes: Int = 0,
    @get:PropertyName("bodyRes") @set:PropertyName("bodyRes") var bodyRes: Int = 0
) {
    constructor() : this(0, 0)

    fun toModel() = NotificationItem(titleRes = titleRes, bodyRes = bodyRes)
}

fun NotificationItem.toDto() = NotificationItemDto(
    titleRes = titleRes,
    bodyRes = bodyRes
)
