package com.example.testapp.presentation.screen.practice.navigation

import android.util.Log
import com.example.testapp.core.session.strategy.navigation.SessionAnsweredHistoryDebugPipeline
import com.example.testapp.domain.model.PracticeSessionState

internal object NavigationHistoryDebugLog {
    private const val TAG = "PracticeHistorySwipe"

    fun logBuildOrdered(
        size: Int,
        orderedDebugLine: String,
    ) {
        Log.d(TAG, "buildOrdered | size=$size | $orderedDebugLine")
    }

    fun logSwipe(
        action: String,
        state: PracticeSessionState,
        mode: AnsweredHistoryNavigationState.Active?,
        targetIndex: Int?,
        result: String?,
        resolveAnswerTime: (PracticeSessionState, Int) -> Long,
    ) {
        val orderedDebugLine =
            SessionAnsweredHistoryDebugPipeline.formatOrderedDebugLine(
                orderedIndices = mode?.orderedIndices ?: emptyList(),
                questions = state.questions,
                resolveAnswerTime = { resolveAnswerTime(state, it) },
            )
        Log.d(
            TAG,
            SessionAnsweredHistoryDebugPipeline.formatSwipeLogLine(
                action = action,
                result = result,
                currentIndex = state.currentIndex,
                originIndex = mode?.originIndex,
                historyPosition = mode?.historyPosition,
                anchorPoolIndices = mode?.anchorPoolIndices,
                orderedDebugLine = orderedDebugLine,
                targetIndex = targetIndex,
            ),
        )
    }
}
