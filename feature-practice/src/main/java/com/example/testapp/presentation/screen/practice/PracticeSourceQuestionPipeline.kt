package com.example.testapp.presentation.screen.practice

import com.example.testapp.core.util.extractSourceQuestionId
import com.example.testapp.domain.model.UnifiedQuestionState

/** 变体题号 → 源题号（出题池维度） */
object PracticeSourceQuestionPipeline {

    fun answeredSourceIds(questionStateMap: Map<Int, UnifiedQuestionState>): Set<Int> =
        questionStateMap
            .filterValues { state ->
                state.selectedOptions.isNotEmpty() || state.textAnswer.isNotBlank()
            }
            .keys
            .map(::extractSourceQuestionId)
            .toSet()

    fun lastRoundSourceIds(fixedQuestionOrder: List<Int>): Set<Int> =
        fixedQuestionOrder.map(::extractSourceQuestionId).toSet()

    fun savedSourcesFullyAnswered(
        savedSourceIds: List<Int>,
        questionStateMap: Map<Int, UnifiedQuestionState>
    ): Boolean {
        val sources = savedSourceIds.distinct()
        if (sources.isEmpty() || questionStateMap.isEmpty()) return false
        return sources.all { sourceId ->
            questionStateMap.any { (questionId, state) ->
                extractSourceQuestionId(questionId) == sourceId &&
                    state.showResult &&
                    (state.selectedOptions.isNotEmpty() || state.textAnswer.isNotBlank())
            }
        }
    }
}
