package com.example.testapp.presentation.screen.result

import com.example.testapp.domain.model.HistoryRecord
import com.example.testapp.domain.model.calculateResultHistoryRecordStats
import com.example.testapp.domain.model.formatResultPercent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class ResultDisplayStatsDashboardTest {

    // ===== calculateResultHistoryRecordStats tests =====

    @Test
    fun `stats total 11 score 0 unanswered 11 gives answered 0 wrong 0 rate 0 percent`() {
        val record = HistoryRecord(score = 0, total = 11, unanswered = 11,
            fileName = "test", time = LocalDateTime.now())
        val stats = calculateResultHistoryRecordStats(record)
        assertEquals(0, stats.answered)
        assertEquals(0, stats.correct)
        assertEquals(0, stats.wrong)
        assertEquals(0.0, stats.rate, 0.0001)
        assertEquals("0%", stats.rateText)
    }

    @Test
    fun `stats total 2 score 1 unanswered 0 gives answered 2 wrong 1 rate 50 percent`() {
        val record = HistoryRecord(score = 1, total = 2, unanswered = 0,
            fileName = "test", time = LocalDateTime.now())
        val stats = calculateResultHistoryRecordStats(record)
        assertEquals(2, stats.answered)
        assertEquals(1, stats.correct)
        assertEquals(1, stats.wrong)
        assertEquals(0.5, stats.rate, 0.0001)
        assertEquals("50%", stats.rateText)
    }

    @Test
    fun `stats total 12 score 7 unanswered 0 gives wrong 5 rate 58 dot 33 percent`() {
        val record = HistoryRecord(score = 7, total = 12, unanswered = 0,
            fileName = "test", time = LocalDateTime.now())
        val stats = calculateResultHistoryRecordStats(record)
        assertEquals(12, stats.answered)
        assertEquals(7, stats.correct)
        assertEquals(5, stats.wrong)
        assertEquals(7.0 / 12.0, stats.rate, 0.0001)
        assertEquals("58.33%", stats.rateText)
    }

    @Test
    fun `stats score greater than answered clamps correctly`() {
        val record = HistoryRecord(score = 99, total = 10, unanswered = 0,
            fileName = "test", time = LocalDateTime.now())
        val stats = calculateResultHistoryRecordStats(record)
        assertEquals(10, stats.answered)
        assertEquals(10, stats.correct)
        assertEquals(0, stats.wrong)
        assertEquals(1.0, stats.rate, 0.0001)
        assertEquals("100%", stats.rateText)
    }

    @Test
    fun `stats unanswered greater than total clamps correctly`() {
        val record = HistoryRecord(score = 1, total = 5, unanswered = 99,
            fileName = "test", time = LocalDateTime.now())
        val stats = calculateResultHistoryRecordStats(record)
        assertEquals(0, stats.answered)
        assertEquals(0, stats.correct)
        assertEquals(0, stats.wrong)
        assertEquals(0.0, stats.rate, 0.0001)
        assertEquals("0%", stats.rateText)
    }

    @Test
    fun `same record in history list and chart produces identical rate`() {
        val record = HistoryRecord(score = 7, total = 12, unanswered = 0,
            fileName = "test", time = LocalDateTime.now())
        val recordStats = calculateResultHistoryRecordStats(record)

        val chartRate = recordStats.rate.toFloat()
        val expectedChartValue = 7f / 12f
        assertEquals(expectedChartValue, chartRate, 0.001f)
        assertEquals(recordStats.rateText, "58.33%")
        assertEquals(recordStats.rateText, "58.33%")
    }

    @Test
    fun `12 history records take last 9 and retain 4th to 12th sequence numbers`() {
        val history = (1..12).map { index ->
            HistoryRecord(
                score = index,
                total = 10,
                unanswered = 0,
                fileName = "bank.xlsx",
                time = LocalDateTime.of(2026, 7, 1, 0, index),
            )
        }
        val stats = buildStats(history = history)
        assertEquals(9, stats.accuracyList.size)
        // sameFileHistory has all 12 records matching the file name
        assertEquals(12, stats.sameFileHistory.size)
        // accuracyList uses takeLast(9): first element is record 4 (score=4 -> rate=0.4)
        assertEquals(0.4f, stats.accuracyList.first(), 0.001f)
        assertEquals(1f, stats.accuracyList.last(), 0.001f)
    }

    @Test
    fun `0 or 1 history records produce empty chart state`() {
        assertTrue(buildStats(history = emptyList()).accuracyList.isEmpty())
        val singleHistory = listOf(
            HistoryRecord(score = 5, total = 10, unanswered = 0,
                fileName = "bank.xlsx", time = LocalDateTime.now())
        )
        val statsWithOne = buildStats(history = singleHistory)
        assertTrue(statsWithOne.accuracyList.size < 2)
    }

    // ===== formatResultPercent tests =====

    @Test
    fun `percent formatting covers zero half full and sample`() {
        assertEquals("0%", formatResultPercent(0.0))
        assertEquals("50%", formatResultPercent(0.5))
        assertEquals("58.33%", formatResultPercent(7.0 / 12.0))
        assertEquals("100%", formatResultPercent(1.0))
        assertEquals("100%", formatResultPercent(5.0))
    }

    // ===== buildResultDisplayStats tests =====

    @Test
    fun `current metrics always add up to total`() {
        val stats = buildStats(score = 7, total = 10, unanswered = 2)
        assertEquals(8, stats.currentAnswered)
        assertEquals(1, stats.currentWrong)
        assertEquals(stats.currentTotal, stats.currentScore + stats.currentWrong + stats.currentUnanswered)
        assertEquals("87.5%", stats.currentRateText)
    }

    @Test
    fun `invalid current values are safely clamped`() {
        val stats = buildStats(score = 99, total = 10, unanswered = -3)
        assertEquals(10, stats.currentScore)
        assertEquals(0, stats.currentWrong)
        assertEquals(0, stats.currentUnanswered)
        assertEquals("100%", stats.currentRateText)
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
        val stats = buildResultDisplayStats(
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
