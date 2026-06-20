package com.example.testapp.uicommon.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AnswerCardGrid(
    items: List<AnswerCardItemState>,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColorMap = rememberStatusColorMap()
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        modifier = modifier.heightIn(max = 300.dp)
    ) {
        items(
            items = items,
            key = { "card_${it.index}" }
        ) { item ->
            val isCurrent = item.isCurrent
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .then(
                        if (isCurrent) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                        else Modifier
                    )
                    .background(statusColorMap[item.status] ?: Color.Transparent)
                    .clickable { onClick(item.index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.label,
                    fontSize = LocalFontSize.current,
                    fontFamily = LocalFontFamily.current
                )
            }
        }
    }
}

@Composable
private fun rememberStatusColorMap(): Map<AnswerCardStatus, Color> {
    val scheme = MaterialTheme.colorScheme
    return mapOf(
        AnswerCardStatus.CORRECT to scheme.primary.copy(alpha = 0.3f),
        AnswerCardStatus.WRONG to scheme.error.copy(alpha = 0.3f),
        AnswerCardStatus.SELECTED to scheme.secondary.copy(alpha = 0.1f),
        AnswerCardStatus.UNANSWERED to Color.Transparent
    )
}
