package com.example.testapp.presentation.screen.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.testapp.feature.settings.R
import com.example.testapp.uicommon.design.AppElevatedConfirmDialog

@Composable
fun SettingsStoragePermissionDialog(
    onDismiss: () -> Unit,
    onGrant: () -> Unit,
) {
    AppElevatedConfirmDialog(
        onDismiss = onDismiss,
        title = stringResource(R.string.import_quiz_local_perm_title),
        message = stringResource(R.string.import_quiz_local_perm_message),
        confirmLabel = stringResource(R.string.import_quiz_local_perm_grant),
        dismissLabel = stringResource(android.R.string.cancel),
        onConfirm = onGrant,
    )
}
