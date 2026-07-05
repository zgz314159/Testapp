package com.example.testapp.presentation.screen.result

import com.example.testapp.domain.model.HistoryRecord

data class ResultDisplayStats(
    val isExamMode: Boolean,
    val modeText: String,
    val fileName: String,
    val currentLabel: String,
    val overallLabel: String,
    val currentScore: Int,
    val currentActualAnswered: Int,
    val currentUnanswered: Int,
    val currentAnswered: Int,
    val currentWrong: Int,
    val currentRate: Double,
    val currentRateText: String,
    val overallScore: Int,
    val overallAnswered: Int,
    val overallWrong: Int,
    val overallUnanswered: Int,
    val overallTotal: Int,
    val displayOverallTotal: Int,
    val overallRateText: String,
    val sameFileHistory: List<HistoryRecord>,
    val actualExamCount: Int,
    val accuracyList: List<Float>
)

fun buildResultDisplayStats(
    quizId: String,
    score: Int,
    total: Int,
    unanswered: Int,
    cumulativeCorrect: Int?,
    cumulativeAnswered: Int?,
    cumulativeExamCount: Int?,
    historyList: List<HistoryRecord>,
    totalQuestions: Int
): ResultDisplayStats {
    val isExamMode = quizId.startsWith("exam_")
    val latest = historyList.maxByOrNull { it.time }
    val currentFileName = latest?.fileName ?: ""
    val sameFileHistory = historyList.filter { it.fileName == currentFileName }
    val overallTotal = totalQuestions.takeIf { it > 0 } ?: (latest?.total ?: 0)

    val (currentAnswered, currentWrong) = if (isExamMode) {
        if (latest == null) {
            total to (total - score).coerceAtLeast(0)
        } else {
            val previousAnswered = latest.total
            val newAnswered = (total - previousAnswered).coerceAtLeast(0)
            newAnswered to (newAnswered - score).coerceAtLeast(0)
        }
    } else {
        total to (total - score).coerceAtLeast(0)
    }

    val currentRate = if (currentAnswered > 0) score.toDouble() / currentAnswered else 0.0
    val (overallScore, overallAnswered) = if (isExamMode) {
        (cumulativeCorrect ?: score) to (cumulativeAnswered ?: total)
    } else {
        val practiceScore = cumulativeCorrect ?: run {
            if (sameFileHistory.isNotEmpty()) {
                val latestRecord = sameFileHistory.first()
                val totalAnswered = overallTotal - latestRecord.unanswered
                val bestHistoricalScore = sameFileHistory.maxOf { it.score }
                minOf(bestHistoricalScore + score, totalAnswered)
            } else {
                0
            }
        }
        val practiceAnswered = cumulativeAnswered ?: run {
            if (sameFileHistory.isNotEmpty()) {
                overallTotal - sameFileHistory.first().unanswered
            } else {
                0
            }
        }
        practiceScore to practiceAnswered
    }

    val overallUnanswered = if (isExamMode) {
        (overallTotal - overallAnswered).coerceAtLeast(0)
    } else if (sameFileHistory.isNotEmpty()) {
        sameFileHistory.first().unanswered
    } else {
        overallTotal
    }
    val overallWrong = (overallAnswered - overallScore).coerceAtLeast(0)
    val overallRate = if (overallAnswered > 0) overallScore.toFloat() / overallAnswered else 0f
    val (modeText, fileName) = when {
        quizId.startsWith("exam_") -> "考试" to quizId.removePrefix("exam_")
        quizId.startsWith("practice_") -> "练习" to quizId.removePrefix("practice_")
        else -> "练习" to quizId
    }

    return ResultDisplayStats(
        isExamMode = isExamMode,
        modeText = modeText,
        fileName = fileName,
        currentLabel = if (isExamMode) "本次考试：" else "本次练习：",
        overallLabel = if (isExamMode) "考试总计：" else "题库总计：",
        currentScore = score,
        currentActualAnswered = total,
        currentUnanswered = unanswered,
        currentAnswered = currentAnswered,
        currentWrong = currentWrong,
        currentRate = currentRate,
        currentRateText = String.format("%.2f", currentRate * 100),
        overallScore = overallScore,
        overallAnswered = overallAnswered,
        overallWrong = overallWrong,
        overallUnanswered = overallUnanswered,
        overallTotal = overallTotal,
        displayOverallTotal = if (isExamMode) overallTotal else overallAnswered,
        overallRateText = String.format("%.2f", overallRate * 100),
        sameFileHistory = sameFileHistory,
        actualExamCount = cumulativeExamCount ?: sameFileHistory.size,
        accuracyList = historyList.map { if (it.total > 0) it.score.toFloat() / it.total else 0f }
    )
}
