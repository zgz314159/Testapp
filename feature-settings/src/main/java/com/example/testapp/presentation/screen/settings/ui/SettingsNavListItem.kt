package com.example.testapp.presentation.screen.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun SettingsNavListItem(
    label: String,
    fontSize: Float,
    onClick: () -> Unit,
    leadingIcon: ImageVector? = null,
) {
    ListItem(
        modifier = Modifier
            .heightIn(min = 52.dp)
            .clickable(onClick = onClick),
        headlineContent = { SettingsHeadlineText(label, fontSize) },
        leadingContent = leadingIcon?.let { icon ->
            { SettingsElevatedLeadingIcon(icon = icon) }
        },
        trailingContent = {
            SettingsElevatedChevron(icon = Icons.AutoMirrored.Filled.KeyboardArrowRight)
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}
