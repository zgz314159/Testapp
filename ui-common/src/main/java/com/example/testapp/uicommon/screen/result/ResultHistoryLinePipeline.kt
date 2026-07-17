package com.example.testapp.uicommon.screen.result

import com.example.testapp.domain.model.HistoryRecord
import com.example.testapp.domain.model.calculateResultHistoryRecordStats

/**
 * 历史记录行格式化（已废弃，请直接使用 ResultHistoryRecordStats + 独立卡片布局）。
 * 保留仅为兼容旧引用。
 */
fun formatResultHistoryLine(
    index: Int,
    record: HistoryRecord,
    timeText: String
): String {
    val stats = calculateResultHistoryRecordStats(record)
    return "${index + 1}. 正确:${stats.correct} 错误:${stats.wrong} 正确率:${stats.rateText} 时间:$timeText"
}
