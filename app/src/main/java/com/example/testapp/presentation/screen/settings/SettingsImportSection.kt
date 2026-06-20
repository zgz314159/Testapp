package com.example.testapp.presentation.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
fun SettingsImportButton(
    labelResId: Int,
    fontSize: Float,
    onClick: () -> Unit
) {
    Button(onClick = onClick) {
        Text(
            stringResource(labelResId),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = fontSize.sp,
                fontFamily = LocalFontFamily.current
            )
        )
    }
}

@Composable
fun SettingsImportProgressOverlay(
    isLoading: Boolean,
    importProgress: Float,
    fontSize: Float,
    onCancel: () -> Unit
) {
    if (!isLoading) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Card(modifier = Modifier.padding(16.dp)) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    stringResource(R.string.importing_quiz),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = fontSize.sp,
                        fontFamily = LocalFontFamily.current
                    )
                )
                if (importProgress > 0f) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.progress_percent, (importProgress * 100).toInt()),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = (fontSize - 2).sp,
                            fontFamily = LocalFontFamily.current
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { importProgress },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
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
    }
}
