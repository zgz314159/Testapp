package com.example.testapp.presentation.screen.exam.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.core.util.answerToOptionIndices
import com.example.testapp.core.util.isFillAnswerCorrect
import com.example.testapp.core.util.resolveDisplayOptions
import com.example.testapp.core.util.resolveFillCorrectAnswer
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.feature.exam.R
import com.example.testapp.presentation.screen.exam.resolveExamAnswerResultWrongToken
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.component.RichText
import com.example.testapp.uicommon.component.TextResponseAnswerContent
import com.example.testapp.uicommon.design.ReadingCollapsibleSection
import com.example.testapp.uicommon.design.answerFeedbackColors
import com.example.testapp.uicommon.design.resolveAnswerResultPreviewLine
import com.example.testapp.uicommon.util.buildFillAnswerDisplayParts
import com.example.testapp.uicommon.util.stripDrawingTags

@Composable
fun AnswerResultRow(
    question: com.example.testapp.domain.model.Question,
    selectedOption: List<Int>,
    textAnswer: String,
    questionFontSize: Float,
    letterSpacing: Float = 0f
) {
    val correctIndices = answerToOptionIndices(question)
    val displayOptions = resolveDisplayOptions(question)
    val resolvedFillAnswer = resolveFillCorrectAnswer(question)
    val feedbackColors = answerFeedbackColors()
    val correct = if (QuestionTypes.isFill(question.type)) {
        isFillAnswerCorrect(textAnswer, resolvedFillAnswer)
    } else {
        selectedOption.sorted() == correctIndices.sorted()
    }
    val contentColor = if (correct) feedbackColors.correctText else feedbackColors.incorrectText
    val summaryText = if (correct) {
        stringResource(R.string.answer_correct)
    } else {
        stringResource(
            R.string.answer_wrong_format,
            resolveExamAnswerResultWrongToken(
                question = question,
                correctIndices = correctIndices,
                displayOptions = displayOptions,
                resolvedFillAnswer = resolvedFillAnswer
            )
        )
    }
    val previewLine = resolveAnswerResultPreviewLine(summaryText)
    val sectionKey = listOf(question.id, textAnswer, selectedOption, correct)

    ReadingCollapsibleSection(
        containerColor = feedbackColors.resultContainer,
        contentColor = contentColor,
        resetKey = sectionKey,
        collapsedContent = {
            Text(
                text = previewLine,
                color = contentColor,
                fontSize = questionFontSize.sp,
                fontFamily = LocalFontFamily.current,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        },
        expandedContent = {
            Column(modifier = Modifier.fillMaxWidth()) {
                ExamAnswerResultBody(
                    question = question,
                    selectedOption = selectedOption,
                    textAnswer = textAnswer,
                    questionFontSize = questionFontSize,
                    letterSpacing = letterSpacing,
                    correct = correct,
                    correctIndices = correctIndices,
                    displayOptions = displayOptions,
                    resolvedFillAnswer = resolvedFillAnswer,
                    feedbackColors = feedbackColors
                )
            }
        }
    )
}

@Composable
private fun ExamAnswerResultBody(
    question: com.example.testapp.domain.model.Question,
    selectedOption: List<Int>,
    textAnswer: String,
    questionFontSize: Float,
    letterSpacing: Float,
    correct: Boolean,
    correctIndices: List<Int>,
    displayOptions: List<String>,
    resolvedFillAnswer: String,
    feedbackColors: com.example.testapp.uicommon.design.AnswerFeedbackColors
) {
    if (QuestionTypes.isInlineBlank(question.type)) {
        FillAnswerResultText(
            content = question.content,
            userAnswer = textAnswer,
            correctAnswer = resolvedFillAnswer,
            questionFontSize = questionFontSize,
            allCorrect = correct
        )
    } else if (QuestionTypes.isTextResponse(question.type)) {
        Column(modifier = Modifier.fillMaxWidth()) {
            RichText(
                text = if (correct) {
                    stringResource(R.string.answer_correct)
                } else {
                    stringResource(R.string.answer_wrong_format, stripDrawingTags(resolvedFillAnswer))
                },
                color = if (correct) feedbackColors.correctText else feedbackColors.incorrectText,
                fontSize = questionFontSize.sp,
                fontFamily = LocalFontFamily.current
            )
            Spacer(modifier = Modifier.height(12.dp))
            TextResponseAnswerContent(
                answer = question.answer,
                questionFontSize = questionFontSize
            )
        }
    } else {
        val answerText = if (correctIndices.all { it in displayOptions.indices }) {
            correctIndices.joinToString(", ") { displayOptions[it] }
        } else {
            question.answer
        }
        RichText(
            text = if (correct) {
                stringResource(R.string.answer_correct)
            } else {
                stringResource(R.string.answer_wrong_format, answerText)
            },
            color = if (correct) feedbackColors.correctText else feedbackColors.incorrectText,
            fontSize = questionFontSize.sp,
            fontFamily = LocalFontFamily.current
        )
    }
}

@Composable
fun FillAnswerResultText(
    content: String,
    userAnswer: String,
    correctAnswer: String,
    questionFontSize: Float,
    allCorrect: Boolean,
    letterSpacing: Float = 0f
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
                        append(" ($appendedCorrect)")
                    }
                }
            }
        }
    }

    Text(text = annotated, style = style)
}
