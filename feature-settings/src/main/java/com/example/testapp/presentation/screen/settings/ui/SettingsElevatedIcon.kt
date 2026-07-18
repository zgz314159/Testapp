package com.example.testapp.presentation.screen.settings.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.testapp.uicommon.design.AppElevatedActionSheetTokens

@Composable
fun SettingsElevatedLeadingIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = AppElevatedActionSheetTokens.brandBlue,
    background: Color = AppElevatedActionSheetTokens.brandBlueSoft,
) {
    Surface(
        modifier = modifier.size(40.dp),
        shape = RoundedCornerShape(12.dp),
        color = background,
        tonalElevation = 1.dp,
        shadowElevation = 5.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(21.dp),
            )
        }
    }
}

@Composable
fun SettingsElevatedChevron(
    icon: ImageVector,
    contentDescription: String? = null,
) {
    val tokens = AppElevatedActionSheetTokens
    Surface(
        modifier = Modifier.size(28.dp),
        shape = CircleShape,
        color = tokens.brandBlueSoft,
        tonalElevation = 1.dp,
        shadowElevation = 4.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tokens.brandBlue,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
