package com.example.testapp.presentation.screen.settings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.testapp.uicommon.design.AppElevatedActionSheetTokens
import com.example.testapp.uicommon.design.AppSpacing

@Composable
fun SettingsCardGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val tokens = AppElevatedActionSheetTokens
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = tokens.cardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
    ) {
        Column(content = content)
    }
}

@Composable
fun SettingsCardDivider() {
    val tokens = AppElevatedActionSheetTokens
    HorizontalDivider(
        modifier = Modifier.padding(start = 64.dp, end = 16.dp),
        color = tokens.textSecondary.copy(alpha = 0.18f),
    )
}
