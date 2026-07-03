package com.example.testapp.presentation.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.core.util.isFillAnswerCorrect
import com.example.testapp.feature.practice.R
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.component.LocalFontSize
import com.example.testapp.uicommon.design.answerFeedbackColors
import com.example.testapp.uicommon.util.buildFillAnswerDisplayParts

@Composable
fun FillAnswerResultText(
    content: String,
    userAnswer: String,
    correctAnswer: String,
    questionFontSize: Float,
    allCorrect: Boolean,
    letterSpacing: Float
) {
    val feedbackColors = answerFeedbackColors()
    val style = TextStyle(
        fontSize = questionFontSize.sp,
        lineHeight = (questionFontSize * 1.28f).sp,
        letterSpacing = letterSpacing.sp,
        fontFamily = LocalFontFamily.current,
        color = if (allCorrect) feedbackColors.correctText else feedbackColors.incorrectText
    )

    if (allCorrect) {
        Text(text = stringResource(R.string.answer_correct), style = style)
        return
    }

    val parts = buildFillAnswerDisplayParts(content, correctAnswer, userAnswer)
    val annotated = buildAnnotatedString {
        append(stringResource(R.string.answer_wrong_format, ""))
        parts.forEachIndexed { index, part ->
            if (index > 0) append("；")
            withStyle(
                SpanStyle(
                    color = if (part.isCorrect) feedbackColors.correctText else feedbackColors.incorrectText
                )
            ) {
                append(part.label)
                append(part.value)
                part.appendedCorrectValue?.let { appendedCorrect ->
                    withStyle(
                        SpanStyle(
                            color = feedbackColors.incorrectHintText,
                            fontSize = (questionFontSize - 2f).coerceAtLeast(10f).sp
                        )
                    ) {
                        append("（$appendedCorrect）")
                    }
                }
            }
        }
    }

    Text(text = annotated, style = style)
}

@Composable
fun FillBlankAnswerField(
    answerText: String,
    correctAnswer: String,
    showResult: Boolean,
    onAnswerChange: (String) -> Unit
) {
    val isCorrect = showResult && isFillAnswerCorrect(answerText, correctAnswer)
    val displayValue = if (!showResult) {
        answerText
    } else if (isCorrect) {
        answerText
    } else {
        "（$correctAnswer）"
    }

    val feedbackColors = answerFeedbackColors()
    val bgColor = when {
        !showResult -> MaterialTheme.colorScheme.surface
        isCorrect -> feedbackColors.correctFieldBackground
        else -> feedbackColors.incorrectFieldBackground
    }

    OutlinedTextField(
        value = displayValue,
        onValueChange = { onAnswerChange(it) },
        enabled = !showResult,
        placeholder = {
            Text(
                text = "在此输入答案",
                fontSize = LocalFontSize.current,
                fontFamily = LocalFontFamily.current
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(vertical = 4.dp),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontSize = LocalFontSize.current,
            fontFamily = LocalFontFamily.current,
            lineHeight = (LocalFontSize.current.value * 1.2f).sp
        )
    )
}
