package com.example.testapp.uicommon.component

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AnswerCardGrid(
    items: List<AnswerCardItemState>,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        modifier = modifier.heightIn(max = 300.dp)
    ) {
        items(
            items = items,
            key = { "card_${it.index}" }
        ) { item ->
            AnswerCardCell(item = item, onClick = { onClick(item.index) })
        }
    }
}
