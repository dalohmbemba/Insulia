package com.example.insulia.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.insulia.R
import com.example.insulia.data.DateFormats
import com.example.insulia.data.DiabetesType
import com.example.insulia.data.GlucoseReading
import com.example.insulia.data.GlucoseStatus
import com.example.insulia.data.Medication
import com.example.insulia.data.PatientProfile
import com.example.insulia.navigation.Screen
import com.example.insulia.ui.components.InfoRecordCard
import com.example.insulia.ui.components.InsuliaBottomBar
import com.example.insulia.ui.components.InsuliaCard
import com.example.insulia.ui.components.PageHeader
import com.example.insulia.ui.components.glucoseStatusColor
import java.util.concurrent.TimeUnit

/**
 * The saved information about the patient.
 *
 * Every value comes from the shared state, so the numbers here always match
 * what is shown on the home screen.
 */
@Composable
fun PersonalRecordsScreen(
    patient: PatientProfile,
    latestReading: GlucoseReading?,
    readingCount: Int,
    foodNoteCount: Int,
    appointmentCount: Int,
    medications: List<Medication> = emptyList(),
    onBack: () -> Unit,
    onSelectTab: (Screen) -> Unit,
    selectedTab: Screen
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PageHeader(
            title = stringResource(R.string.page_personal_records),
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.records_intro),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = stringResource(R.string.records_personal_information),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            InfoRecordCard(
                label = stringResource(R.string.records_full_name),
                value = patient.fullName
            )
            InfoRecordCard(
                label = stringResource(R.string.records_date_of_birth),
                value = patient.dateOfBirth
            )
            InfoRecordCard(
                label = stringResource(R.string.records_patient_id),
                value = patient.patientId
            )
            InfoRecordCard(
                label = stringResource(R.string.records_clinic),
                value = patient.clinic
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = stringResource(R.string.records_diabetes_records),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            InfoRecordCard(
                label = stringResource(R.string.records_latest_reading),
                value = if (latestReading == null) {
                    stringResource(R.string.glucose_levels_empty)
                } else {
                    val status = GlucoseStatus.of(latestReading.valueMgDl)
                    "${stringResource(R.string.glucose_value_format, latestReading.valueMgDl)} · ${stringResource(status.labelRes)}"
                }
            )

            InfoRecordCard(
                label = stringResource(R.string.records_last_clinic_visit),
                value = patient.lastClinicVisit
            )

            InfoRecordCard(
                label = stringResource(R.string.records_diabetes_type),
                value = stringResource(patient.diabetesType.labelRes)
            )

            InfoRecordCard(
                label = stringResource(R.string.records_medication),
                value = stringResource(R.string.records_medication_value)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = stringResource(R.string.records_medication_tracking),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (medications.isEmpty()) {
                InsuliaCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.records_no_medications),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(13.dp)
                    )
                }
            } else {
                medications.filter { it.isActive }.forEach { medication ->
                    MedicationTrackingCard(medication = medication)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            InfoRecordCard(
                label = stringResource(R.string.records_readings_logged),
                value = readingCount.toString()
            )

            InfoRecordCard(
                label = stringResource(R.string.records_food_notes),
                value = foodNoteCount.toString()
            )

            InfoRecordCard(
                label = stringResource(R.string.records_appointments),
                value = appointmentCount.toString()
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = stringResource(R.string.records_disclaimer),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        InsuliaBottomBar(
            selected = selectedTab,
            onSelect = onSelectTab
        )
    }
}

@Composable
private fun MedicationTrackingCard(medication: Medication) {
    val now = System.currentTimeMillis()
    val daysLeft = TimeUnit.MILLISECONDS.toDays(medication.finishAt - now)
    val isOverdue = medication.isActive && medication.finishAt < now
    val isDueSoon = medication.isActive && !isOverdue && daysLeft <= 7

    InsuliaCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = medication.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(medication.diabetesType.labelRes),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.records_medication_name) + ": " + medication.name,
                fontSize = 13.sp
            )
            Text(
                text = stringResource(R.string.records_dosage) + ": " + medication.dosage,
                fontSize = 13.sp
            )
            Text(
                text = stringResource(R.string.records_started_corrected) + ": " + DateFormats.shortDate(medication.startedAt),
                fontSize = 13.sp
            )
            Text(
                text = stringResource(R.string.records_finish_date) + ": " + DateFormats.shortDate(medication.finishAt),
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            val statusText = when {
                isOverdue -> stringResource(R.string.records_status_overdue)
                isDueSoon -> stringResource(R.string.records_status_due_soon, daysLeft)
                else -> stringResource(R.string.records_status_on_track)
            }
            val statusColor = when {
                isOverdue -> MaterialTheme.colorScheme.error
                isDueSoon -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            Text(
                text = stringResource(R.string.records_status) + ": " + statusText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = statusColor
            )

            if (isOverdue || isDueSoon) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(
                        R.string.records_medication_alert,
                        medication.name,
                        if (isOverdue) stringResource(R.string.records_status_overdue) else stringResource(R.string.records_status_due_soon, daysLeft)
                    ),
                    fontSize = 11.sp,
                    color = statusColor
                )
            }
        }
    }
}
