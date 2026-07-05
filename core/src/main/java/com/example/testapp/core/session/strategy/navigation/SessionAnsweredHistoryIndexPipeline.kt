package com.example.testapp.core.session.strategy.navigation

import com.example.testapp.core.session.SessionMemoryMode
import com.example.testapp.domain.model.QuestionWithState
import com.example.testapp.domain.review.AnsweredBrowseOrder

/** 已答历史 orderedIndices 构建（从 PracticeAnsweredBrowseNavigation 收编） */
object SessionAnsweredHistoryIndexPipeline {
    fun buildSwipeHistoryIndices(
        questionsWithState: List<QuestionWithState>,
        resolveSnapshot: (QuestionWithState) -> QuestionWithState?,
    ): List<Int> {
        val entries =
            questionsWithState.mapIndexedNotNull { index, item ->
                resolveSnapshot(item)?.let { snapshot -> index to snapshot.sessionAnswerTime }
            }
        return AnsweredBrowseOrder.sortIndicesByAnswerTimeDesc(entries)
    }

    fun applyMemoryRoundPriority(
        answeredByTimeDesc: List<Int>,
        questionsWithState: List<QuestionWithState>,
        roundIds: Set<Int>,
        memoryModeActive: Boolean,
        memoryPoolMode: Int,
    ): List<Int> {
        if (!memoryModeActive ||
            memoryPoolMode != SessionMemoryMode.MEMORY_POOL_MODE_ROUND ||
            roundIds.isEmpty()
        ) {
            return answeredByTimeDesc
        }
        val currentRoundAnswered =
            answeredByTimeDesc.filter { index ->
                questionsWithState[index].question.id in roundIds
            }
        if (currentRoundAnswered.isEmpty()) return answeredByTimeDesc
        val other =
            answeredByTimeDesc.filterNot { index ->
                questionsWithState[index].question.id in roundIds
            }
        return (currentRoundAnswered + other).distinct()
    }
}
