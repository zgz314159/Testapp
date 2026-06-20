package com.example.testapp.presentation.screen.settings

import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.example.testapp.uicommon.component.LocalFontFamily

@Composable
fun SettingsExportButton(
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
