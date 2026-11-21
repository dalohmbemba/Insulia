package com.example.insulia.data.repository

import com.example.insulia.data.Appointment
import com.example.insulia.data.FoodNote
import com.example.insulia.data.GlucoseReading
import com.example.insulia.data.Medication
import com.example.insulia.data.NotificationItem
import com.example.insulia.data.PatientProfile
import com.example.insulia.data.remote.AuthManager
import com.example.insulia.data.remote.FirestoreDataSource
import kotlinx.coroutines.flow.Flow

/**
 * The single entry point the ViewModel uses for patient data.
 *
 * It keeps the ViewModel unaware of Firebase types and makes it possible to
 * test the app with an unavailable backend.
 */
class PatientRepository(
    private val dataSource: FirestoreDataSource = FirestoreDataSource(),
    private val authManager: AuthManager = AuthManager()
) {
    val isAvailable: Boolean get() = dataSource.isAvailable

    // ---------------------------------------------------------------------
    // Account
    // ---------------------------------------------------------------------

    val isAuthAvailable: Boolean get() = authManager.isAvailable
    val isSignedIn: Boolean get() = authManager.isSignedIn
    val isAnonymousAccount: Boolean get() = authManager.isAnonymous
    val accountEmail: String? get() = authManager.email
    val accountUid: String? get() = authManager.uid

    suspend fun ensureSignedIn(): String = authManager.ensureSignedIn()

    fun signOut() = authManager.signOut()

    suspend fun linkAccount(email: String, password: String): Result<Unit> =
        authManager.linkWithEmail(email, password)

    suspend fun signInToAccount(email: String, password: String): Result<Unit> =
        authManager.signInWithEmail(email, password)

    suspend fun sendPasswordReset(email: String): Result<Unit> =
        authManager.sendPasswordReset(email)

    // ---------------------------------------------------------------------
    // Profile
    // ---------------------------------------------------------------------

    fun observeProfile(): Flow<PatientProfile?> = dataSource.observeProfile()

    suspend fun saveProfile(profile: PatientProfile) = dataSource.saveProfile(profile)

    val isPatientIdAvailable: Boolean get() = dataSource.isAvailable

    /**
     * Assigns the signed-in patient's permanent Patient ID or returns the ID
     * that is already on record. The value is reserved in `/patientIds` and
     * written to the profile in the same Firestore transaction.
     */
    suspend fun ensurePatientId(): String? = dataSource.ensurePatientId()

    // ---------------------------------------------------------------------
    // Readings
    // ---------------------------------------------------------------------

    fun observeReadings(): Flow<List<GlucoseReading>> = dataSource.observeReadings()
    suspend fun addReading(reading: GlucoseReading) = dataSource.addReading(reading)
    suspend fun deleteReading(id: Long) = dataSource.deleteReading(id)

    // ---------------------------------------------------------------------
    // Food notes
    // ---------------------------------------------------------------------

    fun observeFoodNotes(): Flow<List<FoodNote>> = dataSource.observeFoodNotes()
    suspend fun addFoodNote(note: FoodNote) = dataSource.addFoodNote(note)
    suspend fun deleteFoodNote(id: Long) = dataSource.deleteFoodNote(id)

    // ---------------------------------------------------------------------
    // Appointments
    // ---------------------------------------------------------------------

    fun observeAppointments(): Flow<List<Appointment>> = dataSource.observeAppointments()
    suspend fun saveAppointments(appointments: List<Appointment>) = dataSource.saveAppointments(appointments)
    suspend fun saveAppointment(appointment: Appointment) = dataSource.saveAppointment(appointment)
    suspend fun deleteAppointment(id: Long) = dataSource.deleteAppointment(id)

    // ---------------------------------------------------------------------
    // Medications
    // ---------------------------------------------------------------------

    fun observeMedications(): Flow<List<Medication>> = dataSource.observeMedications()
    suspend fun saveMedications(medications: List<Medication>) = dataSource.saveMedications(medications)
    suspend fun saveMedication(medication: Medication) = dataSource.saveMedication(medication)
    suspend fun deleteMedication(id: Long) = dataSource.deleteMedication(id)

    // ---------------------------------------------------------------------
    // Notifications
    // ---------------------------------------------------------------------

    fun observeNotifications(): Flow<List<NotificationItem>> = dataSource.observeNotifications()
    suspend fun saveNotifications(notifications: List<NotificationItem>) = dataSource.saveNotifications(notifications)
    suspend fun deleteNotification(id: String) = dataSource.deleteNotification(id)

    /**
     * Rebuilds notification documents from the Firestore source collections.
     * Used by WorkManager after record changes and after a process restart.
     */
    suspend fun reconcileNotifications() = dataSource.reconcileNotifications()
}
