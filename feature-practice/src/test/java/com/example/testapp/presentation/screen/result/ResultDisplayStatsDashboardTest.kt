package com.example.testapp.presentation.screen.result

import com.example.testapp.domain.model.HistoryRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class ResultDisplayStatsDashboardTest {
    @Test
    fun `current metrics always add up to total`() {
        val stats = buildStats(score = 7, total = 10, unanswered = 2)

        assertEquals(8, stats.currentAnswered)
        assertEquals(1, stats.currentWrong)
        assertEquals(stats.currentTotal, stats.currentScore + stats.currentWrong + stats.currentUnanswered)
        assertEquals("87.5", stats.currentRateText)
    }

    @Test
    fun `invalid current values are safely clamped`() {
        val stats = buildStats(score = 99, total = 10, unanswered = -3)

        assertEquals(10, stats.currentScore)
        assertEquals(0, stats.currentWrong)
        assertEquals(0, stats.currentUnanswered)
        assertEquals("100", stats.currentRateText)
    }

    @Test
    fun `percent formatting covers zero half and full`() {
        assertEquals("0", formatResultPercent(0.0))
        assertEquals("50", formatResultPercent(0.5))
        assertEquals("100", formatResultPercent(1.0))
        assertEquals("100", formatResultPercent(5.0))
    }

    @Test
    fun `history trend keeps latest nine and clamps rates`() {
        val history = (1..12).map { index ->
            HistoryRecord(
                score = if (index == 12) 20 else index,
                total = 10,
                unanswered = 0,
                fileName = "bank.xlsx",
                time = LocalDateTime.of(2026, 7, 1, 0, index),
            )
        }

        val stats = buildStats(history = history)

        assertEquals(9, stats.accuracyList.size)
        assertEquals(0.4f, stats.accuracyList.first())
        assertEquals(1f, stats.accuracyList.last())
        assertTrue(stats.accuracyList.all { it in 0f..1f })
    }

    @Test
    fun `no history produces empty trend`() {
        assertTrue(buildStats(history = emptyList()).accuracyList.isEmpty())
    }

    @Test
    fun `adaptive result exposes its mode and original bank name`() {
        val stats =
            buildResultDisplayStats(
                quizId = "adaptive_bank.sqlite",
                score = 8,
                total = 10,
                unanswered = 0,
                cumulativeCorrect = null,
                cumulativeAnswered = null,
                cumulativeExamCount = null,
                historyList = emptyList(),
                totalQuestions = 100,
            )

        assertEquals("自适应渐隐", stats.modeText)
        assertEquals("bank.sqlite", stats.fileName)
        assertEquals(8, stats.overallScore)
        assertEquals(10, stats.overallAnswered)
        assertEquals("本轮渐隐", stats.overallLabel)
    }

    private fun buildStats(
        score: Int = 1,
        total: Int = 10,
        unanswered: Int = 0,
        history: List<HistoryRecord> = emptyList(),
    ): ResultDisplayStats = buildResultDisplayStats(
        quizId = "practice_bank.xlsx",
        score = score,
        total = total,
        unanswered = unanswered,
        cumulativeCorrect = null,
        cumulativeAnswered = null,
        cumulativeExamCount = null,
        historyList = history,
        totalQuestions = 10,
    )
}
