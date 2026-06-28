package com.example.testapp.presentation.screen.practice.navigation

import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.QuestionWithState
import com.example.testapp.domain.review.AnsweredBrowseOrder

/**
 * 练习侧：按时间倒序浏览已答（右滑历史）与 domain [AnsweredBrowseOrder] 的衔接。
 */
object PracticeAnsweredBrowseNavigation {
    fun buildSwipeHistoryIndices(
        questionsWithState: List<QuestionWithState>,
        resolveSnapshot: (QuestionWithState) -> QuestionWithState?
    ): List<Int> {
        val entries = questionsWithState.mapIndexedNotNull { index, item ->
            resolveSnapshot(item)?.let { snapshot -> index to snapshot.sessionAnswerTime }
        }
        return AnsweredBrowseOrder.sortIndicesByAnswerTimeDesc(entries)
    }

    fun applyMemoryRoundPriority(
        answeredByTimeDesc: List<Int>,
        questionsWithState: List<QuestionWithState>,
        roundIds: Set<Int>,
        memoryModeActive: Boolean,
        memoryPoolMode: Int
    ): List<Int> {
        if (!memoryModeActive || memoryPoolMode != NavigationHistory.MEMORY_POOL_MODE_ROUND || roundIds.isEmpty()) {
            return answeredByTimeDesc
        }
        val currentRoundAnswered = answeredByTimeDesc.filter { index ->
            questionsWithState[index].question.id in roundIds
        }
        if (currentRoundAnswered.isEmpty()) return answeredByTimeDesc
        val other = answeredByTimeDesc.filterNot { index -> questionsWithState[index].question.id in roundIds }
        return (currentRoundAnswered + other).distinct()
    }

    fun navigateReadOnly(
        currentState: PracticeSessionState,
        targetIndex: Int,
        onUpdateSession: (PracticeSessionState) -> Unit
    ) {
        if (targetIndex !in currentState.questionsWithState.indices) return
        onUpdateSession(currentState.copy(currentIndex = targetIndex))
    }
}
