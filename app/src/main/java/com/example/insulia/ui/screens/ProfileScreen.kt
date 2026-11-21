package com.example.insulia.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.example.insulia.R
import com.example.insulia.data.PatientProfile
import com.example.insulia.navigation.Screen
import com.example.insulia.ui.components.InsuliaBottomBar
import com.example.insulia.ui.components.InsuliaCard
import com.example.insulia.ui.components.PageHeader
import com.example.insulia.ui.components.ProfileDestination
import com.example.insulia.ui.components.QuickActionType
import com.example.insulia.ui.components.QuickActionsRow

/** The profile overview and the entry points to the other sections. */
@Composable
fun ProfileScreen(
    patient: PatientProfile,
    accountEmail: String?,
    onBack: () -> Unit,
    onSelectTab: (Screen) -> Unit,
    onQuickAction: (QuickActionType) -> Unit,
    onOpen: (Screen) -> Unit,
    onEditProfile: () -> Unit,
    onManageAccount: () -> Unit,
    selectedTab: Screen
) {
    val clipboard = LocalClipboardManager.current
    var idCopied by remember { mutableStateOf(false) }

    LaunchedEffect(idCopied) {
        if (idCopied) {
            delay(2_000)
            idCopied = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PageHeader(
            title = stringResource(R.string.page_profile),
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = stringResource(R.string.profile_overview),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = patient.shortName.ifBlank { patient.fullName }
                    .ifBlank { stringResource(R.string.profile_name_placeholder) },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            TextButton(
                onClick = onEditProfile,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = stringResource(R.string.profile_edit),
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = stringResource(R.string.profile_manage),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            InsuliaCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.profile_patient_id),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = patient.patientId.ifBlank {
                                stringResource(R.string.profile_patient_id_pending)
                            },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.weight(1f)
                        )

                        if (patient.patientId.isNotBlank()) {
                            TextButton(
                                onClick = {
                                    clipboard.setText(AnnotatedString(patient.patientId))
                                    idCopied = true
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (idCopied) {
                                        stringResource(R.string.profile_patient_id_copied)
                                    } else {
                                        stringResource(R.string.profile_patient_id_copy)
                                    },
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResource(R.string.profile_patient_id_hint),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            InsuliaCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.profile_account_title),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (accountEmail != null) {
                            stringResource(R.string.profile_account_linked, accountEmail)
                        } else {
                            stringResource(R.string.profile_account_anonymous)
                        },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    TextButton(
                        onClick = onManageAccount,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = if (accountEmail == null) {
                                stringResource(R.string.profile_account_set_up)
                            } else {
                                stringResource(R.string.profile_account_manage)
                            },
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.home_quick_actions),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            QuickActionsRow(onAction = onQuickAction)

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = stringResource(R.string.profile_destinations),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(10.dp))

            ProfileDestination(
                title = stringResource(R.string.profile_personal_records_title),
                subtitle = stringResource(R.string.profile_personal_records_subtitle),
                onClick = { onOpen(Screen.PersonalRecords) }
            )

            ProfileDestination(
                title = stringResource(R.string.profile_glucose_levels_title),
                subtitle = stringResource(R.string.profile_glucose_levels_subtitle),
                onClick = { onOpen(Screen.GlucoseLevels) }
            )

            ProfileDestination(
                title = stringResource(R.string.profile_appointments_title),
                subtitle = stringResource(R.string.profile_appointments_subtitle),
                onClick = { onOpen(Screen.Appointments) }
            )

            ProfileDestination(
                title = stringResource(R.string.profile_instructions_title),
                subtitle = stringResource(R.string.profile_instructions_subtitle),
                onClick = { onOpen(Screen.Instructions) }
            )

            ProfileDestination(
                title = stringResource(R.string.profile_education_title),
                subtitle = stringResource(R.string.profile_education_subtitle),
                onClick = { onOpen(Screen.Instructions) }
            )
        }

        InsuliaBottomBar(
            selected = selectedTab,
            onSelect = onSelectTab
        )
    }
}
