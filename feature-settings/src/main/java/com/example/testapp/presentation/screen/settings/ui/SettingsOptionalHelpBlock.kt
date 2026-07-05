package com.example.testapp.presentation.screen.settings.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.testapp.uicommon.design.AppSpacing

@Composable
fun SettingsOptionalHelpBlock(
    visible: Boolean,
    onToggle: () -> Unit,
    helpText: String,
    fontSize: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onToggle) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null
            )
        }
    }
    if (visible) {
        SettingsHelpText(
            text = helpText,
            fontSize = fontSize,
            modifier = Modifier.padding(bottom = AppSpacing.sm)
        )
    }
}
