package com.example.insulia.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.insulia.R
import com.example.insulia.data.ReadingContext

/**
 * Dialog used to record a glucose reading.
 *
 * The reading is validated before it can be saved.
 */
@Composable
fun LogReadingDialog(
    onDismiss: () -> Unit,
    onSave: (valueMgDl: Int, context: ReadingContext) -> Unit
) {
    var value by remember { mutableStateOf("") }
    var context by remember { mutableStateOf(ReadingContext.AFTER_BREAKFAST) }

    val parsedValue = value.toIntOrNull()
    val isValid = parsedValue != null && parsedValue in MIN_READING..MAX_READING
    val showError = value.isNotEmpty() && !isValid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_log_reading_title)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = value,
                    onValueChange = { input -> value = input.filter { it.isDigit() } },
                    label = { Text(stringResource(R.string.dialog_log_reading_value)) },
                    singleLine = true,
                    isError = showError,
                    supportingText = if (showError) {
                        { Text(stringResource(R.string.dialog_log_reading_value_error)) }
                    } else {
                        null
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(LogReadingDialogTags.VALUE)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.dialog_log_reading_context),
                    style = MaterialTheme.typography.titleSmall
                )

                ReadingContext.entries.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        RadioButton(
                            selected = context == option,
                            onClick = { context = option },
                            modifier = Modifier.testTag(LogReadingDialogTags.context(option))
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
                onClick = { parsedValue?.let { onSave(it, context) } },
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

private const val MIN_READING = 20
private const val MAX_READING = 600

/** Test tags for the fields of the log reading dialog. */
object LogReadingDialogTags {
    const val VALUE = "reading_value"

    private const val CONTEXT_PREFIX = "reading_context_"

    /** Test tag for the radio button of [option]. */
    fun context(option: ReadingContext): String = CONTEXT_PREFIX + option.name
}
