package com.example.testapp.presentation.screen.settings.ui

import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun SettingsListSwitchRow(
    label: String,
    fontSize: Float,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    leadingIcon: ImageVector? = null,
) {
    ListItem(
        headlineContent = { SettingsHeadlineText(label, fontSize) },
        leadingContent = leadingIcon?.let { icon ->
            { SettingsElevatedLeadingIcon(icon = icon) }
        },
        trailingContent = { Switch(checked = checked, onCheckedChange = onCheckedChange) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}
