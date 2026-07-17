package com.example.testapp.presentation.navigation

import androidx.compose.runtime.Composable
import com.example.testapp.presentation.screen.result.ResultScreen

@Composable
fun ResultRoute(
    score: Int,
    total: Int,
    unanswered: Int,
    quizId: String,
    cumulativeCorrect: Int?,
    cumulativeAnswered: Int?,
    cumulativeExamCount: Int?,
    detailEnabled: Boolean = true,
    onBackHome: () -> Unit,
    onViewDetail: () -> Unit,
    onBack: () -> Unit = onBackHome,
) {
    ResultScreen(
        score = score,
        total = total,
        unanswered = unanswered,
        quizId = quizId,
        cumulativeCorrect = cumulativeCorrect,
        cumulativeAnswered = cumulativeAnswered,
        cumulativeExamCount = cumulativeExamCount,
        detailEnabled = detailEnabled,
        onBackHome = onBackHome,
        onViewDetail = onViewDetail,
        onBack = onBack,
    )
}
