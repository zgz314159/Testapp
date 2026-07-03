package com.example.testapp.uicommon.design

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle

@Composable
private fun appEmptyStateTextStyle(compact: Boolean): TextStyle {
    val base = if (compact) {
        MaterialTheme.typography.bodyMedium
    } else {
        MaterialTheme.typography.bodyLarge
    }
    return base.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
fun AppEmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AppContentText(
            text = message,
            style = appEmptyStateTextStyle(compact = false),
            modifier = Modifier.padding(AppSpacing.md)
        )
    }
}

@Composable
fun AppEmptyStateInline(
    message: String,
    modifier: Modifier = Modifier
) {
    AppContentText(
        text = message,
        style = appEmptyStateTextStyle(compact = true),
        modifier = modifier.padding(AppSpacing.md)
    )
}
