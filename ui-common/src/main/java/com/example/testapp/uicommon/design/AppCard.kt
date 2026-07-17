package com.example.testapp.uicommon.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Preserve the neutral question-card background used before the Material 3 upgrade. */
val QuestionSessionCardContainerLight = Color(0xFFF0F0F2)

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    contentPadding: Modifier = Modifier.padding(AppSpacing.md),
    containerColor: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val resolvedContainerColor = containerColor ?: if (isSystemInDarkTheme()) {
        MaterialTheme.colorScheme.surface
    } else {
        QuestionSessionCardContainerLight
    }
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = resolvedContainerColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 5.dp)
    ) {
        Column(modifier = contentPadding, content = content)
    }
}
