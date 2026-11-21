package com.example.insulia.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.insulia.data.DiabetesType
import com.example.insulia.data.PatientProfile

/** Lets the patient correct the details stored on their own profile document. */
@Composable
fun EditProfileDialog(
    patient: PatientProfile,
    onDismiss: () -> Unit,
    onSave: (PatientProfile) -> Unit
) {
    var fullName by remember { mutableStateOf(patient.fullName) }
    var shortName by remember { mutableStateOf(patient.shortName) }
    var dateOfBirth by remember { mutableStateOf(patient.dateOfBirth) }
    var clinic by remember { mutableStateOf(patient.clinic) }
    var lastVisit by remember { mutableStateOf(patient.lastClinicVisit) }
    var diabetesType by remember { mutableStateOf(patient.diabetesType) }

    val isValid = fullName.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.profile_edit_title)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text(stringResource(R.string.profile_full_name)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(EditProfileDialogTags.FULL_NAME)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = shortName,
                    onValueChange = { shortName = it },
                    label = { Text(stringResource(R.string.profile_short_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = dateOfBirth,
                    onValueChange = { dateOfBirth = it },
                    label = { Text(stringResource(R.string.profile_dob)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = clinic,
                    onValueChange = { clinic = it },
                    label = { Text(stringResource(R.string.profile_clinic)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = lastVisit,
                    onValueChange = { lastVisit = it },
                    label = { Text(stringResource(R.string.profile_last_visit)) },
                    singleLine = true,
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
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        patient.copy(
                            fullName = fullName.trim(),
                            shortName = shortName.trim().ifEmpty { fullName.trim().split(" ").first() },
                            dateOfBirth = dateOfBirth.trim(),
                            clinic = clinic.trim(),
                            lastClinicVisit = lastVisit.trim(),
                            diabetesType = diabetesType
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
}

/** Test tags for the profile dialog fields. */
object EditProfileDialogTags {
    const val FULL_NAME = "profile_full_name"
}
