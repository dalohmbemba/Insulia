package com.example.insulia.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.insulia.R
import com.example.insulia.data.DateFormats

/**
 * Dialog used to book a new appointment.
 *
 * The date is picked in a separate Material date picker, so the form keeps the
 * values that were already typed in.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDialog(
    onDismiss: () -> Unit,
    onBook: (
        patientName: String,
        clinicName: String,
        dateMillis: Long?,
        lastVisit: String,
        isEmergency: Boolean
    ) -> Unit
) {
    var patientName by remember { mutableStateOf("") }
    var clinicName by remember { mutableStateOf("") }
    var lastVisit by remember { mutableStateOf("") }
    var dateMillis by remember { mutableStateOf<Long?>(null) }
    var isEmergency by remember { mutableStateOf(false) }
    var pickingDate by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)

    val isValid = patientName.isNotBlank() && clinicName.isNotBlank() && dateMillis != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.booking_dialog_title)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = patientName,
                    onValueChange = { patientName = it },
                    label = { Text(stringResource(R.string.booking_patient_name)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(BookingDialogTags.PATIENT_NAME)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = clinicName,
                    onValueChange = { clinicName = it },
                    label = { Text(stringResource(R.string.booking_clinic_name)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(BookingDialogTags.CLINIC_NAME)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = lastVisit,
                    onValueChange = { lastVisit = it },
                    label = { Text(stringResource(R.string.booking_last_visit)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(BookingDialogTags.LAST_VISIT)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = dateMillis?.let { DateFormats.utcFullDate(it) }.orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.booking_date)) },
                    placeholder = { Text(stringResource(R.string.booking_pick_date)) },
                    trailingIcon = {
                        IconButton(onClick = { pickingDate = true }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = stringResource(R.string.booking_pick_date),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(BookingDialogTags.DATE)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.booking_type),
                    style = MaterialTheme.typography.titleSmall
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = isEmergency,
                        onClick = { isEmergency = true },
                        modifier = Modifier.testTag(BookingDialogTags.VISIT_TYPE_EMERGENCY)
                    )
                    Text(
                        text = stringResource(R.string.booking_emergency),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = !isEmergency,
                        onClick = { isEmergency = false },
                        modifier = Modifier.testTag(BookingDialogTags.VISIT_TYPE_NORMAL)
                    )
                    Text(
                        text = stringResource(R.string.booking_normal),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onBook(
                        patientName.trim(),
                        clinicName.trim(),
                        dateMillis,
                        lastVisit.trim(),
                        isEmergency
                    )
                },
                enabled = isValid
            ) {
                Text(stringResource(R.string.dialog_submit))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        }
    )

    if (pickingDate) {
        DatePickerDialog(
            onDismissRequest = { pickingDate = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { dateMillis = it }
                        pickingDate = false
                    }
                ) {
                    Text(stringResource(R.string.dialog_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { pickingDate = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/** Test tags for the fields of the booking dialog. */
object BookingDialogTags {
    const val PATIENT_NAME = "booking_patient_name"
    const val CLINIC_NAME = "booking_clinic_name"
    const val LAST_VISIT = "booking_last_visit"
    const val DATE = "booking_date"
    const val VISIT_TYPE_EMERGENCY = "booking_visit_type_emergency"
    const val VISIT_TYPE_NORMAL = "booking_visit_type_normal"
}
