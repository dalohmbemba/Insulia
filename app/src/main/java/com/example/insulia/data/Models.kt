package com.example.insulia.data

import androidx.annotation.StringRes
import com.example.insulia.R

/** The type of diabetes the patient has been diagnosed with. */
enum class DiabetesType(@StringRes val labelRes: Int) {
    TYPE_1(R.string.diabetes_type_1),
    TYPE_2(R.string.diabetes_type_2)
}

/**
 * A medication prescribed to the patient as part of their diabetes treatment.
 *
 * @param diabetesType which diabetes type this medication is for.
 * @param name the name of the medication.
 * @param dosage frequency or dose instructions shown to the patient.
 * @param startedAt when the patient started (or corrected) taking this medication.
 * @param finishAt when the course is due to finish.
 * @param isActive whether the medication is currently being taken.
 */
data class Medication(
    val id: Long,
    val diabetesType: DiabetesType,
    val name: String,
    val dosage: String,
    val startedAt: Long,
    val finishAt: Long,
    val isActive: Boolean = true
)

/**
 * A single glucose measurement.
 *
 * @param valueMgDl the measurement in mg/dL.
 * @param recordedAt when the reading was taken, in epoch milliseconds.
 * @param context what the reading was taken in relation to, used for context.
 */
data class GlucoseReading(
    val id: Long,
    val valueMgDl: Int,
    val recordedAt: Long,
    val context: ReadingContext
)

/** The situation a glucose reading was taken in. */
enum class ReadingContext(@StringRes val labelRes: Int) {
    BEFORE_BREAKFAST(R.string.reading_context_before_breakfast),
    AFTER_BREAKFAST(R.string.reading_context_after_breakfast),
    BEFORE_MEAL(R.string.reading_context_before_meal),
    AFTER_MEAL(R.string.reading_context_after_meal),
    BEDTIME(R.string.reading_context_bedtime)
}

/**
 * Classification of a glucose reading against the target range used by the app.
 *
 * The target range is deliberately kept in one place so the home card, the
 * trend chart and the records screen all agree on what "High" means.
 */
enum class GlucoseStatus(@StringRes val labelRes: Int) {
    LOW(R.string.glucose_status_low),
    IN_RANGE(R.string.glucose_status_in_range),
    HIGH(R.string.glucose_status_high);

    companion object {
        const val LOW_LIMIT = 70
        const val HIGH_LIMIT = 140

        fun of(valueMgDl: Int): GlucoseStatus = when {
            valueMgDl < LOW_LIMIT -> LOW
            valueMgDl > HIGH_LIMIT -> HIGH
            else -> IN_RANGE
        }
    }
}

/** A meal note recorded by the patient. */
data class FoodNote(
    val id: Long,
    val meal: MealType,
    val note: String,
    val recordedAt: Long
)

/** The meals a food note can be attached to. */
enum class MealType(@StringRes val labelRes: Int) {
    BREAKFAST(R.string.meal_breakfast),
    LUNCH(R.string.meal_lunch),
    DINNER(R.string.meal_dinner),
    SNACK(R.string.meal_snack)
}

/**
 * A clinic appointment.
 *
 * @param dateMillis the appointment date, or `null` when no date has been
 * chosen yet.
 * @param isUpcoming `true` for a booking that has not happened yet.
 * @param commentRes the comment shown in the appointments table.
 */
data class Appointment(
    val id: Long,
    val patientName: String,
    val clinicName: String,
    val dateMillis: Long?,
    val lastVisit: String,
    val isEmergency: Boolean,
    val isUpcoming: Boolean,
    @StringRes val commentRes: Int
)

/** A notification shown on the alerts screen. */
data class NotificationItem(
    @StringRes val titleRes: Int,
    @StringRes val bodyRes: Int
)

/** The patient details shown in the profile and personal records screens. */
data class PatientProfile(
    val fullName: String,
    val shortName: String,
    val dateOfBirth: String,
    val patientId: String,
    val clinic: String,
    val lastClinicVisit: String,
    val diabetesType: DiabetesType = DiabetesType.TYPE_2
)
