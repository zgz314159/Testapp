package com.example.testapp.presentation.screen.practice.navigation

import com.example.testapp.core.session.strategy.navigation.SessionAnsweredHistoryOverlayPipeline
import com.example.testapp.core.session.strategy.navigation.SessionAnsweredHistorySnapshotPipeline
import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.QuestionWithState

/** NavigationHistory 快照存储与 overlay（从 NavigationHistory 拆出） */
object NavigationHistorySnapshots {
    fun remember(
        snapshots: MutableMap<Int, QuestionWithState>,
        questionWithState: QuestionWithState,
    ) {
        if (!SessionAnsweredHistorySnapshotPipeline.shouldCapture(questionWithState.showResult)) return
        val questionId = questionWithState.question.id
        val existingSnapshot = snapshots[questionId]
        if (SessionAnsweredHistorySnapshotPipeline.shouldReplaceExisting(
                existingSnapshot?.sessionAnswerTime,
                questionWithState.sessionAnswerTime,
            )
        ) {
            snapshots[questionId] = questionWithState
        }
    }

    fun resolveForBrowse(
        snapshots: Map<Int, QuestionWithState>,
        questionWithState: QuestionWithState,
    ): QuestionWithState? =
        SessionAnsweredHistorySnapshotPipeline.resolveBrowsableSnapshot(
            live = questionWithState,
            storedSnapshot = snapshots[questionWithState.question.id],
        )

    fun restoreOverlays(
        originals: MutableMap<Int, QuestionWithState>,
        currentState: PracticeSessionState,
    ): PracticeSessionState {
        val restore =
            SessionAnsweredHistoryOverlayPipeline.restoreOverlays(
                originals.toMap(),
                currentState.questionsWithState,
            )
        originals.clear()
        return if (restore.changed) {
            currentState.copy(questionsWithState = restore.questionsWithState)
        } else {
            currentState
        }
    }

    fun apply(
        snapshots: Map<Int, QuestionWithState>,
        originals: MutableMap<Int, QuestionWithState>,
        currentState: PracticeSessionState,
        index: Int,
        isQuestionAnswered: (QuestionWithState) -> Boolean,
        preferSnapshot: Boolean = false,
    ): PracticeSessionState {
        val liveQuestionState = currentState.questionsWithState.getOrNull(index) ?: return currentState
        val snapshot =
            snapshots[liveQuestionState.question.id]
                ?: return currentState.copy(currentIndex = index)
        if (SessionAnsweredHistorySnapshotPipeline.shouldKeepLiveStateOnApply(
                preferSnapshot,
                liveQuestionState.showResult,
                isQuestionAnswered(liveQuestionState),
            )
        ) {
            return currentState.copy(currentIndex = index)
        }
        originals.putIfAbsent(liveQuestionState.question.id, liveQuestionState)
        return currentState.copy(
            currentIndex = index,
            questionsWithState =
                SessionAnsweredHistoryOverlayPipeline.applySnapshotOverlay(
                    currentState.questionsWithState,
                    index,
                    snapshot,
                ),
        )
    }
}
