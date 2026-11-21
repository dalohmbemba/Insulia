package com.example.insulia.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.insulia.R

/**
 * Lets a patient attach an email and password to the anonymous account.
 *
 * Account linking keeps the existing UID, so every record already stored under
 * `users/{uid}` stays attached. The same credentials can also be used on a new
 * installation to recover those records.
 */
@Composable
fun AccountDialog(
    isAnonymous: Boolean,
    accountEmail: String?,
    busy: Boolean,
    onDismiss: () -> Unit,
    onCreateAccount: (email: String, password: String) -> Unit,
    onSignIn: (email: String, password: String) -> Unit,
    onResetPassword: (email: String) -> Unit
) {
    var email by remember { mutableStateOf(accountEmail.orEmpty()) }
    var password by remember { mutableStateOf("") }

    val canSubmit = email.isNotBlank() && password.length >= 6 && !busy

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isAnonymous) {
                    stringResource(R.string.account_dialog_title)
                } else {
                    stringResource(R.string.account_dialog_signed_in)
                }
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (isAnonymous) {
                        stringResource(R.string.account_hint_link)
                    } else {
                        stringResource(R.string.account_hint_signed_in, accountEmail.orEmpty())
                    },
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(R.string.account_email)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(AccountDialogTags.EMAIL)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.account_password)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(AccountDialogTags.PASSWORD)
                )

                if (isAnonymous) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.account_password_hint),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResource(R.string.account_sign_in_hint),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            if (isAnonymous) {
                TextButton(
                    onClick = { onCreateAccount(email.trim(), password) },
                    enabled = canSubmit
                ) {
                    Text(stringResource(R.string.account_create))
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.dialog_ok))
                }
            }
        },
        dismissButton = {
            if (isAnonymous) {
                TextButton(
                    onClick = { onSignIn(email.trim(), password) },
                    enabled = canSubmit
                ) {
                    Text(stringResource(R.string.account_sign_in))
                }
            } else {
                TextButton(
                    onClick = { onResetPassword(email.trim()) },
                    enabled = email.isNotBlank() && !busy
                ) {
                    Text(stringResource(R.string.account_reset))
                }
            }
        }
    )
}

/** Test tags for the account dialog fields. */
object AccountDialogTags {
    const val EMAIL = "account_email"
    const val PASSWORD = "account_password"
}
