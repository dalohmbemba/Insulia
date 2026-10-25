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
import com.example.insulia.data.PatientProfile
import com.example.insulia.navigation.Screen
import com.example.insulia.ui.components.InsuliaBottomBar
import com.example.insulia.ui.components.PageHeader
import com.example.insulia.ui.components.ProfileDestination
import com.example.insulia.ui.components.QuickActionType
import com.example.insulia.ui.components.QuickActionsRow

/** The profile overview and the entry points to the other sections. */
@Composable
fun ProfileScreen(
    patient: PatientProfile,
    onBack: () -> Unit,
    onSelectTab: (Screen) -> Unit,
    onQuickAction: (QuickActionType) -> Unit,
    onOpen: (Screen) -> Unit,
    selectedTab: Screen
) {
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
                text = patient.shortName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = stringResource(R.string.profile_manage),
                fontSize = 14.sp
            )

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
