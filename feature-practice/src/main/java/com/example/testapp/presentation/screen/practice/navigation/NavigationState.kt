package com.example.testapp.presentation.screen.practice.navigation

import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.QuestionWithState

/**
 * Navigation State Types — extracted from PracticeNavigationCoordinator.
 * Defines 3 navigation sub-state types:
 *   - AnsweredHistoryNavigationState: for browsing previously answered questions
 *   - RandomNavigationHistoryState: tracks random-mode navigation path
 *   - PracticeNavigationState: composed state of both above
 */
sealed interface AnsweredHistoryNavigationState {
    data object Idle : AnsweredHistoryNavigationState

    data class Active(
        val originIndex: Int,
        /** [orderedIndices] 中当前浏览项的下标；0 = 最新作答 */
        val historyPosition: Int,
        /** 全局作答时间倒序（冻结） */
        val orderedIndices: List<Int>,
        /** 进入历史时锚点词条的轮次池 index 集合（全答模式） */
        val anchorPoolIndices: Set<Int> = emptySet()
    ) : AnsweredHistoryNavigationState
}

data class RandomNavigationHistoryState(
    val history: List<Int> = emptyList()
)

data class PracticeNavigationState(
    val mode: AnsweredHistoryNavigationState = AnsweredHistoryNavigationState.Idle,
    val randomHistory: RandomNavigationHistoryState = RandomNavigationHistoryState()
)

/**
 * Navigation utility functions — pure stateless computation.
 */
fun canGoNext(currentState: PracticeSessionState): Boolean =
    currentState.currentIndex < currentState.questionsWithState.size - 1

fun canGoPrev(currentState: PracticeSessionState): Boolean =
    currentState.currentIndex > 0

fun hasAnswerContent(questionWithState: QuestionWithState): Boolean =
    questionWithState.selectedOptions.isNotEmpty() || questionWithState.textAnswer.isNotBlank()
