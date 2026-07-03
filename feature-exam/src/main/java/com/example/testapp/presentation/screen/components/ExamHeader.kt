package com.example.testapp.presentation.screen.exam.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.testapp.uicommon.design.QuestionSessionHeader

@Composable
fun ExamHeader(
    questionType: String,
    currentIndex: Int,
    total: Int,
    modifier: Modifier = Modifier,
    questionListLabel: String? = null,
    onOpenQuestionList: (() -> Unit)? = null,
    extraContent: @Composable ColumnScope.() -> Unit = {}
) {
    QuestionSessionHeader(
        questionTypeLabel = questionType,
        currentIndex = currentIndex,
        total = total,
        modifier = modifier,
        questionListLabel = questionListLabel,
        onOpenQuestionList = onOpenQuestionList,
        extraContent = extraContent
    )
}
