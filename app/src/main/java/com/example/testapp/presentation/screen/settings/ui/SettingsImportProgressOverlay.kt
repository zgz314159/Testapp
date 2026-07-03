package com.example.testapp.presentation.screen.settings.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.example.testapp.R
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.design.AppLoadingIndicator
import com.example.testapp.uicommon.design.AppLoadingOverlay
import com.example.testapp.uicommon.design.AppSpacing

@Composable
fun SettingsImportProgressOverlay(
    isLoading: Boolean,
    importProgress: Float,
    fontSize: Float,
    onCancel: () -> Unit
) {
    AppLoadingOverlay(visible = isLoading) {
        AppLoadingIndicator()
        Spacer(modifier = Modifier.height(AppSpacing.md))
        Text(
            stringResource(R.string.importing_quiz),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = fontSize.sp,
                fontFamily = LocalFontFamily.current
            )
        )
        if (importProgress > 0f) {
            Spacer(modifier = Modifier.height(AppSpacing.sm))
            Text(
                stringResource(R.string.progress_percent, (importProgress * 100).toInt()),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = (fontSize - 2).sp,
                    fontFamily = LocalFontFamily.current
                )
            )
            Spacer(modifier = Modifier.height(AppSpacing.sm))
            LinearProgressIndicator(
                progress = { importProgress },
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(AppSpacing.sm))
        TextButton(onClick = onCancel) {
            Text(
                stringResource(android.R.string.cancel),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = fontSize.sp,
                    fontFamily = LocalFontFamily.current
                )
            )
        }
    }
}
