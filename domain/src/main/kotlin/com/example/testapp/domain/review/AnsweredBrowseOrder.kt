package com.example.testapp.domain.review

import com.example.testapp.domain.model.QuestionWithState

object AnsweredBrowseOrder {
    fun hasAnswerContent(item: QuestionWithState): Boolean =
        item.selectedOptions.isNotEmpty() || item.textAnswer.isNotBlank()

    fun sortIndicesByAnswerTimeDesc(entries: List<Pair<Int, Long>>): List<Int> =
        entries
            .filter { (_, time) -> time > 0L }
            .sortedWith(
                compareByDescending<Pair<Int, Long>> { it.second }
                    .thenByDescending { it.first }
            )
            .map { it.first }

    fun buildAnsweredIndicesByTimeDesc(items: List<QuestionWithState>): List<Int> {
        if (items.isEmpty()) return emptyList()
        val entries = items.indices.mapNotNull { index ->
            val item = items[index]
            if (!hasAnswerContent(item) || item.sessionAnswerTime <= 0L) null
            else index to item.sessionAnswerTime
        }
        return sortIndicesByAnswerTimeDesc(entries)
    }

    fun buildReviewDisplayOrder(items: List<QuestionWithState>): List<Int> {
        if (items.isEmpty()) return emptyList()
        val answered = items.indices
            .filter { hasAnswerContent(items[it]) }
            .sortedByDescending { items[it].sessionAnswerTime }
        val unanswered = items.indices.filter { it !in answered }
        return answered + unanswered
    }
}
