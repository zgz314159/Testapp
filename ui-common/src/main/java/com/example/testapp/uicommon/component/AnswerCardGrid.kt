package com.example.testapp.uicommon.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

private const val GRID_COLUMNS = 5

@Composable
fun AnswerCardGrid(
    items: List<AnswerCardItemState>,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColors = answerCardStatusColors()
    Column(modifier = modifier) {
        items.chunked(GRID_COLUMNS).forEach { rowItems ->
            AnswerCardGridRow(
                rowItems = rowItems,
                statusColors = statusColors,
                onClick = onClick
            )
        }
    }
}

@Composable
private fun AnswerCardGridRow(
    rowItems: List<AnswerCardItemState>,
    statusColors: Map<AnswerCardStatus, Color>,
    onClick: (Int) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        rowItems.forEach { item ->
            AnswerCardCell(
                item = item,
                onClick = { onClick(item.index) },
                modifier = Modifier.weight(1f),
                statusColors = statusColors
            )
        }
        repeat(GRID_COLUMNS - rowItems.size) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
