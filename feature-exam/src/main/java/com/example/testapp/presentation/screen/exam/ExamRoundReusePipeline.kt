package com.example.testapp.presentation.screen.exam

import com.example.testapp.domain.model.ExamProgress

/** 是否复用 progress 中已保存的 fixedQuestionOrder */
object ExamRoundReusePipeline {

    fun canReuseSavedOrder(
        progress: ExamProgress?,
        savedSourceOrder: List<Int>,
        questionCount: Int,
        fullPoolSize: Int,
        expectedSeq: List<Int>,
        random: Boolean,
        canReuseByFill: Boolean
    ): Boolean {
        if (progress == null || savedSourceOrder.isEmpty()) return false
        if (ExamRoundCompletePipeline.isComplete(progress)) return false
        if (ExamSourceQuestionPipeline.savedSourcesFullyAnswered(
                savedSourceOrder,
                progress.questionStateMap
            )
        ) {
            return false
        }
        if (!canReuseByFill) return false
        if (!ExamQuestionCountPolicy.canReuseSavedOrder(
                savedSourceCount = savedSourceOrder.size,
                questionCount = questionCount,
                fullPoolSize = fullPoolSize
            )
        ) {
            return false
        }
        return ExamSavedOrderMatchPipeline.matches(savedSourceOrder, expectedSeq, random)
    }
}
