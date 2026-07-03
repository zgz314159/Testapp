package com.example.testapp.presentation.screen.practice



import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Spacer

import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.layout.height

import androidx.compose.material3.Text

import androidx.compose.runtime.Composable

import androidx.compose.ui.Modifier

import androidx.compose.ui.text.style.TextOverflow

import androidx.compose.ui.unit.dp

import androidx.compose.ui.unit.sp

import com.example.testapp.domain.QuestionTypes

import com.example.testapp.domain.model.Question

import com.example.testapp.presentation.screen.components.FillAnswerResultText

import com.example.testapp.uicommon.component.LocalFontFamily

import com.example.testapp.uicommon.component.RichText

import com.example.testapp.uicommon.component.TextResponseAnswerContent

import com.example.testapp.uicommon.design.ReadingCollapsibleSection

import com.example.testapp.uicommon.design.answerFeedbackColors

import com.example.testapp.uicommon.design.resolveAnswerResultPreviewLine



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

    answerResultText: String,

    onInteraction: () -> Unit = {}

) {

    if (!showResult) {

        Spacer(modifier = Modifier.height(16.dp))

        return

    }



    val feedbackColors = answerFeedbackColors()

    val contentColor = if (allCorrect) feedbackColors.correctText else feedbackColors.incorrectText

    val previewLine = resolveAnswerResultPreviewLine(answerResultText)



    ReadingCollapsibleSection(

        containerColor = feedbackColors.resultContainer,

        contentColor = contentColor,

        resetKey = listOf(question.id, answerResultText, textAnswer),

        onInteraction = onInteraction,

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

                        color = contentColor,

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

                        color = contentColor,

                        fontSize = questionFontSize.sp,

                        fontFamily = LocalFontFamily.current,

                        lineSpacingMultiplier = questionLineSpacing,

                        letterSpacing = questionLetterSpacing

                    )

                }

            }

        }

    )

}


