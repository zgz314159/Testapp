package com.example.testapp.presentation.screen.exam

import com.example.testapp.domain.model.UnifiedQuestionState

/** 考试跨轮次 questionStateMap 合并 — 无状态管道 */
object ExamQuestionStateMapPipeline {

    fun mergeCumulative(
        prior: Map<Int, UnifiedQuestionState>,
        current: Map<Int, UnifiedQuestionState>
    ): Map<Int, UnifiedQuestionState> = prior + current
}
