package com.example.testapp.presentation.screen.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.core.util.answerToOptionIndices
import com.example.testapp.core.util.resolveDisplayOptions
import com.example.testapp.domain.model.Question
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.component.RichText
import com.example.testapp.uicommon.design.QuestionOptionSurface
import com.example.testapp.uicommon.design.answerChoiceBorderColor
import com.example.testapp.uicommon.design.answerChoicePalette
import com.example.testapp.uicommon.design.colorFor
import com.example.testapp.uicommon.design.resolveAnswerChoiceTone

@Composable
fun ExamOptionsList(
    question: Question,
    questionFontSize: Float,
    lineSpacingMultiplier: Float,
    letterSpacing: Float = 0f,
    selectedOption: List<Int>,
    showResult: Boolean,
    onOptionClick: (Int) -> Unit
) {
    val displayOptions = resolveDisplayOptions(question)
    val correctIndices = answerToOptionIndices(question)
    val optionFontSize = (questionFontSize - 1.5f).coerceAtLeast(12f)

    val palette = answerChoicePalette()
    displayOptions.forEachIndexed { idx, option ->
        val isSelected = selectedOption.contains(idx)
        val isCorrect = showResult && correctIndices.contains(idx)

        val tone = resolveAnswerChoiceTone(showResult, isSelected, isCorrect)
        val backgroundColor = palette.colorFor(tone)

        QuestionOptionSurface(
            containerColor = backgroundColor,
            borderColor = answerChoiceBorderColor(tone),
            enabled = !showResult,
            onClick = { onOptionClick(idx) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 7.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = null,
                    enabled = !showResult,
                    modifier = Modifier.scale(1.5f)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            RichText(
                text = option,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 16.dp, horizontal = 4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = optionFontSize.sp,
                fontFamily = LocalFontFamily.current,
                lineSpacingMultiplier = lineSpacingMultiplier,
                letterSpacing = letterSpacing
            )
        }
    }
}
