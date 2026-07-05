package com.example.testapp.presentation.screen.settings

import com.example.testapp.domain.model.HistoryRecord

/** 历史记录 Excel 行数据 — 无 POI / Uri 依赖。 */
object HistoryExcelExportPipeline {

    fun buildSheets(
        history: List<HistoryRecord>,
        headers: List<String>,
        sheetName: String,
    ): Map<String, List<List<String>>> {
        val rows = buildList {
            add(headers)
            history.forEach { h ->
                add(
                    listOf(
                        h.score.toString(),
                        h.total.toString(),
                        h.unanswered.toString(),
                        h.time.toString(),
                    )
                )
            }
        }
        return mapOf(sheetName to rows)
    }
}
