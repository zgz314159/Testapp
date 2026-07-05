package com.example.testapp.presentation.screen.exam.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.example.testapp.core.util.answerLettersToIndices
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.component.LocalFontSize
import com.example.testapp.uicommon.design.answerChoicePalette
import com.example.testapp.uicommon.design.colorFor
import com.example.testapp.uicommon.design.resolveAnswerChoiceTone

@Composable
fun ExamOptionsList(
    question: com.example.testapp.domain.model.Question,
    questionFontSize: Float = LocalFontSize.current.value,
    lineSpacingMultiplier: Float = 1f,
    letterSpacing: Float = 0f,
    selectedOption: List<Int>,
    showResult: Boolean,
    onOptionClick: (Int) -> Unit
) {
    val palette = answerChoicePalette()
    question.options.forEachIndexed { idx, option ->
        val correctIndices = answerLettersToIndices(question.answer)
        val isSelected = selectedOption.contains(idx)
        val isCorrect = showResult && correctIndices.contains(idx)

        val backgroundColor = palette.colorFor(
            resolveAnswerChoiceTone(showResult, isSelected, isCorrect)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .background(backgroundColor)
                .clickable(enabled = !showResult) { onOptionClick(idx) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                if (question.type == "多选题") {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onOptionClick(idx) },
                        enabled = !showResult,
                        modifier = Modifier.scale(1.5f)
                    )
                } else {
                    RadioButton(
                        selected = isSelected,
                        onClick = null,
                        enabled = !showResult,
                        modifier = Modifier.scale(1.5f)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = option,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp),
                fontSize = LocalFontSize.current,
                fontFamily = LocalFontFamily.current
            )
        }
    }
}


