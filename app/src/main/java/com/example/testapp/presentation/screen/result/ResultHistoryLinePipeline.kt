package com.example.testapp.presentation.screen.result

import com.example.testapp.domain.model.HistoryRecord

fun formatResultHistoryLine(
    index: Int,
    record: HistoryRecord,
    timeText: String
): String {
    val wrong = record.total - record.score - record.unanswered
    val ratePercent = if (record.total > 0) {
        record.score.toFloat() / record.total.toFloat() * 100f
    } else {
        0f
    }
    return "${index + 1}. 正确:${record.score} 错误:$wrong 正确率:${"%.2f".format(ratePercent)}% 时间:$timeText"
}
