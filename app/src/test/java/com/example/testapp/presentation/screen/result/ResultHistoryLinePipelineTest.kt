package com.example.testapp.presentation.screen.result

import com.example.testapp.domain.model.HistoryRecord
import com.example.testapp.uicommon.screen.result.formatResultHistoryLine
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class ResultHistoryLinePipelineTest {

    @Test
    fun formatResultHistoryLine_includesScoreWrongRateAndTime() {
        val record = HistoryRecord(
            fileName = "quiz.json",
            score = 8,
            total = 10,
            unanswered = 1,
            time = LocalDateTime.of(2026, 6, 28, 12, 0, 0)
        )

        val line = formatResultHistoryLine(0, record, "2026-06-28 12:00:00")

        assertEquals(
            "1. 正确:8 错误:1 正确率:80.00% 时间:2026-06-28 12:00:00",
            line
        )
    }
}
