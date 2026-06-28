package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.PracticeProgress

/** 上一轮练习是否已答完 */
object PracticeRoundCompletePipeline {

    fun isComplete(progress: PracticeProgress?): Boolean {
        if (progress == null) return false
        val order = progress.fixedQuestionOrder
        if (order.isNotEmpty() && progress.questionStateMap.isNotEmpty()) {
            val allInOrderDone = order.all { questionId ->
                val state = progress.questionStateMap[questionId] ?: return@all false
                (state.selectedOptions.isNotEmpty() || state.textAnswer.isNotBlank()) && state.showResult
            }
            if (allInOrderDone) return true
        }
        val selections = progress.selectedOptions
        if (selections.isEmpty()) return false
        if (selections.any { it.isEmpty() }) return false
        val results = progress.showResultList
        return results.size >= selections.size && results.all { it }
    }
}
