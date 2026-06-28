package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.PracticeSessionState

object PracticeReviewReusePipeline {
    fun canReuse(
        currentProgressId: String,
        targetProgressId: String,
        state: PracticeSessionState
    ): Boolean {
        if (state.questionsWithState.isEmpty()) return false
        return normalizePracticeProgressId(currentProgressId) ==
            normalizePracticeProgressId(targetProgressId)
    }

    private fun normalizePracticeProgressId(id: String): String =
        if (id.startsWith("practice_")) id else "practice_$id"
}
