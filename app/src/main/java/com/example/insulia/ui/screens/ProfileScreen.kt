package com.example.insulia.ui.screens

import androidx.compose.foundation.background
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.insulia.R
import com.example.insulia.data.PatientProfile
import com.example.insulia.navigation.Screen
import com.example.insulia.ui.components.InsuliaBottomBar
import com.example.insulia.ui.components.InsuliaCard
import com.example.insulia.ui.components.PageHeader
import com.example.insulia.ui.components.ProfileDestination

/** The profile overview and the entry points to the other sections. */
@Composable
fun ProfileScreen(
    patient: PatientProfile,
    onBack: () -> Unit,
    onSelectTab: (Screen) -> Unit,
    onOpen: (Screen) -> Unit,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit,
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
            var personalExpanded by rememberSaveable { mutableStateOf(true) }
            var careExpanded by rememberSaveable { mutableStateOf(false) }

            ProfileAccordionSection(
                title = stringResource(R.string.profile_overview),
                expanded = personalExpanded,
                onToggle = { personalExpanded = !personalExpanded }
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Text(
                        text = patient.shortName.ifBlank { patient.fullName }
                            .ifBlank { stringResource(R.string.profile_name_placeholder) },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(onClick = onEditProfile) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.profile_edit))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ProfileAccordionSection(
                title = stringResource(R.string.profile_destinations),
                expanded = careExpanded,
                onToggle = { careExpanded = !careExpanded },
                contentInHeaderCard = false
            ) {
                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                    ProfileDestination(
                        title = stringResource(R.string.profile_personal_records_title),
                        subtitle = stringResource(R.string.profile_personal_records_subtitle),
                        icon = Icons.Outlined.FolderOpen,
                        onClick = { onOpen(Screen.PersonalRecords) }
                    )
                    ProfileDestination(
                        title = stringResource(R.string.profile_glucose_levels_title),
                        subtitle = stringResource(R.string.profile_glucose_levels_subtitle),
                        icon = Icons.AutoMirrored.Outlined.ShowChart,
                        onClick = { onOpen(Screen.GlucoseLevels) }
                    )
                    ProfileDestination(
                        title = stringResource(R.string.profile_appointments_title),
                        subtitle = stringResource(R.string.profile_appointments_subtitle),
                        icon = Icons.Outlined.Event,
                        onClick = { onOpen(Screen.Appointments) }
                    )
                    ProfileDestination(
                        title = stringResource(R.string.profile_instructions_title),
                        subtitle = stringResource(R.string.profile_instructions_subtitle),
                        icon = Icons.AutoMirrored.Outlined.MenuBook,
                        onClick = { onOpen(Screen.Instructions) }
                    )
                    ProfileDestination(
                        title = stringResource(R.string.profile_education_title),
                        subtitle = stringResource(R.string.profile_education_subtitle),
                        icon = Icons.Outlined.School,
                        onClick = { onOpen(Screen.Instructions) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(stringResource(R.string.profile_logout))
            }
        }

        InsuliaBottomBar(
            selected = selectedTab,
            onSelect = onSelectTab
        )
    }
}

@Composable
private fun ProfileAccordionSection(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    contentInHeaderCard: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        label = "profile accordion arrow"
    )

    val header: @Composable () -> Unit = {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.rotate(arrowRotation)
            )
        }
    }

    if (contentInHeaderCard) {
        InsuliaCard(
            modifier = Modifier.fillMaxWidth().animateContentSize(),
            onClick = onToggle
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                header()
                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(content = content)
                }
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxWidth().animateContentSize()) {
            InsuliaCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = onToggle,
                content = { header() }
            )
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(content = content)
            }
        }
    }
}
