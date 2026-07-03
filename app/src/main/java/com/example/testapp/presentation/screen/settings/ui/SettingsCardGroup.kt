package com.example.testapp.presentation.screen.settings.ui

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.testapp.uicommon.design.AppCard
import com.example.testapp.uicommon.design.AppSpacing

@Composable
fun SettingsCardGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
        contentPadding = Modifier,
        content = content
    )
}

@Composable
fun SettingsCardDivider() {
    HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
}
