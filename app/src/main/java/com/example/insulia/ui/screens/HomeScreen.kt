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
import com.example.insulia.data.DateFormats
import com.example.insulia.data.FoodNote
import com.example.insulia.data.GlucoseReading
import com.example.insulia.data.GlucoseStatus
import com.example.insulia.data.PatientProfile
import com.example.insulia.navigation.Screen
import com.example.insulia.ui.components.DietScheduleCard
import com.example.insulia.ui.components.GlucoseTrendCard
import com.example.insulia.ui.components.InsuliaBottomBar
import com.example.insulia.ui.components.InsuliaCard
import com.example.insulia.ui.components.LatestFoodNoteCard
import com.example.insulia.ui.components.QuickActionType
import com.example.insulia.ui.components.QuickActionsRow
import com.example.insulia.ui.components.SectionHeading
import com.example.insulia.ui.components.glucoseStatusColor

@Composable
fun HomeScreen(
    patient: PatientProfile,
    latestReading: GlucoseReading?,
    recentReadings: List<GlucoseReading>,
    latestFoodNote: FoodNote?,
    foodNoteCount: Int,
    onQuickAction: (QuickActionType) -> Unit,
    onSelectTab: (Screen) -> Unit,
    onOpenFoodNotes: () -> Unit,
    onDeleteFoodNote: (Long) -> Unit = {},
    selectedTab: Screen
) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(text = stringResource(R.string.home_greeting), fontSize = 22.sp,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(text = patient.shortName, fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurface)
        }

        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeading(
                text = stringResource(R.string.home_latest_glucose),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            LatestGlucoseCard(reading = latestReading)

            Spacer(modifier = Modifier.height(16.dp))
            SectionHeading(
                text = stringResource(R.string.home_quick_actions),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            QuickActionsRow(onAction = onQuickAction)

            Spacer(modifier = Modifier.height(16.dp))
            SectionHeading(
                text = stringResource(R.string.home_glucose_trend),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            GlucoseTrendCard(readings = recentReadings)

            Spacer(modifier = Modifier.height(16.dp))
            SectionHeading(
                text = stringResource(R.string.home_diet_suggestion),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            DietScheduleCard()

            Spacer(modifier = Modifier.height(16.dp))
            LatestFoodNoteCard(
                note = latestFoodNote,
                count = foodNoteCount,
                onViewAll = onOpenFoodNotes,
                onDelete = latestFoodNote?.let { note -> { onDeleteFoodNote(note.id) } }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        InsuliaBottomBar(selected = selectedTab, onSelect = onSelectTab)
    }
}

@Composable
private fun LatestGlucoseCard(reading: GlucoseReading?) {
    InsuliaCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (reading == null) {
                Text(text = stringResource(R.string.glucose_levels_empty), fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                return@Column
            }
            val status = GlucoseStatus.of(reading.valueMgDl)
            Text(text = stringResource(R.string.glucose_value_format, reading.valueMgDl),
                fontSize = 28.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
            Text(text = stringResource(status.labelRes), fontSize = 16.sp,
                color = glucoseStatusColor(status))
            Text(text = "${stringResource(reading.context.labelRes)} · ${DateFormats.timeLabel(reading.recordedAt)}",
                fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
