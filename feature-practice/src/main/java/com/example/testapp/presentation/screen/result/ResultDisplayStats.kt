package com.example.testapp.presentation.screen.result

import com.example.testapp.domain.model.HistoryRecord
import java.util.Locale

data class ResultDisplayStats(
    val isExamMode: Boolean,
    val modeText: String,
    val fileName: String,
    val currentLabel: String,
    val overallLabel: String,
    val currentScore: Int,
    val currentTotal: Int,
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
    val accuracyList: List<Float>,
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
    totalQuestions: Int,
): ResultDisplayStats {
    val isExamMode = quizId.startsWith("exam_")
    val isAdaptiveMode = quizId.startsWith("adaptive_")
    val latest = historyList.maxByOrNull { it.time }
    val currentFileName = latest?.fileName.orEmpty()
    val sameFileHistory = historyList.filter { it.fileName == currentFileName }.sortedBy { it.time }

    val safeTotal = total.coerceAtLeast(0)
    val safeUnanswered = unanswered.coerceIn(0, safeTotal)
    val currentAnswered = (safeTotal - safeUnanswered).coerceAtLeast(0)
    val currentScore = score.coerceIn(0, currentAnswered)
    val currentWrong = currentAnswered - currentScore
    val currentRate = if (currentAnswered > 0) currentScore.toDouble() / currentAnswered else 0.0
    val overallTotal =
        if (isAdaptiveMode) safeTotal else totalQuestions.takeIf { it > 0 } ?: (latest?.total ?: 0)

    val (rawOverallScore, rawOverallAnswered) =
        when {
            isAdaptiveMode -> currentScore to currentAnswered
            isExamMode -> (cumulativeCorrect ?: currentScore) to (cumulativeAnswered ?: currentAnswered)
            else -> {
                val practiceScore = cumulativeCorrect ?: run {
                    if (sameFileHistory.isNotEmpty()) {
                        val latestRecord = sameFileHistory.first()
                        val totalAnswered = (overallTotal - latestRecord.unanswered).coerceAtLeast(0)
                        minOf(sameFileHistory.maxOf { it.score } + currentScore, totalAnswered)
                    } else {
                        0
                    }
                }
                val practiceAnswered = cumulativeAnswered ?: run {
                    if (sameFileHistory.isNotEmpty()) {
                        (overallTotal - sameFileHistory.first().unanswered).coerceAtLeast(0)
                    } else {
                        0
                    }
                }
                practiceScore to practiceAnswered
            }
        }
    val overallAnswered = rawOverallAnswered.coerceAtLeast(0)
    val overallScore = rawOverallScore.coerceIn(0, overallAnswered)
    val overallWrong = overallAnswered - overallScore
    val overallUnanswered = (overallTotal - overallAnswered).coerceAtLeast(0)
    val overallRate = if (overallAnswered > 0) overallScore.toDouble() / overallAnswered else 0.0
    val (modeText, fileName) = when {
        quizId.startsWith("exam_") -> "考试" to quizId.removePrefix("exam_")
        quizId.startsWith("practice_") -> "练习" to quizId.removePrefix("practice_")
        quizId.startsWith("adaptive_") -> "自适应渐隐" to quizId.removePrefix("adaptive_")
        else -> "练习" to quizId
    }

    return ResultDisplayStats(
        isExamMode = isExamMode,
        modeText = modeText,
        fileName = fileName,
        currentLabel = if (isExamMode) "本次考试" else "本次练习",
        overallLabel = when {
            isExamMode -> "考试总计"
            isAdaptiveMode -> "本轮渐隐"
            else -> "题库总计"
        },
        currentScore = currentScore,
        currentTotal = safeTotal,
        currentUnanswered = safeUnanswered,
        currentAnswered = currentAnswered,
        currentWrong = currentWrong,
        currentRate = currentRate,
        currentRateText = formatResultPercent(currentRate),
        overallScore = overallScore,
        overallAnswered = overallAnswered,
        overallWrong = overallWrong,
        overallUnanswered = overallUnanswered,
        overallTotal = overallTotal,
        displayOverallTotal = overallAnswered,
        overallRateText = formatResultPercent(overallRate),
        sameFileHistory = sameFileHistory,
        actualExamCount = cumulativeExamCount ?: sameFileHistory.size,
        accuracyList = sameFileHistory.takeLast(9).map { record ->
            val answered = (record.total - record.unanswered).coerceAtLeast(0)
            if (answered > 0) {
                (record.score.toFloat() / answered).coerceIn(0f, 1f)
            } else {
                0f
            }
        },
    )
}

fun formatResultPercent(rate: Double): String {
    val percent = rate.coerceIn(0.0, 1.0) * 100.0
    return String.format(Locale.US, "%.2f", percent).trimEnd('0').trimEnd('.')
}
