package com.example.testapp.uicommon.component

/** 紧凑答题卡视觉行：词条行（5 列）+ 可选分题号行（以词条列为中心向两侧排）。 */
object AnswerCardEntryGridLines {

    const val COLUMNS = 5

    sealed interface DisplayLine {
        data class EntryLine(val entries: List<AnswerCardEntryCompactLayout.EntryRow>) : DisplayLine
        data class RoundLine(
            val entryOrder: Int,
            val anchorColumn: Int,
            val startColumn: Int,
            val rounds: List<AnswerCardItemState>
        ) : DisplayLine
    }

    fun build(rows: List<AnswerCardEntryCompactLayout.EntryRow>, expandedEntryOrder: Int?): List<DisplayLine> {
        if (rows.isEmpty()) return emptyList()
        val lines = mutableListOf<DisplayLine>()
        rows.chunked(COLUMNS).forEach { chunk ->
            lines.add(DisplayLine.EntryLine(chunk))
            chunk.forEachIndexed { columnIndex, entry ->
                if (entry.section.entryOrder == expandedEntryOrder && entry.rounds.size > 1) {
                    AnswerCardRoundLinePlacement.lines(columnIndex, entry.rounds).forEach { placed ->
                        lines.add(
                            DisplayLine.RoundLine(
                                entryOrder = entry.section.entryOrder,
                                anchorColumn = columnIndex,
                                startColumn = placed.startColumn,
                                rounds = placed.rounds
                            )
                        )
                    }
                }
            }
        }
        return lines
    }
}
