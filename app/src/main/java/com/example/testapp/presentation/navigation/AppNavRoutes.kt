package com.example.testapp.presentation.navigation

import androidx.navigation.NavHostController

// Practice/Exam result navigation
internal fun NavHostController.navToResult(
    prefix: String, quizId: String, score: Int, total: Int, unanswered: Int,
    cumulativeCorrect: Int?, cumulativeAnswered: Int?, cumulativeExamCount: Int? = null,
    sessionProgressId: String? = null,
    popUpTo: String = "home"
) {
    val id = "${prefix}_$quizId"
    val e = java.net.URLEncoder.encode(id, "UTF-8")
    val extras = buildString {
        append("result/$score/$total/$unanswered/$e")
        append("?cumulativeCorrect=${cumulativeCorrect ?: -1}")
        append("&cumulativeAnswered=${cumulativeAnswered ?: -1}")
        if (cumulativeExamCount != null) append("&cumulativeExamCount=$cumulativeExamCount")
        if (!sessionProgressId.isNullOrBlank()) {
            append("&sessionProgressId=${java.net.URLEncoder.encode(sessionProgressId, "UTF-8")}")
        }
    }
    navigate(extras) { popUpTo(popUpTo) { inclusive = false } }
}
