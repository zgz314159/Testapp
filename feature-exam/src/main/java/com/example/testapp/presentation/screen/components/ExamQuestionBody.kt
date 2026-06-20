package com.example.testapp.presentation.screen.exam.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.core.util.resolveFillCorrectAnswer
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.presentation.screen.exam.components.ExamOptionsList as LocalExamOptionsList

@Composable
fun ExamQuestionBody(
    question: com.example.testapp.domain.model.Question,
    questionFontSize: Float,
    lineSpacingMultiplier: Float,
    selectedOption: List<Int>,
    textAnswer: String,
    showResult: Boolean,
    onOptionClick: (Int) -> Unit,
    onTextAnswerChange: (String) -> Unit
) {
    if (QuestionTypes.isInlineBlank(question.type)) {
        InlineBlankQuestionContent(
            content = question.content,
            answerText = textAnswer,
            correctAnswer = resolveFillCorrectAnswer(question),
            questionFontSize = questionFontSize,
            lineSpacingMultiplier = lineSpacingMultiplier,
            showResult = showResult,
            onAnswerChange = onTextAnswerChange
        )
        Spacer(modifier = Modifier.height(16.dp))
    } else if (QuestionTypes.isTextResponse(question.type)) {
        TextAnswerQuestionContent(
            content = question.content,
            answerText = textAnswer,
            questionFontSize = questionFontSize,
            lineSpacingMultiplier = lineSpacingMultiplier,
            showResult = showResult,
            stemImages = question.stemImages,
            onAnswerChange = onTextAnswerChange
        )
        Spacer(modifier = Modifier.height(16.dp))
    } else {
        StemContent(
            content = question.content,
            stemImages = question.stemImages,
            fontSize = questionFontSize.sp,
            fontFamily = LocalFontFamily.current
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 使用共享的选项列表组件
        LocalExamOptionsList(
            question = question,
            questionFontSize = questionFontSize,
            lineSpacingMultiplier = lineSpacingMultiplier,
            selectedOption = selectedOption,
            showResult = showResult,
            onOptionClick = onOptionClick
        )
    }
}


