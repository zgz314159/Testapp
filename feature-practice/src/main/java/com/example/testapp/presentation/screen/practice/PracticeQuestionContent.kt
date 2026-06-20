package com.example.testapp.presentation.screen.practice

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.Question
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.presentation.screen.components.ExamOptionsList
import com.example.testapp.presentation.screen.components.InlineBlankQuestionContent
import com.example.testapp.presentation.screen.components.FillBlankAnswerField
import com.example.testapp.presentation.screen.components.StemContent
import com.example.testapp.uicommon.component.StemImagesSection
import com.example.testapp.presentation.screen.components.TextAnswerQuestionContent

@Composable
fun PracticeQuestionContent(
    question: Question,
    textAnswer: String,
    showResult: Boolean,
    selectedOption: List<Int>,
    displayOptions: List<String>,
    resolvedFillAnswer: String,
    questionFontSize: Float,
    questionLineSpacing: Float,
    questionLetterSpacing: Float,
    onAnswerChange: (String) -> Unit,
    onOptionClick: (Int) -> Unit,
    submitCurrentAnswer: (Int?) -> Unit
) {
    if (QuestionTypes.isInlineBlank(question.type)) {
        Column {
            InlineBlankQuestionContent(
                content = question.content,
                answerText = textAnswer,
                correctAnswer = resolvedFillAnswer,
                questionFontSize = questionFontSize,
                lineSpacingMultiplier = questionLineSpacing,
                letterSpacing = questionLetterSpacing,
                showResult = showResult,
                onAnswerChange = onAnswerChange
            )
            if (question.stemImages.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                StemImagesSection(
                    imagePaths = question.stemImages,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    } else if (QuestionTypes.isTextResponse(question.type)) {
        TextAnswerQuestionContent(
            content = question.content,
            answerText = textAnswer,
            questionFontSize = questionFontSize,
            lineSpacingMultiplier = questionLineSpacing,
            letterSpacing = questionLetterSpacing,
            showResult = showResult,
            stemImages = question.stemImages,
            onAnswerChange = onAnswerChange
        )
    } else if (QuestionTypes.isFill(question.type)) {
        Column {
            StemContent(
                content = question.content,
                stemImages = question.stemImages,
                fontSize = questionFontSize.sp,
                fontFamily = LocalFontFamily.current,
                lineSpacingMultiplier = questionLineSpacing,
                letterSpacing = questionLetterSpacing
            )
            Spacer(modifier = Modifier.height(16.dp))
            FillBlankAnswerField(
                answerText = textAnswer,
                correctAnswer = resolvedFillAnswer,
                showResult = showResult,
                onAnswerChange = onAnswerChange
            )
        }
    } else {
        StemContent(
            content = question.content,
            stemImages = question.stemImages,
            fontSize = questionFontSize.sp,
            fontFamily = LocalFontFamily.current,
            lineSpacingMultiplier = questionLineSpacing,
            letterSpacing = questionLetterSpacing
        )
        Spacer(modifier = Modifier.height(16.dp))

        ExamOptionsList(
            question = question,
            questionFontSize = questionFontSize,
            lineSpacingMultiplier = questionLineSpacing,
            letterSpacing = questionLetterSpacing,
            selectedOption = selectedOption,
            showResult = showResult,
            onOptionClick = { idx ->
                if (QuestionTypes.isSingle(question.type) || QuestionTypes.isJudge(question.type)) {
                    submitCurrentAnswer(idx)
                } else {
                    onOptionClick(idx)
                }
            }
        )
    }
}
