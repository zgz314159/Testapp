package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.PracticeProgress

/** 是否复用 progress 中已保存的 fixedQuestionOrder */
object PracticeRoundReusePipeline {

    fun canReuseSavedOrder(
        progress: PracticeProgress?,
        savedSourceOrder: List<Int>,
        questionCount: Int,
        fullPoolSize: Int,
        canReuseByFill: Boolean
    ): Boolean {
        if (progress == null || savedSourceOrder.isEmpty()) return false
        if (PracticeRoundCompletePipeline.isComplete(progress)) return false
        if (PracticeSourceQuestionPipeline.savedSourcesFullyAnswered(
                savedSourceOrder,
                progress.questionStateMap
            )
        ) {
            return false
        }
        if (!canReuseByFill) return false
        return PracticeQuestionCountPolicy.canReuseSavedOrder(
            savedSourceCount = savedSourceOrder.size,
            questionCount = questionCount,
            fullPoolSize = fullPoolSize
        )
    }
}
