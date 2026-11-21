package com.example.insulia.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.sp
import com.example.insulia.R
import com.example.insulia.data.DateFormats
import com.example.insulia.data.DiabetesType
import com.example.insulia.data.Medication

/**
 * Adds a new medication or edits an existing one.
 *
 * Every change is saved to the patient's own `medications` collection; the
 * medication list is never re-seeded over the patient's data.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationDialog(
    initial: Medication,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (Medication) -> Unit
) {
    var name by remember { mutableStateOf(initial.name) }
    var dosage by remember { mutableStateOf(initial.dosage) }
    var diabetesType by remember { mutableStateOf(initial.diabetesType) }
    var active by remember { mutableStateOf(initial.isActive) }
    var startedAt by remember { mutableStateOf(initial.startedAt) }
    var finishAt by remember { mutableStateOf(initial.finishAt) }
    var pickingStart by remember { mutableStateOf(false) }
    var pickingFinish by remember { mutableStateOf(false) }

    val startedPicker = rememberDatePickerState(initialSelectedDateMillis = startedAt)
    val finishPicker = rememberDatePickerState(initialSelectedDateMillis = finishAt)

    val isValid = name.isNotBlank() && dosage.isNotBlank() && finishAt >= startedAt

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(
                    if (isNew) R.string.medication_new_title else R.string.medication_edit_title
                )
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.medication_name)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(MedicationDialogTags.NAME)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text(stringResource(R.string.medication_dosage)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(MedicationDialogTags.DOSAGE)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = DateFormats.utcFullDate(startedAt),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.medication_started)) },
                    trailingIcon = {
                        IconButton(onClick = { pickingStart = true }) {
                            Icon(
                                imageVector = Icons.Outlined.DateRange,
                                contentDescription = stringResource(R.string.booking_pick_date),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = DateFormats.utcFullDate(finishAt),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.medication_finish)) },
                    trailingIcon = {
                        IconButton(onClick = { pickingFinish = true }) {
                            Icon(
                                imageVector = Icons.Outlined.DateRange,
                                contentDescription = stringResource(R.string.booking_pick_date),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.profile_diabetes_type),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                DiabetesType.entries.forEach { type ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = diabetesType == type,
                            onClick = { diabetesType = type }
                        )
                        Text(
                            text = stringResource(type.labelRes),
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.medication_active),
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = active,
                        onCheckedChange = { active = it }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        initial.copy(
                            name = name.trim(),
                            dosage = dosage.trim(),
                            diabetesType = diabetesType,
                            isActive = active,
                            startedAt = startedAt,
                            finishAt = finishAt
                        )
                    )
                },
                enabled = isValid
            ) {
                Text(stringResource(R.string.dialog_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        }
    )

    if (pickingStart) {
        DatePickerDialog(
            onDismissRequest = { pickingStart = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        startedPicker.selectedDateMillis?.let { startedAt = it }
                        pickingStart = false
                    }
                ) {
                    Text(stringResource(R.string.dialog_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { pickingStart = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        ) {
            DatePicker(state = startedPicker)
        }
    }

    if (pickingFinish) {
        DatePickerDialog(
            onDismissRequest = { pickingFinish = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        finishPicker.selectedDateMillis?.let { finishAt = it }
                        pickingFinish = false
                    }
                ) {
                    Text(stringResource(R.string.dialog_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { pickingFinish = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        ) {
            DatePicker(state = finishPicker)
        }
    }
}

/** Test tags for the medication dialog fields. */
object MedicationDialogTags {
    const val NAME = "medication_name"
    const val DOSAGE = "medication_dosage"
}
