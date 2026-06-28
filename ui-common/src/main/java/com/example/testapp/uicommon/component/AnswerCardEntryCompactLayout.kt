package com.example.testapp.uicommon.component

import com.example.testapp.domain.model.Question

/** 全答紧凑答题卡：折叠=词条序号；展开=词条号+圈号轮次（如 1①）。 */
object AnswerCardEntryCompactLayout {

    data class EntryRow(
        val section: AnswerCardEntrySection,
        val collapsed: AnswerCardItemState,
        val rounds: List<AnswerCardItemState>
    )

    fun buildRows(
        questions: List<Question>,
        displayInfo: Map<Int, AnswerCardDisplayInfo>,
        selectedOptions: List<List<Int>>,
        textAnswers: List<String>,
        showResultList: List<Boolean>,
        currentIndex: Int
    ): List<EntryRow> = AnswerCardEntryGrouping.groupByEntry(questions, displayInfo).map { section ->
        val rounds = AnswerCardStateBuilder.build(
            section.questionIndices,
            questions,
            selectedOptions,
            textAnswers,
            showResultList,
            displayInfo,
            currentIndex = currentIndex
        ).map { item ->
            val round = displayInfo[questions.getOrNull(item.index)?.id]?.round
            item.copy(label = roundLabel(section.entryOrder, round, item.index))
        }
        EntryRow(
            section = section,
            collapsed = aggregateCollapsed(section.entryOrder, rounds),
            rounds = rounds
        )
    }

    fun roundLabel(entryOrder: Int, round: Int?, fallbackIndex: Int): String =
        if (round != null) "$entryOrder${circled(round)}" else (entryOrder.toString())

    fun circled(round: Int): String =
        if (round in 1..20) "${('\u2460'.code + round - 1).toChar()}" else "($round)"

    private fun aggregateCollapsed(entryOrder: Int, rounds: List<AnswerCardItemState>): AnswerCardItemState {
        val status = when {
            rounds.any { it.status == AnswerCardStatus.WRONG } -> AnswerCardStatus.WRONG
            rounds.isNotEmpty() && rounds.all { it.status == AnswerCardStatus.CORRECT } -> AnswerCardStatus.CORRECT
            rounds.any { it.status == AnswerCardStatus.SELECTED } -> AnswerCardStatus.SELECTED
            else -> AnswerCardStatus.UNANSWERED
        }
        val anchorIndex = rounds.firstOrNull { it.isCurrent }?.index
            ?: rounds.firstOrNull()?.index
            ?: 0
        return AnswerCardItemState(
            index = anchorIndex,
            label = entryOrder.toString(),
            status = status,
            isCurrent = rounds.any { it.isCurrent }
        )
    }
}
