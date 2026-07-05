package com.example.testapp.presentation.screen.settings

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.testapp.feature.settings.R

@Composable
fun SettingsStoragePermissionDialog(
    onDismiss: () -> Unit,
    onGrant: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.import_quiz_local_perm_title)) },
        text = { Text(stringResource(R.string.import_quiz_local_perm_message)) },
        confirmButton = {
            TextButton(onClick = onGrant) {
                Text(stringResource(R.string.import_quiz_local_perm_grant))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}
