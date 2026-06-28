package com.example.testapp.uicommon.component

import com.example.testapp.domain.model.Question

data class AnswerCardEntrySection(
    val sectionKey: String,
    val entryOrder: Int,
    val questionIndices: List<Int>
)

object AnswerCardEntryGrouping {

    fun groupByEntry(
        questions: List<Question>,
        displayInfo: Map<Int, AnswerCardDisplayInfo>
    ): List<AnswerCardEntrySection> {
        val byOrder = linkedMapOf<Int, MutableList<Int>>()
        questions.forEachIndexed { index, question ->
            val order = displayInfo[question.id]?.order ?: (index + 1)
            byOrder.getOrPut(order) { mutableListOf() }.add(index)
        }
        return byOrder.entries
            .sortedBy { it.key }
            .map { (order, indices) ->
                AnswerCardEntrySection(
                    sectionKey = "entry_$order",
                    entryOrder = order,
                    questionIndices = indices
                )
            }
    }
}
