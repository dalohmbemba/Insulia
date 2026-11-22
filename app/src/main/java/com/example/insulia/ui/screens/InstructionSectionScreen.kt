package com.example.insulia.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.insulia.R
import com.example.insulia.data.DiabetesType
import com.example.insulia.navigation.Screen
import com.example.insulia.ui.components.InstructionCard
import com.example.insulia.ui.components.InsuliaBottomBar
import com.example.insulia.ui.components.PageHeader

/** Guidance for patients and caregivers. */
@Composable
fun InstructionSectionScreen(
    onBack: () -> Unit,
    onSelectTab: (Screen) -> Unit,
    selectedTab: Screen,
    diabetesType: DiabetesType,
    scrollState: ScrollState,
    onOpenResource: (title: String, url: String) -> Unit
) {
    val glucoseTitle = stringResource(R.string.instructions_glucose_title)
    val medicationTitle = stringResource(R.string.instructions_medication_title)
    val foodTitle = stringResource(R.string.instructions_food_title)
    val footTitle = stringResource(R.string.instructions_foot_title)
    val appointmentTitle = stringResource(R.string.instructions_appointments_title)
    val helpTitle = stringResource(R.string.instructions_help_title)

    val glucoseUrl = stringResource(R.string.instructions_url_glucose_monitoring)
    val medicationUrl = stringResource(
        if (diabetesType == DiabetesType.TYPE_1) R.string.instructions_url_medication_type1
        else R.string.instructions_url_medication_type2
    )
    val foodUrl = stringResource(R.string.instructions_url_food)
    val footUrl = stringResource(R.string.instructions_url_foot)
    val appointmentUrl = stringResource(
        if (diabetesType == DiabetesType.TYPE_1) R.string.instructions_url_appointments_type1
        else R.string.instructions_url_appointments_type2
    )
    val helpUrl = stringResource(R.string.instructions_url_urgent_help)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PageHeader(
            title = stringResource(R.string.page_instructions),
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.instructions_intro),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(18.dp))

            InstructionCard(
                title = glucoseTitle,
                description = stringResource(R.string.instructions_glucose_body),
                onLearnMore = { onOpenResource(glucoseTitle, glucoseUrl) }
            )

            InstructionCard(
                title = medicationTitle,
                description = stringResource(R.string.instructions_medication_body),
                onLearnMore = { onOpenResource(medicationTitle, medicationUrl) }
            )

            InstructionCard(
                title = foodTitle,
                description = stringResource(R.string.instructions_food_body),
                onLearnMore = { onOpenResource(foodTitle, foodUrl) }
            )

            InstructionCard(
                title = footTitle,
                description = stringResource(R.string.instructions_foot_body),
                onLearnMore = { onOpenResource(footTitle, footUrl) }
            )

            InstructionCard(
                title = appointmentTitle,
                description = stringResource(R.string.instructions_appointments_body),
                onLearnMore = { onOpenResource(appointmentTitle, appointmentUrl) }
            )

            InstructionCard(
                title = helpTitle,
                description = stringResource(R.string.instructions_help_body),
                onLearnMore = { onOpenResource(helpTitle, helpUrl) }
            )
        }

        InsuliaBottomBar(
            selected = selectedTab,
            onSelect = onSelectTab
        )
    }
}
