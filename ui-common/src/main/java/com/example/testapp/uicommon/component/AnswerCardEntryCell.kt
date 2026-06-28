package com.example.testapp.uicommon.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
fun AnswerCardEntryCell(
    item: AnswerCardItemState,
    multiRound: Boolean,
    expanded: Boolean,
    onSingleClick: () -> Unit,
    onDoubleClick: () -> Unit,
    modifier: Modifier = Modifier,
    statusColors: Map<AnswerCardStatus, Color> = answerCardStatusColors()
) {
    Box(
        modifier = modifier
            .padding(4.dp)
            .defaultMinSize(minHeight = 36.dp)
            .then(
                if (item.isCurrent || expanded) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                } else {
                    Modifier
                }
            )
            .background(statusColors[item.status] ?: Color.Transparent, RoundedCornerShape(4.dp))
            .combinedClickable(
                onClick = onSingleClick,
                onDoubleClick = onDoubleClick
            ),
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
