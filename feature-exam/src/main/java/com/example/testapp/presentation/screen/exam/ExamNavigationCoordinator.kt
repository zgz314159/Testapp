package com.example.testapp.presentation.screen.exam

import com.example.testapp.core.util.FullAnswerIconNavigationStrategyPipeline
import com.example.testapp.core.util.FullAnswerIconTapStrategy
import com.example.testapp.core.util.FullAnswerMultiRoundSessionPipeline
import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.QuestionWithState
import com.example.testapp.domain.model.UnifiedQuestionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExamNavigationCoordinator(
    private val sessionState: MutableStateFlow<PracticeSessionState>,
    private val scope: CoroutineScope,
    private val navHelper: ExamNavigationHelper,
    private val answerRules: ExamAnswerRules,
    private val fullAnswerModeActive: () -> Boolean,
    private val fullAnswerRequireCorrect: () -> Boolean,
    private val randomExamEnabled: () -> Boolean,
    private val memoryModeActive: () -> Boolean,
    private val effectiveCurrentMemoryRoundQuestionIds: () -> Set<Int>,
    private val buildExamQuestionState: (Int) -> UnifiedQuestionState,
    private val advanceMemoryRoundIfNeeded: suspend () -> Boolean,
    private val reopenQuestionForFullAnswerRetry: (Int) -> Unit,
    private val scheduleNavigationSave: () -> Unit,
    private val saveProgressInternal: suspend () -> Unit
) {
    private fun sessionGraded(state: PracticeSessionState): Boolean =
        state.finished || state.questionsWithState.any { it.showResult }

    private fun isCorrectFn(): (com.example.testapp.domain.model.Question, UnifiedQuestionState) -> Boolean =
        { question, unified -> answerRules.isQuestionCorrect(question, unified) }

    private fun navigableInRoundPool(state: PracticeSessionState): List<Int> =
        ExamFullAnswerRoundNavigablePipeline.navigableIndicesInPool(
            questions = state.questions,
            questionsWithState = state.questionsWithState,
            currentIndex = state.currentIndex,
            fullAnswerRequireCorrect = fullAnswerRequireCorrect()
        )

    private fun mustStayInRoundPool(state: PracticeSessionState): Boolean =
        fullAnswerModeActive() &&
            ExamFullAnswerRoundUnansweredPipeline.hasUnansweredInPool(
                state.questions,
                state.questionsWithState,
                state.currentIndex
            )

    private fun roundPoolAllAnswered(state: PracticeSessionState): Boolean =
        ExamFullAnswerRoundCompletePipeline.isComplete(
            questions = state.questions,
            questionsWithState = state.questionsWithState,
            currentIndex = state.currentIndex,
            fullAnswerModeActive = fullAnswerModeActive()
        )

    private fun navigateToIndex(index: Int, reopenWrongFullAnswerRetry: Boolean = false) {
        val state = sessionState.value
        if (index !in state.questionsWithState.indices) return
        val target = state.questionsWithState[index]
        if (reopenWrongFullAnswerRetry && fullAnswerRequireCorrect() &&
            target.showResult && target.isCorrect != true
        ) {
            reopenQuestionForFullAnswerRetry(index)
            return
        }
        if (index != state.currentIndex) {
            sessionState.update { it.copy(currentIndex = index) }
            scheduleNavigationSave()
        }
    }

    private fun tryNavigateWithinRoundPool(forward: Boolean): Boolean {
        if (!fullAnswerModeActive()) return false
        val state = sessionState.value
        val navigable = navigableInRoundPool(state)
        if (navigable.isEmpty()) return false

        val currentIdx = state.currentIndex
        val targetIndex = if (forward) {
            ExamFullAnswerIconNavigation.resolveNextInRoundPool(currentIdx, navigable)
        } else {
            ExamFullAnswerIconNavigation.resolvePrevInRoundPool(currentIdx, navigable)
        }

        if (targetIndex != null) {
            if (targetIndex != currentIdx) {
                navigateToIndex(targetIndex, reopenWrongFullAnswerRetry = true)
                return true
            }
            ExamFullAnswerIconRetryPipeline.resolveStayIndexForWrongRetry(
                state,
                fullAnswerRequireCorrect()
            )?.let { stayIndex ->
                navigateToIndex(stayIndex, reopenWrongFullAnswerRetry = true)
                return true
            }
        }
        return false
    }

    fun currentFullAnswerCandidateIndices(candidates: List<Int>): List<Int> {
        val state = sessionState.value
        return navHelper.currentFullAnswerCandidateIndices(
            questions = state.questions,
            questionsWithState = state.questionsWithState,
            currentIndex = state.currentIndex,
            eligible = candidates,
            fullAnswerModeActive = fullAnswerModeActive(),
            fullAnswerRequireCorrect = fullAnswerRequireCorrect(),
            sessionGraded = sessionGraded(state),
            isCorrect = isCorrectFn()
        )
    }

    fun navigateCandidateIndices(): List<Int> {
        val state = sessionState.value
        return navHelper.navigateCandidateIndices(
            questions = state.questions,
            questionsWithState = state.questionsWithState,
            currentIndex = state.currentIndex,
            fullAnswerModeActive = fullAnswerModeActive(),
            fullAnswerRequireCorrect = fullAnswerRequireCorrect(),
            sessionGraded = sessionGraded(state),
            memoryActive = memoryModeActive(),
            roundIds = effectiveCurrentMemoryRoundQuestionIds(),
            isCorrect = isCorrectFn()
        )
    }

    suspend fun navigateToRandomUnansweredOrAdvanceRound() {
        val candidates = navigateCandidateIndices()
        if (candidates.isNotEmpty()) {
            sessionState.update { it.copy(currentIndex = candidates.random()) }
            return
        }
        advanceMemoryRoundIfNeeded()
    }

    private fun isPendingAt(questionWithState: QuestionWithState): Boolean =
        ExamPendingQuestionPipeline.isQuestionPending(questionWithState)

    fun hasPendingQuestions(): Boolean =
        ExamPendingQuestionPipeline.hasPending(sessionState.value.questionsWithState)

    fun canNavigateToNextUnanswered(): Boolean {
        val state = sessionState.value
        if (fullAnswerModeActive()) {
            val navigable = navigableInRoundPool(state)
            if (ExamFullAnswerIconNavigation.hasNextInRoundPool(state.currentIndex, navigable)) {
                return true
            }
            if (mustStayInRoundPool(state)) return false
        }
        return ExamUnansweredNavigation.hasNextUnanswered(
            anchorIndex = state.currentIndex,
            questionsWithState = state.questionsWithState,
            isPending = ::isPendingAt,
            randomExam = randomExamEnabled()
        )
    }

    fun canNavigateToPrevUnanswered(): Boolean {
        val state = sessionState.value
        if (fullAnswerModeActive()) {
            val navigable = navigableInRoundPool(state)
            if (ExamFullAnswerIconNavigation.hasPrevInRoundPool(state.currentIndex, navigable)) {
                return true
            }
            if (mustStayInRoundPool(state)) return false
        }
        return ExamUnansweredNavigation.hasPrevUnanswered(
            anchorIndex = state.currentIndex,
            questionsWithState = state.questionsWithState,
            isPending = ::isPendingAt,
            randomExam = randomExamEnabled()
        )
    }

    fun canGoPrevSequential(): Boolean {
        val state = sessionState.value
        return state.currentIndex > 0
    }

    fun canGoNextSequential(): Boolean {
        val state = sessionState.value
        return state.currentIndex < state.questionsWithState.size - 1
    }

    private fun iconTapStrategy(): FullAnswerIconTapStrategy =
        FullAnswerIconNavigationStrategyPipeline.resolve(
            fullAnswerModeActive = fullAnswerModeActive(),
            multiRoundSession = FullAnswerMultiRoundSessionPipeline.isMultiRoundSession(
                sessionState.value.questions
            )
        )

    fun prevQuestionViaIcon() {
        val strategy = iconTapStrategy()
        if (FullAnswerIconNavigationStrategyPipeline.singleTapUsesRoundPool(strategy)) {
            if (tryNavigateWithinRoundPool(forward = false)) return
            return
        }
        val state = sessionState.value
        val (result, targetIndex) = ExamUnansweredNavigation.resolvePrevUnansweredIndex(
            anchorIndex = state.currentIndex,
            questionsWithState = state.questionsWithState,
            isPending = ::isPendingAt,
            randomExam = randomExamEnabled()
        )
        if (result == ExamUnansweredNavResult.Navigated && targetIndex != null) {
            navigateToIndex(targetIndex)
            return
        }
        if (ExamIconUnansweredNavigationPipeline.shouldFallbackToAdjacentSource(
                navigated = false,
                strategy = strategy
            )
        ) {
            skipToAdjacentSource(forward = false)
        }
    }

    fun nextQuestionViaIcon() {
        val strategy = iconTapStrategy()
        if (FullAnswerIconNavigationStrategyPipeline.singleTapUsesRoundPool(strategy)) {
            if (tryNavigateWithinRoundPool(forward = true)) return
            return
        }
        val state = sessionState.value
        val (result, targetIndex) = ExamUnansweredNavigation.resolveNextUnansweredIndex(
            anchorIndex = state.currentIndex,
            questionsWithState = state.questionsWithState,
            isPending = ::isPendingAt,
            randomExam = randomExamEnabled()
        )
        if (result == ExamUnansweredNavResult.Navigated && targetIndex != null) {
            navigateToIndex(targetIndex)
            return
        }
        if (ExamIconUnansweredNavigationPipeline.shouldFallbackToAdjacentSource(
                navigated = false,
                strategy = strategy
            )
        ) {
            skipToAdjacentSource(forward = true)
        }
    }

    fun prevQuestionViaIconDoubleClick(): Boolean {
        val strategy = iconTapStrategy()
        return if (FullAnswerIconNavigationStrategyPipeline.doubleTapUsesCrossSource(strategy)) {
            val before = sessionState.value.currentIndex
            skipToAdjacentSource(forward = false)
            sessionState.value.currentIndex != before
        } else {
            tryNavigateWithinRoundPool(forward = false)
        }
    }

    fun nextQuestionViaIconDoubleClick(): Boolean {
        val strategy = iconTapStrategy()
        return if (FullAnswerIconNavigationStrategyPipeline.doubleTapUsesCrossSource(strategy)) {
            val before = sessionState.value.currentIndex
            skipToAdjacentSource(forward = true)
            sessionState.value.currentIndex != before
        } else {
            tryNavigateWithinRoundPool(forward = true)
        }
    }

    fun nextQuestion() {
        val stateBefore = sessionState.value
        if (tryNavigateWithinRoundPool(forward = true)) return
        if (mustStayInRoundPool(stateBefore)) return

        val state = sessionState.value
        if (fullAnswerModeActive() && !randomExamEnabled()) {
            val next = ExamFullAnswerNavigation.resolveSequentialNextIndex(
                questions = state.questions,
                questionsWithState = state.questionsWithState,
                currentIndex = state.currentIndex,
                fullAnswerModeActive = true,
                requireCorrect = fullAnswerRequireCorrect(),
                sessionGraded = sessionGraded(state),
                isCorrect = isCorrectFn()
            )
            if (next != null) {
                navigateToIndex(next, reopenWrongFullAnswerRetry = true)
                return
            }
        }
        if (randomExamEnabled()) {
            navigateRandomOrAdvanceRound()
            return
        }
        val (result, targetIndex) = ExamUnansweredNavigation.resolveNextUnansweredIndex(
            anchorIndex = state.currentIndex,
            questionsWithState = state.questionsWithState,
            isPending = ::isPendingAt,
            randomExam = false
        )
        if (result == ExamUnansweredNavResult.Navigated && targetIndex != null) {
            sessionState.update { it.copy(currentIndex = targetIndex) }
            scheduleNavigationSave()
        }
    }

    fun prevQuestion() {
        val stateBefore = sessionState.value
        if (tryNavigateWithinRoundPool(forward = false)) return
        if (mustStayInRoundPool(stateBefore)) return

        val state = sessionState.value
        if (randomExamEnabled()) {
            navigateRandomOrAdvanceRound()
            return
        }
        val (result, targetIndex) = ExamUnansweredNavigation.resolvePrevUnansweredIndex(
            anchorIndex = state.currentIndex,
            questionsWithState = state.questionsWithState,
            isPending = ::isPendingAt,
            randomExam = false
        )
        if (result == ExamUnansweredNavResult.Navigated && targetIndex != null) {
            sessionState.update { it.copy(currentIndex = targetIndex) }
            scheduleNavigationSave()
        }
    }

    fun prevQuestionSequential() {
        val state = sessionState.value
        if (state.currentIndex > 0) {
            sessionState.update { it.copy(currentIndex = state.currentIndex - 1) }
            scheduleNavigationSave()
        }
    }

    fun nextQuestionSequential() {
        val state = sessionState.value
        if (state.currentIndex < state.questionsWithState.size - 1) {
            sessionState.update { it.copy(currentIndex = state.currentIndex + 1) }
            scheduleNavigationSave()
        }
    }

    fun canSkipToAdjacentSource(forward: Boolean): Boolean {
        if (!fullAnswerModeActive()) return false
        val state = sessionState.value
        return ExamFullAnswerNavigation.resolveSkipToAdjacentSourceIndex(
            questions = state.questions,
            currentIndex = state.currentIndex,
            forward = forward,
            randomOrder = randomExamEnabled()
        ) != null
    }

    fun skipToAdjacentSource(forward: Boolean) {
        if (!fullAnswerModeActive()) return
        val state = sessionState.value
        val target = ExamFullAnswerNavigation.resolveSkipToAdjacentSourceIndex(
            questions = state.questions,
            currentIndex = state.currentIndex,
            forward = forward,
            randomOrder = randomExamEnabled()
        ) ?: return
        if (target != state.currentIndex) {
            sessionState.update { it.copy(currentIndex = target) }
            scheduleNavigationSave()
        }
    }

    fun goToQuestion(index: Int) {
        if (index in sessionState.value.questionsWithState.indices) {
            sessionState.update { it.copy(currentIndex = index) }
            scheduleNavigationSave()
        }
    }

    private fun navigateRandomOrAdvanceRound() {
        val state = sessionState.value
        if (mustStayInRoundPool(state)) {
            tryNavigateWithinRoundPool(forward = true)
            return
        }
        val candidates = navigateCandidateIndices()
        if (candidates.isNotEmpty()) {
            sessionState.update { it.copy(currentIndex = candidates.random()) }
            scheduleNavigationSave()
        } else if (memoryModeActive()) {
            scope.launch {
                if (advanceMemoryRoundIfNeeded()) saveProgressInternal()
            }
        }
    }
}
