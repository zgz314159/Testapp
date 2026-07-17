package com.example.testapp.uicommon.design

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap

@Composable
fun QuestionSessionHeader(
    questionTypeLabel: String,
    currentIndex: Int,
    total: Int,
    modifier: Modifier = Modifier,
    questionListLabel: String? = null,
    onOpenQuestionList: (() -> Unit)? = null,
    extraContent: @Composable ColumnScope.() -> Unit = {}
) {
    Column(modifier = modifier.fillMaxWidth()) {
        LinearProgressIndicator(
            progress = { if (total > 0) (currentIndex + 1f) / total else 0f },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp),
            strokeCap = StrokeCap.Round,
        )
        Spacer(modifier = Modifier.height(AppSpacing.md))
        AppCard(
            contentPadding = Modifier,
            containerColor = questionSessionFloatingContainerColor(),
        ) {
            Column(modifier = Modifier.padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md)) {
                QuestionCardHeaderRow(
                    questionTypeLabel = questionTypeLabel,
                    progressLabel = "${currentIndex + 1}/$total",
                    questionListLabel = questionListLabel,
                    onOpenQuestionList = onOpenQuestionList
                )
                extraContent()
            }
        }
    }
}
