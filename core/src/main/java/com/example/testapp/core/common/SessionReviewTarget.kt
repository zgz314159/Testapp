package com.example.testapp.core.common

data class SessionReviewTarget(
    val progressId: String,
    val quizFileName: String,
    val isExam: Boolean,
    val isWrongBookMode: Boolean,
    val isFavoriteMode: Boolean,
    val questionCount: Int,
    val randomEnabled: Boolean
)

fun parseProgressScopeCount(progressId: String): Int {
    val scope = progressId.substringAfter(PROGRESS_SCOPE_MARKER, "")
    if (scope.isEmpty()) return 0
    return scope.split(';')
        .firstOrNull { it.startsWith("q=") }
        ?.removePrefix("q=")
        ?.takeIf { it != "all" }
        ?.toIntOrNull()
        ?: 0
}

fun parseProgressScopeRandom(progressId: String): Boolean =
    progressId.substringAfter(PROGRESS_SCOPE_MARKER, "").contains("r=1")

fun parseExamReviewTarget(progressId: String): SessionReviewTarget =
    SessionReviewTarget(
        progressId = progressId,
        quizFileName = examProgressBaseId(progressId).removePrefix("exam_"),
        isExam = true,
        isWrongBookMode = false,
        isFavoriteMode = false,
        questionCount = parseProgressScopeCount(progressId),
        randomEnabled = parseProgressScopeRandom(progressId)
    )

fun parsePracticeReviewTarget(progressId: String): SessionReviewTarget {
    val base = practiceProgressBaseId(progressId).removePrefix("practice_")
    return when {
        base.startsWith("wrongbook_") -> SessionReviewTarget(
            progressId = progressId,
            quizFileName = base.removePrefix("wrongbook_"),
            isExam = false,
            isWrongBookMode = true,
            isFavoriteMode = false,
            questionCount = parseProgressScopeCount(progressId),
            randomEnabled = parseProgressScopeRandom(progressId)
        )
        base.startsWith("favorite_") -> SessionReviewTarget(
            progressId = progressId,
            quizFileName = base.removePrefix("favorite_"),
            isExam = false,
            isWrongBookMode = false,
            isFavoriteMode = true,
            questionCount = parseProgressScopeCount(progressId),
            randomEnabled = parseProgressScopeRandom(progressId)
        )
        else -> SessionReviewTarget(
            progressId = progressId,
            quizFileName = base,
            isExam = false,
            isWrongBookMode = false,
            isFavoriteMode = false,
            questionCount = parseProgressScopeCount(progressId),
            randomEnabled = parseProgressScopeRandom(progressId)
        )
    }
}

private const val PROGRESS_SCOPE_MARKER = "__scope="
