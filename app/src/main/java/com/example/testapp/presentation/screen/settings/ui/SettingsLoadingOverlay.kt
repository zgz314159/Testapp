package com.example.testapp.presentation.screen.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.R
import com.example.testapp.uicommon.component.LocalFontFamily

@Composable
fun SettingsLoadingOverlay(
    isLoading: Boolean,
    fontSize: Float,
    importProgress: Float,
    onCancel: () -> Unit
) {
    if (!isLoading) return

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Card(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    stringResource(R.string.importing_quiz),
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp, fontFamily = LocalFontFamily.current)
                )
                if (importProgress > 0f) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.progress_percent, (importProgress * 100).toInt()),
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = (fontSize - 2).sp, fontFamily = LocalFontFamily.current)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(progress = { importProgress }, modifier = Modifier.fillMaxWidth())
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onCancel) {
                    Text(stringResource(R.string.cancel), style = MaterialTheme.typography.bodyMedium.copy(fontSize = fontSize.sp, fontFamily = LocalFontFamily.current))
                }
            }
        }
    }
}

