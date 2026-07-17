package com.example.testapp.uicommon.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
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

/** 全答展开分题号：primary 字色，与当前题定点一致便于辨认。 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AnswerCardRoundCell(
    item: AnswerCardItemState,
    onDoubleClick: () -> Unit,
    modifier: Modifier = Modifier,
    statusColors: Map<AnswerCardStatus, Color> = answerCardStatusColors()
) {
    val primary = MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(14.dp)
    val fill = statusColors[item.status] ?: MaterialTheme.colorScheme.surface
    Surface(
        modifier = modifier
            .padding(4.dp)
            .then(
                if (item.isCurrent) {
                    Modifier.border(2.dp, primary, shape)
                } else {
                    Modifier
                }
            )
            .combinedClickable(onClick = {}, onDoubleClick = onDoubleClick),
        shape = shape,
        color = fill,
        tonalElevation = if (item.status == AnswerCardStatus.UNANSWERED) 2.dp else 3.dp,
        shadowElevation = if (item.isCurrent) {
            AppOverlayMetrics.answerCardCellCurrentElevation
        } else {
            AppOverlayMetrics.answerCardCellElevation
        },
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = item.label,
                fontSize = LocalFontSize.current,
                fontFamily = LocalFontFamily.current,
                color = primary
            )
        }
    }
}
