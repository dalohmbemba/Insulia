package com.example.insulia.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.insulia.R

/** The quick actions offered on the home and profile screens. */
enum class QuickActionType {
    LOG_READING,
    FOOD_NOTE,
    LEARN
}

/**
 * The row of quick actions.
 *
 * Every action now does something: readings and food notes are recorded in
 * the shared state, and "Learn" opens the instruction section.
 */
@Composable
fun QuickActionsRow(
    onAction: (QuickActionType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        QuickAction(
            title = stringResource(R.string.quick_log_reading),
            icon = Icons.AutoMirrored.Outlined.ShowChart,
            onClick = { onAction(QuickActionType.LOG_READING) }
        )

        QuickAction(
            title = stringResource(R.string.quick_food_note),
            icon = Icons.Outlined.Restaurant,
            onClick = { onAction(QuickActionType.FOOD_NOTE) }
        )

        QuickAction(
            title = stringResource(R.string.quick_learn),
            icon = Icons.AutoMirrored.Outlined.MenuBook,
            onClick = { onAction(QuickActionType.LEARN) }
        )
    }
}
