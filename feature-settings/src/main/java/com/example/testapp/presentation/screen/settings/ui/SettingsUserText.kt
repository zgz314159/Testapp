package com.example.testapp.presentation.screen.settings.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.example.testapp.uicommon.component.LocalFontFamily

private const val HELP_TEXT_OFFSET = 2f
private const val HELP_TEXT_MIN_SP = 12f

@Composable
fun SettingsHeadlineText(text: String, fontSize: Float, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyLarge.copy(
            fontSize = fontSize.sp,
            fontFamily = LocalFontFamily.current
        )
    )
}

@Composable
fun SettingsHelpText(text: String, fontSize: Float, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = (fontSize - HELP_TEXT_OFFSET).coerceAtLeast(HELP_TEXT_MIN_SP).sp,
            fontFamily = LocalFontFamily.current,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}
