package com.example.insulia.data.repository

import com.example.insulia.data.Appointment
import com.example.insulia.data.FoodNote
import com.example.insulia.data.GlucoseReading
import com.example.insulia.data.Medication
import com.example.insulia.data.NotificationItem
import com.example.insulia.data.PatientProfile
import com.example.insulia.data.remote.FirestoreDataSource
import kotlinx.coroutines.flow.Flow

class PatientRepository(
    private val dataSource: FirestoreDataSource = FirestoreDataSource()
) {
    val isAvailable: Boolean get() = dataSource.isAvailable

    fun observeProfile(): Flow<PatientProfile> = dataSource.observeProfile()
    suspend fun saveProfile(profile: PatientProfile) = dataSource.saveProfile(profile)

    fun observeReadings(): Flow<List<GlucoseReading>> = dataSource.observeReadings()
    suspend fun addReading(reading: GlucoseReading) = dataSource.addReading(reading)
    suspend fun deleteReading(id: Long) = dataSource.deleteReading(id)

    fun observeFoodNotes(): Flow<List<FoodNote>> = dataSource.observeFoodNotes()
    suspend fun addFoodNote(note: FoodNote) = dataSource.addFoodNote(note)

    fun observeAppointments(): Flow<List<Appointment>> = dataSource.observeAppointments()
    suspend fun saveAppointments(appointments: List<Appointment>) = dataSource.saveAppointments(appointments)

    fun observeMedications(): Flow<List<Medication>> = dataSource.observeMedications()
    suspend fun saveMedications(medications: List<Medication>) = dataSource.saveMedications(medications)

    fun observeNotifications(): Flow<List<NotificationItem>> = dataSource.observeNotifications()
    suspend fun saveNotifications(notifications: List<NotificationItem>) = dataSource.saveNotifications(notifications)
}
