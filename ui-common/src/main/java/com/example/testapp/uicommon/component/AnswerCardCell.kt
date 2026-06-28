package com.example.testapp.uicommon.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AnswerCardCell(
    item: AnswerCardItemState,
    onClick: () -> Unit = {},
    onDoubleClick: () -> Unit = onClick,
    modifier: Modifier = Modifier,
    statusColors: Map<AnswerCardStatus, Color> = answerCardStatusColors()
) {
    Box(
        modifier = modifier
            .padding(4.dp)
            .then(
                if (item.isCurrent) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                } else {
                    Modifier
                }
            )
            .background(statusColors[item.status] ?: Color.Transparent, RoundedCornerShape(4.dp))
            .combinedClickable(onClick = onClick, onDoubleClick = onDoubleClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = item.label,
            fontSize = LocalFontSize.current,
            fontFamily = LocalFontFamily.current
        )
    }
}

@Composable
fun answerCardStatusColors(): Map<AnswerCardStatus, Color> {
    val scheme = MaterialTheme.colorScheme
    return mapOf(
        AnswerCardStatus.CORRECT to scheme.primary.copy(alpha = 0.3f),
        AnswerCardStatus.WRONG to scheme.error.copy(alpha = 0.3f),
        AnswerCardStatus.SELECTED to scheme.secondary.copy(alpha = 0.1f),
        AnswerCardStatus.UNANSWERED to Color.Transparent
    )
}
