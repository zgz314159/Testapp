package com.example.testapp.presentation.screen.practice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.Question
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.component.LocalFontSize
import com.example.testapp.presentation.screen.components.FillAnswerResultText
import com.example.testapp.uicommon.component.RichText
import com.example.testapp.uicommon.component.TextResponseAnswerContent

@Composable
fun PracticeResultSection(
    question: Question,
    showResult: Boolean,
    textAnswer: String,
    resolvedFillAnswer: String,
    correctIndices: List<Int>,
    displayOptions: List<String>,
    selectedOption: List<Int>,
    questionFontSize: Float,
    questionLineSpacing: Float,
    questionLetterSpacing: Float,
    allCorrect: Boolean,
    correctText: String,
    answerResultText: String,
    retryLabel: String,
    retryWrongLabel: String,
    onRetry: () -> Unit,
    onRetryWrongBlanks: (() -> Unit)?
) {
    if (!showResult) {
        Spacer(modifier = Modifier.height(16.dp))
        return
    }

    // Result box
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFD0E8FF))
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (QuestionTypes.isInlineBlank(question.type)) {
                FillAnswerResultText(
                    content = question.content,
                    userAnswer = textAnswer,
                    correctAnswer = resolvedFillAnswer,
                    questionFontSize = questionFontSize,
                    allCorrect = allCorrect,
                    letterSpacing = questionLetterSpacing
                )
            } else if (QuestionTypes.isTextResponse(question.type)) {
                RichText(
                    text = answerResultText,
                    color = if (allCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    fontSize = questionFontSize.sp,
                    fontFamily = LocalFontFamily.current,
                    lineSpacingMultiplier = questionLineSpacing,
                    letterSpacing = questionLetterSpacing
                )
                Spacer(modifier = Modifier.height(12.dp))
                TextResponseAnswerContent(
                    answer = question.answer,
                    questionFontSize = questionFontSize
                )
            } else {
                RichText(
                    text = answerResultText,
                    color = if (allCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    fontSize = questionFontSize.sp,
                    fontFamily = LocalFontFamily.current,
                    lineSpacingMultiplier = questionLineSpacing,
                    letterSpacing = questionLetterSpacing
                )
            }
        }
    }

    // Retry buttons
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(
            onClick = onRetry,
            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = retryLabel
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = retryLabel,
                fontSize = LocalFontSize.current,
                fontFamily = LocalFontFamily.current
            )
        }

        if (QuestionTypes.isInlineBlank(question.type) && onRetryWrongBlanks != null) {
            TextButton(
                onClick = onRetryWrongBlanks,
                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = retryWrongLabel
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = retryWrongLabel,
                    fontSize = LocalFontSize.current,
                    fontFamily = LocalFontFamily.current
                )
            }
        }
    }
}

