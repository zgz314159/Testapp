package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.QuestionWithState
import com.example.testapp.presentation.screen.practice.navigation.NavigationController
import com.example.testapp.presentation.screen.practice.navigation.NavigationHistory
import com.example.testapp.presentation.screen.practice.navigation.PracticeNavigationState
import com.example.testapp.presentation.screen.practice.navigation.canGoNext
import com.example.testapp.presentation.screen.practice.navigation.canGoPrev
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * PracticeNavigationCoordinator — 瘦外观层，委托给 3 个子组件
 *   - NavigationHistory  : 导航状态 + 历史记录管理
 *   - NavigationController : nextQuestion / prevQuestion / goToQuestion 编排
 *   - NavigationState.kt  : 纯类型定义 + 工具函数（top-level）
 */
class PracticeNavigationCoordinator {

    private val history = NavigationHistory()
    private var controller: NavigationController? = null

    // === 实例状态暴露（委托给 history） ===
    var navigationState: PracticeNavigationState
        get() = history.navigationState
        set(value) { history.navigationState = value }

    val answeredHistorySnapshots: MutableMap<Int, QuestionWithState>
        get() = history.answeredHistorySnapshots
    val answeredHistoryOriginalStates: MutableMap<Int, QuestionWithState>
        get() = history.answeredHistoryOriginalStates

    var randomPracticeEnabled: Boolean = false

    // === 计算属性（委托） ===
    val isInAnsweredHistory: Boolean
        get() = history.isInAnsweredHistory

    fun canGoNext(currentState: PracticeSessionState): Boolean =
        com.example.testapp.presentation.screen.practice.navigation.canGoNext(currentState)

    fun canGoPrev(currentState: PracticeSessionState): Boolean =
        com.example.testapp.presentation.screen.practice.navigation.canGoPrev(currentState)

    // === 历史快照方法（委托） ===
    fun rememberAnsweredHistorySnapshot(questionWithState: QuestionWithState) =
        history.rememberAnsweredHistorySnapshot(questionWithState)

    fun historySnapshotFor(questionWithState: QuestionWithState): QuestionWithState? =
        history.historySnapshotFor(questionWithState)

    fun restoreAnsweredHistoryOverlays(currentState: PracticeSessionState): PracticeSessionState =
        history.restoreAnsweredHistoryOverlays(currentState)

    fun applyAnsweredHistorySnapshot(
        currentState: PracticeSessionState,
        index: Int,
        isQuestionAnswered: (QuestionWithState) -> Boolean
    ): PracticeSessionState = history.applyAnsweredHistorySnapshot(currentState, index, isQuestionAnswered)

    // === 导航状态转换（委托） ===
    fun clearRandomNavigationState() = history.clearRandomNavigationState()
    fun clearAnsweredHistoryNavigation() = history.clearAnsweredHistoryNavigation()
    fun clearRandomNavigationHistory() = history.clearAll()
    fun restoreAnsweredHistoryIfNeeded(sessionState: MutableList<PracticeSessionState>?) = history.restoreAnsweredHistoryIfNeeded()
    fun updateAnsweredHistoryNavigation(originIndex: Int, historyPosition: Int) =
        history.updateAnsweredHistoryNavigation(originIndex, historyPosition)
    fun prepareStateForForwardNavigation(currentState: PracticeSessionState): PracticeSessionState =
        history.prepareStateForForwardNavigation(currentState)
    fun resumeFromAnsweredHistory(currentState: PracticeSessionState): PracticeSessionState =
        history.resumeFromAnsweredHistory(currentState)

    fun buildPreviousAnsweredIndices(
        currentState: PracticeSessionState,
        effectiveCurrentMemoryRoundQuestionIds: (List<QuestionWithState>) -> Set<Int>,
        memoryModeActive: Boolean,
        memoryPoolMode: Int
    ): List<Int> = history.buildPreviousAnsweredIndices(
        currentState, effectiveCurrentMemoryRoundQuestionIds, memoryModeActive, memoryPoolMode
    )

    fun navigateToPreviousAnsweredQuestion(
        currentState: PracticeSessionState,
        onUpdateSession: (PracticeSessionState) -> Unit,
        onSaveProgress: () -> Unit,
        effectiveCurrentMemoryRoundQuestionIds: (List<QuestionWithState>) -> Set<Int>,
        memoryModeActive: Boolean,
        memoryPoolMode: Int,
        isQuestionAnswered: (QuestionWithState) -> Boolean
    ): Boolean = history.navigateToPreviousAnsweredQuestion(
        currentState, onUpdateSession, onSaveProgress,
        effectiveCurrentMemoryRoundQuestionIds, memoryModeActive, memoryPoolMode, isQuestionAnswered
    )

    fun recordRandomNavigationOrigin(currentIndex: Int) =
        history.recordRandomNavigationOrigin(currentIndex, randomPracticeEnabled)

    fun seedRandomNavigationHistory(
        questionsWithState: List<QuestionWithState>,
        currentIndex: Int,
        isQuestionAnswered: (QuestionWithState) -> Boolean
    ) = history.seedRandomNavigationHistory(
        questionsWithState, currentIndex, isQuestionAnswered, randomPracticeEnabled
    )

    // === Phase 4: 导航编排（委托给 controller） ===

    fun initPhase4(
        _sessionState: MutableStateFlow<PracticeSessionState>,
        scope: CoroutineScope,
        isQuestionPendingForCurrentMode: (QuestionWithState) -> Boolean,
        shouldReopenUnansweredReveal: (QuestionWithState) -> Boolean,
        currentSourcePendingIndices: (List<QuestionWithState>, Int, List<Int>) -> List<Int>,
        isCurrentSourceComplete: (PracticeSessionState) -> Boolean,
        findNextSourceEntryIndices: (PracticeSessionState) -> List<Int>,
        findAdjacentDerivedQuestionIndex: (PracticeSessionState, Boolean) -> Int?,
        effectiveCurrentMemoryRoundQuestionIds: (List<QuestionWithState>) -> Set<Int>,
        nextFullAnswerCandidateIndices: (List<QuestionWithState>, Int, List<Int>) -> List<Int>,
        reopenQuestionForPendingRetry: (Int) -> Unit,
        reopenQuestionForFullAnswerRetry: (Int) -> Unit,
        saveProgress: () -> Unit,
        fullAnswerModeActive: () -> Boolean,
        fullAnswerRequireCorrect: () -> Boolean,
        memoryModeActive: () -> Boolean
    ) {
        controller = NavigationController(
            _sessionState, scope, history,
            isQuestionPendingForCurrentMode, shouldReopenUnansweredReveal,
            currentSourcePendingIndices, isCurrentSourceComplete,
            findNextSourceEntryIndices, findAdjacentDerivedQuestionIndex,
            effectiveCurrentMemoryRoundQuestionIds, nextFullAnswerCandidateIndices,
            reopenQuestionForPendingRetry, reopenQuestionForFullAnswerRetry,
            saveProgress,
            fullAnswerModeActive, fullAnswerRequireCorrect, memoryModeActive
        ) { randomPracticeEnabled }
    }

    fun nextQuestion() = controller?.nextQuestion()
    fun prevQuestion() = controller?.prevQuestion()

    fun goToQuestion(index: Int) = controller?.goToQuestion(index)
    fun resetNavigationForManualJump() = controller?.resetNavigationForManualJump()

    companion object {
        const val MEMORY_POOL_MODE_ROUND = NavigationHistory.MEMORY_POOL_MODE_ROUND
    }
}
