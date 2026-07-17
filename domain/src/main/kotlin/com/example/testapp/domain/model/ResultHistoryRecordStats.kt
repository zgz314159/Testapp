package com.example.testapp.domain.model

/**
 * 纯函数统计入口 — 计算单条历史记录的各项指标。
 * 折线图、历史列表必须共同使用该函数。
 */
data class ResultHistoryRecordStats(
    val answered: Int,
    val correct: Int,
    val wrong: Int,
    val rate: Double,
    val rateText: String,
)

fun calculateResultHistoryRecordStats(record: HistoryRecord): ResultHistoryRecordStats {
    val safeTotal = record.total.coerceAtLeast(0)
    val unanswered = record.unanswered.coerceIn(0, safeTotal)
    val answered = safeTotal - unanswered
    val correct = record.score.coerceIn(0, answered)
    val wrong = answered - correct
    val rate = if (answered > 0) correct.toDouble() / answered else 0.0
    return ResultHistoryRecordStats(
        answered = answered,
        correct = correct,
        wrong = wrong,
        rate = rate,
        rateText = formatResultPercent(rate),
    )
}

/**
 * 统一百分比格式化。
 */
fun formatResultPercent(rate: Double): String {
    val percent = rate.coerceIn(0.0, 1.0) * 100.0
    val formatted = String.format(java.util.Locale.US, "%.2f", percent).trimEnd('0').trimEnd('.')
    return "$formatted%"
}