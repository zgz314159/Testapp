package com.example.testapp.presentation.screen.settings.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.R
import com.example.testapp.uicommon.component.LocalFontFamily

@Composable
fun SettingsSoundDarkPanel(
    fontSize: Float,
    soundEnabled: Boolean,
    darkTheme: Boolean,
    onSoundChange: (Boolean) -> Unit,
    onDarkThemeChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            stringResource(R.string.sound_label),
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp, fontFamily = LocalFontFamily.current)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Switch(checked = soundEnabled, onCheckedChange = onSoundChange)
    }
    Spacer(modifier = Modifier.height(24.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            stringResource(R.string.dark_mode_label),
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp, fontFamily = LocalFontFamily.current)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Switch(checked = darkTheme, onCheckedChange = onDarkThemeChange)
    }
    Spacer(modifier = Modifier.height(24.dp))
}

