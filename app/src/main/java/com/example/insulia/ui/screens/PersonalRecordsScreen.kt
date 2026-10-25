package com.example.insulia.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.insulia.R
import com.example.insulia.data.GlucoseReading
import com.example.insulia.data.GlucoseStatus
import com.example.insulia.data.PatientProfile
import com.example.insulia.navigation.Screen
import com.example.insulia.ui.components.InfoRecordCard
import com.example.insulia.ui.components.InsuliaBottomBar
import com.example.insulia.ui.components.PageHeader
import com.example.insulia.ui.components.glucoseStatusColor

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
                label = stringResource(R.string.records_medication),
                value = stringResource(R.string.records_medication_value)
            )

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
