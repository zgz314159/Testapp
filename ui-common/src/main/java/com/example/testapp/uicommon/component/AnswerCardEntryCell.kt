package com.example.testapp.uicommon.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.testapp.uicommon.design.AppOverlayMetrics

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AnswerCardEntryCell(
    item: AnswerCardItemState,
    multiRound: Boolean,
    expanded: Boolean,
    onSingleClick: () -> Unit,
    onDoubleClick: () -> Unit,
    modifier: Modifier = Modifier,
    statusColors: Map<AnswerCardStatus, Color> = answerCardStatusColors()
) {
    val shape = RoundedCornerShape(14.dp)
    val fill = statusColors[item.status] ?: MaterialTheme.colorScheme.surface
    Surface(
        modifier = modifier
            .padding(4.dp)
            .defaultMinSize(minHeight = 36.dp)
            .then(
                if (item.isCurrent || expanded) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, shape)
                } else {
                    Modifier
                }
            )
            .combinedClickable(
                onClick = onSingleClick,
                onDoubleClick = onDoubleClick
            ),
        shape = shape,
        color = fill,
        tonalElevation = if (item.status == AnswerCardStatus.UNANSWERED) 2.dp else 3.dp,
        shadowElevation = if (item.isCurrent || expanded) {
            AppOverlayMetrics.answerCardCellCurrentElevation
        } else {
            AppOverlayMetrics.answerCardCellElevation
        },
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.label,
                fontSize = LocalFontSize.current,
                fontFamily = LocalFontFamily.current,
                modifier = Modifier.padding(bottom = if (multiRound) 8.dp else 0.dp)
            )
            if (multiRound) {
                AnswerCardExpandIndicator(
                    expanded = expanded,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 1.dp)
                        .size(12.dp)
                )
            }
        }
    }
}
