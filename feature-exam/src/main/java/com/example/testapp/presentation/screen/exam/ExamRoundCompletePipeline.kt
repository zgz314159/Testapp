package com.example.testapp.presentation.screen.exam

import com.example.testapp.domain.model.ExamProgress

/** 上一轮考试是否已答完（含 finished 或全题已批改） */
object ExamRoundCompletePipeline {

    fun isComplete(progress: ExamProgress?): Boolean {
        if (progress == null) return false
        if (progress.finished) return true
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
