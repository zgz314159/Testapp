package com.example.testapp.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.testapp.domain.model.Question
import com.example.testapp.presentation.component.LocalFontFamily
import com.example.testapp.presentation.component.LocalFontSize
import com.example.testapp.util.answerLetterToIndex
import androidx.compose.material3.Text

@Composable
fun AnswerCardGrid(
    indices: List<Int>,
    questions: List<Question>,
    selectedOptions: List<List<Int>>,
    showResultList: List<Boolean>,
    onClick: (Int) -> Unit
) {
    LazyVerticalGrid(columns = GridCells.Fixed(5), modifier = Modifier.heightIn(max = 300.dp)) {
        items(indices) { idx ->
            val resultShown = showResultList.getOrNull(idx) == true
            val selected = selectedOptions.getOrNull(idx) ?: emptyList<Int>()
            val q = questions.getOrNull(idx)
            val correctIdx = q?.let { answerLetterToIndex(it.answer) }

            val bgColor = when {
                resultShown && selected.singleOrNull() == correctIdx ->
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                resultShown && selected.isNotEmpty() && selected.singleOrNull() != correctIdx ->
                    MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                selected.isNotEmpty() ->
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                else -> Color.Transparent
            }

            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .background(bgColor)
                    .clickable { onClick(idx) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${idx + 1}",
                    fontSize = LocalFontSize.current,
                    fontFamily = LocalFontFamily.current
                )
            }
        }
    }
}