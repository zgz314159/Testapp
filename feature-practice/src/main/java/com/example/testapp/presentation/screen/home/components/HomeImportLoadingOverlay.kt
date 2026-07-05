package com.example.testapp.presentation.screen.home.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.testapp.uicommon.design.AppLoadingOverlay
import com.example.testapp.uicommon.design.AppSpacing

@Composable
fun HomeImportLoadingOverlay(
    visible: Boolean,
    importProgress: Float,
) {
    AppLoadingOverlay(visible = visible) {
        if (importProgress > 0f) {
            CircularProgressIndicator(progress = { importProgress.coerceIn(0f, 1f) })
        } else {
            CircularProgressIndicator()
        }
        Spacer(modifier = Modifier.height(AppSpacing.sm))
        Text(
            text = "正在处理，请稍后",
            style = MaterialTheme.typography.bodyMedium
        )
        if (importProgress > 0f) {
            Spacer(modifier = Modifier.height(AppSpacing.sm))
            LinearProgressIndicator(
                progress = { importProgress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
