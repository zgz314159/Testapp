package com.example.testapp.presentation.screen.practice.navigation

import com.example.testapp.core.session.strategy.navigation.SessionNavigationHistoryGate
import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.QuestionWithState
import com.example.testapp.domain.session.navigation.SessionNavigationOrchestration
import com.example.testapp.presentation.screen.practice.SkipUnansweredSourceResult
import com.example.testapp.presentation.screen.practice.UnansweredNavResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Thin facade — delegates to focused navigation handlers.
 * See `.ai/practice_session_navigation_spec.md` before editing.
 */
class NavigationController(
    private val _sessionState: MutableStateFlow<PracticeSessionState>,
    scope: CoroutineScope,
    history: NavigationHistory,
    isQuestionPendingForCurrentMode: (QuestionWithState) -> Boolean,
    isQuestionAnswered: (QuestionWithState) -> Boolean,
    shouldReopenUnansweredReveal: (QuestionWithState) -> Boolean,
    currentSourcePendingIndices: (List<QuestionWithState>, Int, List<Int>) -> List<Int>,
    isCurrentSourceComplete: (PracticeSessionState) -> Boolean,
    findNextSourceEntryIndices: (PracticeSessionState) -> List<Int>,
    findAdjacentDerivedQuestionIndex: (PracticeSessionState, Boolean) -> Int?,
    effectiveCurrentMemoryRoundQuestionIds: (List<QuestionWithState>) -> Set<Int>,
    nextFullAnswerCandidateIndices: (List<QuestionWithState>, Int, List<Int>) -> List<Int>,
    reopenQuestionForPendingRetry: (Int) -> Unit,
    reopenQuestionForFullAnswerRetry: (Int) -> Unit,
    scheduleNavigationSave: () -> Unit,
    fullAnswerModeActive: () -> Boolean,
    fullAnswerRequireCorrect: () -> Boolean,
    fullAnswerRandomOrder: () -> Boolean,
    memoryModeActive: () -> Boolean,
    randomPracticeEnabled: () -> Boolean,
    navigationOrchestration: () -> SessionNavigationOrchestration? = { null },
) {
    private val env = NavigationEnvironment(
        sessionState = _sessionState,
        scope = scope,
        history = history,
        isQuestionPendingForCurrentMode = isQuestionPendingForCurrentMode,
        isQuestionAnswered = isQuestionAnswered,
        shouldReopenUnansweredReveal = shouldReopenUnansweredReveal,
        currentSourcePendingIndices = currentSourcePendingIndices,
        isCurrentSourceComplete = isCurrentSourceComplete,
        findNextSourceEntryIndices = findNextSourceEntryIndices,
        findAdjacentDerivedQuestionIndex = findAdjacentDerivedQuestionIndex,
        effectiveCurrentMemoryRoundQuestionIds = effectiveCurrentMemoryRoundQuestionIds,
        nextFullAnswerCandidateIndices = nextFullAnswerCandidateIndices,
        reopenQuestionForPendingRetry = reopenQuestionForPendingRetry,
        reopenQuestionForFullAnswerRetry = reopenQuestionForFullAnswerRetry,
        scheduleNavigationSave = scheduleNavigationSave,
        fullAnswerModeActive = fullAnswerModeActive,
        fullAnswerRequireCorrect = fullAnswerRequireCorrect,
        fullAnswerRandomOrder = fullAnswerRandomOrder,
        memoryModeActive = memoryModeActive,
        randomPracticeEnabled = randomPracticeEnabled,
        navigationOrchestration = navigationOrchestration,
    )

    private val targets = NavigationTargetNavigator(env)
    private val skipSource = NavigationSkipSource(env, targets)
    private val multiRound = NavigationMultiRoundIconNav(env, targets, skipSource)
    private val unansweredIcon = NavigationUnansweredIconNav(env, targets, multiRound, skipSource)
    private val iconCanMove = NavigationIconCanMove(env, skipSource, multiRound)
    private val sequentialNext = NavigationSequentialNext(env, targets)
    private val sequentialPrev = NavigationSequentialPrev(env, targets, multiRound)

    fun nextQuestionViaIcon(): UnansweredNavResult = unansweredIcon.navigateNext()

    fun prevQuestionViaIcon(): UnansweredNavResult = unansweredIcon.navigatePrev()

    fun nextQuestionViaIconDoubleClick(): Boolean {
        if (!env.fullAnswerModeActive()) return false
        return skipSource.skipToUnansweredSource(forward = true, forceCrossSource = true) ==
            SkipUnansweredSourceResult.Navigated
    }

    fun prevQuestionViaIconDoubleClick(): Boolean {
        if (!env.fullAnswerModeActive()) return false
        return skipSource.skipToUnansweredSource(forward = false, forceCrossSource = true) ==
            SkipUnansweredSourceResult.Navigated
    }

    fun canNavigateToPrevUnanswered(): Boolean = iconCanMove.canNavigateToPrevUnanswered()

    fun canNavigateToNextUnanswered(): Boolean = iconCanMove.canNavigateToNextUnanswered()

    fun nextQuestion() = sequentialNext.nextQuestion()

    fun prevQuestion() = sequentialPrev.prevQuestion()

    fun browseAnsweredHistoryOlder(): AnsweredHistoryBackwardResult {
        val currentState = _sessionState.value
        return env.history.navigateToPreviousAnsweredQuestion(
            currentState = currentState,
            onUpdateSession = { _sessionState.value = it },
            onSaveProgress = { env.scheduleNavigationSave() },
            effectiveCurrentMemoryRoundQuestionIds = env.effectiveCurrentMemoryRoundQuestionIds,
            memoryModeActive = env.memoryModeActive(),
            memoryPoolMode = NavigationHistory.MEMORY_POOL_MODE_ROUND,
            isQuestionAnswered = env.isQuestionAnswered,
            fullAnswerModeActive = env.fullAnswerModeActive()
        )
    }

    fun browseAnsweredHistoryNewer(): AnsweredHistoryForwardResult {
        val currentState = _sessionState.value
        return env.history.navigateToNextAnsweredInHistory(
            currentState = currentState,
            onUpdateSession = { _sessionState.value = it },
            onSaveProgress = { env.scheduleNavigationSave() },
            effectiveCurrentMemoryRoundQuestionIds = env.effectiveCurrentMemoryRoundQuestionIds,
            memoryModeActive = env.memoryModeActive(),
            memoryPoolMode = NavigationHistory.MEMORY_POOL_MODE_ROUND,
            isQuestionAnswered = env.isQuestionAnswered,
            fullAnswerModeActive = env.fullAnswerModeActive()
        )
    }

    fun goToQuestion(index: Int) {
        var currentState = _sessionState.value
        if (index in 0 until currentState.questionsWithState.size) {
            if (shouldClearHistoryOnManualJump()) {
                env.history.clearAll()
            }
            currentState = _sessionState.value
            val targetQuestion = currentState.questionsWithState[index]
            if (env.shouldReopenUnansweredReveal(targetQuestion)) {
                env.reopenQuestionForPendingRetry(index)
            } else {
                _sessionState.value = currentState.copy(currentIndex = index)
                env.scheduleNavigationSave()
            }
        }
    }

    fun resetNavigationForManualJump() {
        if (shouldClearHistoryOnManualJump()) {
            env.history.clearAll()
        }
    }

    private fun shouldClearHistoryOnManualJump(): Boolean =
        env.navigationOrchestration()?.let(SessionNavigationHistoryGate::shouldClearHistoryOnManualJump)
            ?: true

    fun canSkipToUnansweredSource(forward: Boolean): Boolean =
        skipSource.canSkipToUnansweredSource(forward)

    fun skipToUnansweredSource(forward: Boolean, forceCrossSource: Boolean = false): SkipUnansweredSourceResult =
        skipSource.skipToUnansweredSource(forward, forceCrossSource)

    @Deprecated("Use canSkipToUnansweredSource")
    fun canSkipToAdjacentSource(forward: Boolean): Boolean = canSkipToUnansweredSource(forward)

    @Deprecated("Use skipToUnansweredSource")
    fun skipToAdjacentSource(forward: Boolean) {
        skipToUnansweredSource(forward)
    }
}
