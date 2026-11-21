package com.example.insulia.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.example.insulia.R
import com.example.insulia.data.MealType

/**
 * Dialog used to save a food note.
 */
@Composable
fun FoodNoteDialog(
    onDismiss: () -> Unit,
    onSave: (meal: MealType, note: String) -> Unit
) {
    var meal by remember { mutableStateOf(MealType.BREAKFAST) }
    var note by remember { mutableStateOf("") }

    // A food note must describe food, so it needs at least one letter.
    // Entries made only of digits and punctuation (for example "123") are
    // rejected instead of being saved as a meaningless note.
    fun isValidFoodNote(text: String): Boolean {
        val trimmed = text.trim()
        return trimmed.isNotEmpty() && trimmed.any { it.isLetter() }
    }

    val isValid = isValidFoodNote(note)
    val showError = note.isNotEmpty() && !isValid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_food_note_title)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.dialog_food_note_text)) },
                    singleLine = true,
                    isError = showError,
                    supportingText = if (showError) {
                        { Text(stringResource(R.string.dialog_food_note_text_error)) }
                    } else {
                        null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(FoodNoteDialogTags.NOTE)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.dialog_food_note_meal),
                    style = MaterialTheme.typography.titleSmall
                )

                MealType.entries.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        RadioButton(
                            selected = meal == option,
                            onClick = { meal = option },
                            modifier = Modifier.testTag(FoodNoteDialogTags.meal(option))
                        )

                        Text(
                            text = stringResource(option.labelRes),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isValidFoodNote(note)) {
                        onSave(meal, note.trim())
                    }
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

/** Test tags for the fields of the food note dialog. */
object FoodNoteDialogTags {
    const val NOTE = "food_note_text"

    private const val MEAL_PREFIX = "food_note_meal_"

    /** Test tag for the radio button of [option]. */
    fun meal(option: MealType): String = MEAL_PREFIX + option.name
}
