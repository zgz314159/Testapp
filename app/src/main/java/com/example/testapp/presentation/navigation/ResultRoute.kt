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
        onBackHome = onBackHome,
        onViewDetail = onViewDetail,
        onBack = onBack,
    )
}
