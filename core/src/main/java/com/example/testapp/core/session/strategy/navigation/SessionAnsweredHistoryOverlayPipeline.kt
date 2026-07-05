package com.example.testapp.core.session.strategy.navigation

import com.example.testapp.domain.model.QuestionWithState

/** 已答历史 overlay 恢复与应用（从 NavigationHistory 收编） */
object SessionAnsweredHistoryOverlayPipeline {
    data class RestoreResult(
        val questionsWithState: List<QuestionWithState>,
        val changed: Boolean,
    )

    fun restoreOverlays(
        originalsByQuestionId: Map<Int, QuestionWithState>,
        questionsWithState: List<QuestionWithState>,
    ): RestoreResult {
        if (originalsByQuestionId.isEmpty()) {
            return RestoreResult(questionsWithState, changed = false)
        }
        var changed = false
        val restored =
            questionsWithState.map { qws ->
                val original = originalsByQuestionId[qws.question.id]
                if (original != null) {
                    changed = true
                    original
                } else {
                    qws
                }
            }
        return RestoreResult(restored, changed)
    }

    fun applySnapshotOverlay(
        questionsWithState: List<QuestionWithState>,
        index: Int,
        snapshot: QuestionWithState,
    ): List<QuestionWithState> {
        val updated = questionsWithState.toMutableList()
        updated[index] = snapshot
        return updated
    }
}
