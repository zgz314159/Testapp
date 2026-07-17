package com.example.testapp.uicommon.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun PracticeExamTopBarShell(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = questionSessionFloatingContainerColor(),
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 7.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(PracticeExamTopBarMetrics.barHeight),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}
