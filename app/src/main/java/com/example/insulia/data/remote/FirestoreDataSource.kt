package com.example.insulia.data.remote

import com.example.insulia.data.Appointment
import com.example.insulia.data.DiabetesType
import com.example.insulia.data.FoodNote
import com.example.insulia.data.GlucoseReading
import com.example.insulia.data.MealType
import com.example.insulia.data.Medication
import com.example.insulia.data.NotificationItem
import com.example.insulia.data.NotificationReconciler
import com.example.insulia.data.PatientIdAllocator
import com.example.insulia.data.PatientIdFormat
import com.example.insulia.data.PatientProfile
import com.example.insulia.data.ReadingContext
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Transaction
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

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

    /**
     * The UID of the patient who is signed in right now.
     *
     * Firestore never creates an account by itself. Sign-out must stay a real
     * sign-out: if a listener or a queued worker were allowed to sign in
     * anonymously here, the app would silently start reading and writing under
     * a new UID and the patient's records would appear to be gone.
     */
    private fun requireUid(): String = authInstance.currentUser?.uid
        ?: throw IllegalStateException("No patient is signed in")

    /**
     * Lets locally queued writes reach Firestore before an explicit sign-out.
     * Offline writes remain in the cache; the timeout only keeps sign-out from
     * hanging while the device has no connection.
     */
    suspend fun flushPendingWrites() {
        val firestore = firestore ?: return
        runCatching {
            withTimeoutOrNull(PENDING_WRITES_TIMEOUT_MS) {
                firestore.waitForPendingWrites().await()
            }
        }
    }

    private suspend fun userDoc() = firestoreInstance.collection("users").document(requireUid())

    private suspend fun readingsCollection() = userDoc().collection("readings")
    private suspend fun foodNotesCollection() = userDoc().collection("foodNotes")
    private suspend fun appointmentsCollection() = userDoc().collection("appointments")
    private suspend fun medicationsCollection() = userDoc().collection("medications")
    private suspend fun notificationsCollection() = userDoc().collection("notifications")

    fun observeProfile(): Flow<PatientProfile?> = callbackFlow {
        var registration: ListenerRegistration? = null
        val job = scope.launch {
            try {
                val doc = userDoc()
                registration = doc.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        // Never show made-up patient details. An unreadable
                        // profile stays absent and the error is reported.
                        close(error)
                        return@addSnapshotListener
                    }
                    val profile = snapshot
                        ?.takeIf { it.exists() }
                        ?.toObject<PatientProfileDto>()
                        ?.toModel()
                    trySend(profile)
                }
            } catch (t: Throwable) {
                close(t)
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

    /**
     * Returns the patient's permanent Patient ID, assigning one when needed.
     *
     * The profile read, the uniqueness check, the reservation and the profile
     * update all happen in one Firestore transaction. If another patient has
     * already reserved a generated value, [PatientIdAllocator] tries a fresh
     * candidate inside the same transaction.
     *
     * Existing IDs are returned unchanged. Older profiles that have an ID but
     * no reservation get a reservation so that another patient cannot claim
     * the ID later.
     *
     * @return the ID, or `null` when the profile has not reached the server
     * yet (for example a profile created while offline). The caller retries.
     */
    suspend fun ensurePatientId(): String? {
        val firestore = firestoreInstance
        val uid = requireUid()
        val userRef = firestore.collection("users").document(uid)
        val patientIds = firestore.collection("patientIds")

        return firestore.runTransaction { transaction ->
            val profile = transaction.get(userRef)
            if (!profile.exists()) {
                return@runTransaction null
            }

            val existing = profile.getString("patientId").orEmpty()
            if (existing.isNotEmpty()) {
                reserveExistingPatientId(transaction, patientIds, uid, existing)
                return@runTransaction existing
            }

            val patientId = PatientIdAllocator.allocate(
                isReserved = { candidate ->
                    transaction.get(patientIds.document(candidate)).exists()
                }
            )
            transaction.set(patientIds.document(patientId), reservation(uid))
            transaction.update(
                userRef,
                mapOf(
                    "patientId" to patientId,
                    "patientIdAssignedAt" to FieldValue.serverTimestamp()
                )
            )
            patientId
        }.await()
    }

    /**
     * Protects an ID that predates the reservation collection.
     *
     * The profile is not rewritten here, so the rest of its data and the exact
     * existing ID are preserved. Malformed legacy values are left alone; they
     * are still immutable because the profile rules only allow an assigned ID
     * to stay the same.
     */
    private fun reserveExistingPatientId(
        transaction: Transaction,
        patientIds: CollectionReference,
        uid: String,
        patientId: String
    ) {
        if (!PatientIdFormat.isValid(patientId)) return

        val reservationRef = patientIds.document(patientId)
        val reservation = transaction.get(reservationRef)
        if (reservation.exists()) {
            if (reservation.getString("uid") != uid) {
                try {
                    android.util.Log.w(
                        "Insulia",
                        "Patient ID $patientId is already reserved by another profile"
                    )
                } catch (_: Throwable) {
                    // Logging is unavailable in plain JVM tests.
                }
            }
            return
        }

        transaction.set(reservationRef, reservation(uid))
    }

    private fun reservation(uid: String): Map<String, Any> = mapOf(
        "uid" to uid,
        "createdAt" to FieldValue.serverTimestamp()
    )

    fun observeReadings(): Flow<List<GlucoseReading>> = callbackFlow {
        var registration: ListenerRegistration? = null
        val job = scope.launch {
            try {
                registration = readingsCollection().addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    val readings = snapshot?.documents?.mapNotNull { it.toObject<GlucoseReadingDto>()?.toModel(it.id) }
                        .orEmpty()
                    trySend(readings)
                }
            } catch (t: Throwable) {
                close(t)
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
                        close(error)
                        return@addSnapshotListener
                    }
                    val notes = snapshot?.documents?.mapNotNull { it.toObject<FoodNoteDto>()?.toModel(it.id) }
                        .orEmpty()
                    trySend(notes)
                }
            } catch (t: Throwable) {
                close(t)
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

    suspend fun deleteFoodNote(id: Long) {
        foodNotesCollection().document(id.toString()).delete().await()
    }

    fun observeAppointments(): Flow<List<Appointment>> = callbackFlow {
        var registration: ListenerRegistration? = null
        val job = scope.launch {
            try {
                registration = appointmentsCollection().addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    val appointments = snapshot?.documents?.mapNotNull { it.toObject<AppointmentDto>()?.toModel(it.id) }
                        .orEmpty()
                    trySend(appointments)
                }
            } catch (t: Throwable) {
                close(t)
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

    suspend fun saveAppointment(appointment: Appointment) {
        appointmentsCollection()
            .document(appointment.id.toString())
            .set(appointment.toDto(), SetOptions.merge())
            .await()
    }

    suspend fun deleteAppointment(id: Long) {
        appointmentsCollection().document(id.toString()).delete().await()
    }

    fun observeMedications(): Flow<List<Medication>> = callbackFlow {
        var registration: ListenerRegistration? = null
        val job = scope.launch {
            try {
                registration = medicationsCollection().addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    val medications = snapshot?.documents?.mapNotNull { it.toObject<MedicationDto>()?.toModel(it.id) }
                        .orEmpty()
                    trySend(medications)
                }
            } catch (t: Throwable) {
                close(t)
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

    suspend fun saveMedication(medication: Medication) {
        medicationsCollection()
            .document(medication.id.toString())
            .set(medication.toDto(), SetOptions.merge())
            .await()
    }

    suspend fun deleteMedication(id: Long) {
        medicationsCollection().document(id.toString()).delete().await()
    }

    fun observeNotifications(): Flow<List<NotificationItem>> = callbackFlow {
        var registration: ListenerRegistration? = null
        val job = scope.launch {
            try {
                registration = notificationsCollection().addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    val notifications = snapshot?.documents
                        ?.mapNotNull { document ->
                            document.toObject<NotificationItemDto>()?.toModel(document.id)
                        }
                        .orEmpty()
                    trySend(notifications)
                }
            } catch (t: Throwable) {
                close(t)
            }
        }
        awaitClose {
            registration?.remove()
            job.cancel()
        }
    }

    /**
     * Rebuilds derived notifications from the signed-in patient's Firestore
     * records. Everything is done in one batch so the worker cannot leave a
     * half-written alerts list if it is killed midway through.
     */
    suspend fun reconcileNotifications() {
        val firestore = firestoreInstance
        val uid = requireUid()
        val userRef = firestore.collection("users").document(uid)
        val readingsRef = userRef.collection("readings")
        val medicationsRef = userRef.collection("medications")
        val appointmentsRef = userRef.collection("appointments")
        val notificationsRef = userRef.collection("notifications")

        val readings = readingsRef.get().await().documents.mapNotNull { document ->
            document.toObject<GlucoseReadingDto>()?.toModel(document.id)
        }
        val medications = medicationsRef.get().await().documents.mapNotNull { document ->
            document.toObject<MedicationDto>()?.toModel(document.id)
        }
        val appointments = appointmentsRef.get().await().documents.mapNotNull { document ->
            document.toObject<AppointmentDto>()?.toModel(document.id)
        }

        val existing = notificationsRef.get().await().documents
        val desired = NotificationReconciler.build(readings, medications, appointments)
        val desiredIds = desired.map { it.id }.toSet()
        if (desired.isEmpty() && existing.isEmpty()) return

        val batch = firestore.batch()
        desired.forEach { notification ->
            batch.set(
                notificationsRef.document(notification.id),
                notification.toDto(),
                SetOptions.merge()
            )
        }
        existing.forEach { document ->
            if (document.id !in desiredIds) {
                batch.delete(document.reference)
            }
        }
        batch.commit().await()
    }

    suspend fun saveNotifications(notifications: List<NotificationItem>) {
        try {
            val batch = firestoreInstance.batch()
            notifications.forEach { notification ->
                val ref = notificationsCollection().document(notification.id)
                batch.set(ref, notification.toDto(), SetOptions.merge())
            }
            batch.commit().await()
        } catch (t: Throwable) {
            throw t
        }
    }

    suspend fun deleteNotification(id: String) {
        try {
            notificationsCollection().document(id).delete().await()
        } catch (t: Throwable) {
            throw t
        }
    }

    val isAvailable: Boolean
        get() = firestore != null && auth != null

    private companion object {
        const val PENDING_WRITES_TIMEOUT_MS = 3_000L
    }
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
        diabetesType = try { DiabetesType.valueOf(diabetesType) } catch (_: Throwable) { DiabetesType.TYPE_2 },
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
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("titleRes") @set:PropertyName("titleRes") var titleRes: Int = 0,
    @get:PropertyName("bodyRes") @set:PropertyName("bodyRes") var bodyRes: Int = 0,
    @get:PropertyName("title") @set:PropertyName("title") var title: String? = null,
    @get:PropertyName("body") @set:PropertyName("body") var body: String? = null,
    @get:PropertyName("timestamp") @set:PropertyName("timestamp") var timestamp: Long = 0,
    @get:PropertyName("type") @set:PropertyName("type") var type: String? = null,
    @get:PropertyName("relatedId") @set:PropertyName("relatedId") var relatedId: String? = null
) {
    constructor() : this("", 0, 0, null, null, 0, null, null)

    fun toModel(documentId: String) = NotificationItem(
        id = id.ifBlank { documentId },
        titleRes = titleRes,
        bodyRes = bodyRes,
        title = title,
        body = body,
        timestamp = if (timestamp == 0L) System.currentTimeMillis() else timestamp,
        type = type,
        relatedId = relatedId
    )
}

fun NotificationItem.toDto() = NotificationItemDto(
    id = id,
    titleRes = titleRes,
    bodyRes = bodyRes,
    title = title,
    body = body,
    timestamp = timestamp,
    type = type,
    relatedId = relatedId
)
