package com.example.testapp.uicommon.component

/** 分题号在 5 列网格内以词条列为中心向两侧排布；溢出按行 chunked 且每行仍居中。 */
object AnswerCardRoundLinePlacement {

    data class PlacedLine(
        val startColumn: Int,
        val rounds: List<AnswerCardItemState>
    )

    fun lines(
        anchorColumn: Int,
        rounds: List<AnswerCardItemState>,
        columns: Int = AnswerCardEntryGridLines.COLUMNS
    ): List<PlacedLine> {
        if (rounds.isEmpty()) return emptyList()
        return rounds.chunked(columns).map { chunk ->
            val start = (anchorColumn - (chunk.size - 1) / 2).coerceIn(0, columns - chunk.size)
            PlacedLine(start, chunk)
        }
    }
}
