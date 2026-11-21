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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.insulia.R
import com.example.insulia.navigation.Screen
import com.example.insulia.ui.components.InstructionCard
import com.example.insulia.ui.components.InsuliaBottomBar
import com.example.insulia.ui.components.FootnoteText
import com.example.insulia.ui.components.PageHeader

/** Guidance for patients and caregivers. */
@Composable
fun InstructionSectionScreen(
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
            title = stringResource(R.string.page_instructions),
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
                text = stringResource(R.string.instructions_intro),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(18.dp))

            InstructionCard(
                title = stringResource(R.string.instructions_glucose_title),
                description = stringResource(R.string.instructions_glucose_body)
            )

            InstructionCard(
                title = stringResource(R.string.instructions_medication_title),
                description = stringResource(R.string.instructions_medication_body)
            )

            InstructionCard(
                title = stringResource(R.string.instructions_food_title),
                description = stringResource(R.string.instructions_food_body)
            )

            InstructionCard(
                title = stringResource(R.string.instructions_foot_title),
                description = stringResource(R.string.instructions_foot_body)
            )

            InstructionCard(
                title = stringResource(R.string.instructions_appointments_title),
                description = stringResource(R.string.instructions_appointments_body)
            )

            InstructionCard(
                title = stringResource(R.string.instructions_help_title),
                description = stringResource(R.string.instructions_help_body)
            )

            Spacer(modifier = Modifier.height(8.dp))

            FootnoteText(text = stringResource(R.string.instructions_disclaimer))
        }

        InsuliaBottomBar(
            selected = selectedTab,
            onSelect = onSelectTab
        )
    }
}
