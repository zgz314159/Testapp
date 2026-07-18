package com.example.testapp.presentation.screen.settings.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.uicommon.design.AppElevatedActionSheetTokens

@Composable
fun SettingsExpandableCardSection(
    title: String,
    fontSize: Float,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    expandDescription: String,
    collapseDescription: String,
    leadingIcon: ImageVector? = null,
    supportingText: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clickable { onExpandedChange(!expanded) },
        headlineContent = { SettingsHeadlineText(title, fontSize) },
        supportingContent = supportingText?.let { text ->
            {
                Text(
                    text = text,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = AppElevatedActionSheetTokens.textSecondary,
                )
            }
        },
        leadingContent = leadingIcon?.let { icon ->
            { SettingsElevatedLeadingIcon(icon = icon) }
        },
        trailingContent = {
            SettingsElevatedChevron(
                icon = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = if (expanded) collapseDescription else expandDescription,
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
    AnimatedVisibility(
        visible = expanded,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        Column(content = content)
    }
}
