package com.example.testapp.presentation.screen.practice

import com.example.testapp.core.util.resolveFillCorrectAnswer
import com.example.testapp.core.util.retainCorrectFillAnswerParts
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState
import com.example.testapp.domain.model.UnifiedQuestionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.lang.System

class PracticeModeCoordinator(
    private val answerHandler: PracticeAnswerHandler,
    private val _sessionState: MutableStateFlow<PracticeSessionState>,
    private val scope: CoroutineScope,
    private val buildStoredQuestionState: suspend (Question, UnifiedQuestionState?) -> QuestionWithState,
    private val applyConfiguredFillQuestions: suspend (List<Question>, Long) -> List<Question>,
    private val saveProgress: suspend () -> Unit,
    private val clearRandomNavigationHistory: () -> Unit,
    private val hasPendingQuestions: (List<QuestionWithState>) -> Boolean,
    private val updatePersistentStateMapFn: (PracticeSessionState) -> Unit,
    val removedMemoryPoolQuestionIds: MutableSet<Int>,
    val persistentQuestionStateMap: MutableMap<Int, UnifiedQuestionState>,
    val currentMemoryRoundQuestionIdsRef: () -> Set<Int>,
    val setCurrentMemoryRoundQuestionIds: (Set<Int>) -> Unit
) {

    companion object {
        const val MEMORY_WRONG_MODE_RETRY_WRONG_BLANKS = 0
        const val MEMORY_WRONG_MODE_REDO_ALL_BLANKS = 1
        const val MEMORY_POOL_MODE_IN_OUT = 0
        const val MEMORY_POOL_MODE_ROUND = 1
    }

    fun buildMemoryRoundPlan(
        sourceQuestions: List<Question>,
        seed: Long,
        fallbackQuestionCount: Int,
        memoryModeBatchSize: Int,
        persistentQuestionStateMap: Map<Int, UnifiedQuestionState>,
        removedMemoryPoolQuestionIds: Set<Int>,
        randomPracticeEnabled: Boolean
    ): MemoryRoundPlan {
        val excluded = removedMemoryPoolQuestionIds
        val eligible = sourceQuestions.filter { it.id !in excluded }
        if (eligible.isEmpty()) return MemoryRoundPlan(emptyList(), emptySet())

        val targetCount = when {
            memoryModeBatchSize > 0 -> memoryModeBatchSize
            fallbackQuestionCount > 0 -> fallbackQuestionCount
            else -> eligible.size
        }.coerceIn(1, eligible.size.coerceAtLeast(1))

        val wrongCandidates = eligible.filter { q ->
            val saved = persistentQuestionStateMap[q.id] ?: return@filter false
            answerHandler.isQuestionAnswered(saved) && !answerHandler.isQuestionCorrect(q, saved)
        }
        val untouchedCandidates = eligible.filter { q ->
            val saved = persistentQuestionStateMap[q.id]
            saved == null || !answerHandler.isQuestionAnswered(saved)
        }

        if (wrongCandidates.isEmpty() && untouchedCandidates.isEmpty()) {
            val refreshed = if (randomPracticeEnabled) {
                eligible.shuffled(java.util.Random(seed))
            } else {
                eligible
            }.take(targetCount)
            return MemoryRoundPlan(refreshed, emptySet())
        }

        val rnd = java.util.Random(seed)
        val shuffledWrong = if (randomPracticeEnabled) wrongCandidates.shuffled(rnd) else wrongCandidates
        val shuffledUntouched = if (randomPracticeEnabled) untouchedCandidates.shuffled(rnd) else untouchedCandidates

        val chosenWrong = shuffledWrong.take(targetCount)
        val remaining = (targetCount - chosenWrong.size).coerceAtLeast(0)
        val chosenUntouched = if (remaining > 0) shuffledUntouched.take(remaining) else emptyList()

        val chosen = (chosenWrong + chosenUntouched).distinctBy { it.id }
        return MemoryRoundPlan(chosen, chosenWrong.map { it.id }.toSet())
    }

    fun updatePersistentStateMap(
        currentState: PracticeSessionState,
        target: MutableMap<Int, UnifiedQuestionState>
    ) {
        currentState.questionsWithState.forEach { qws ->
            target[qws.question.id] = UnifiedQuestionState(
                questionId = qws.question.id,
                selectedOptions = qws.selectedOptions,
                textAnswer = qws.textAnswer,
                showResult = qws.showResult,
                analysis = qws.analysis,
                sparkAnalysis = qws.sparkAnalysis,
                baiduAnalysis = qws.baiduAnalysis,
                note = qws.note,
                answerTime = qws.sessionAnswerTime,
                displayedQuestionContent = qws.question.content,
                displayedQuestionAnswer = qws.question.answer
            )
        }
    }

    fun effectiveCurrentMemoryRoundQuestionIds(
        questionsWithState: List<QuestionWithState>,
        memoryModeActive: Boolean,
        memoryPoolMode: Int,
        currentMemoryRoundQuestionIds: Set<Int>
    ): Set<Int> {
        if (!memoryModeActive || memoryPoolMode != MEMORY_POOL_MODE_ROUND) return currentMemoryRoundQuestionIds
        if (currentMemoryRoundQuestionIds.isNotEmpty()) return currentMemoryRoundQuestionIds
        return questionsWithState.map { it.question.id }.toSet()
    }

    fun fallbackAnswerTime(index: Int, total: Int, sessionStartTime: Long): Long {
        return (index + 1).toLong().coerceAtMost(total.toLong()).times(sessionStartTime % 1000L)
    }

    fun hasConfiguredQuestionSnapshot(state: UnifiedQuestionState): Boolean =
        state.displayedQuestionContent.orEmpty().isNotBlank() || state.displayedQuestionAnswer.orEmpty().isNotBlank()

    fun restoreConfiguredQuestionSnapshot(base: Question, saved: UnifiedQuestionState?): Question {
        if (!isConfiguredSnapshotCompatible(base, saved)) return base
        val s = saved ?: return base
        return base.copy(
            id = s.questionId,
            content = s.displayedQuestionContent.orEmpty().ifBlank { base.content },
            answer = s.displayedQuestionAnswer.orEmpty().ifBlank { base.answer }
        )
    }

    fun isConfiguredSnapshotCompatible(base: Question, saved: UnifiedQuestionState?): Boolean {
        if (saved == null || !answerHandler.isQuestionAnswered(saved) || !hasConfiguredQuestionSnapshot(saved)) return false
        val contentOk = saved.displayedQuestionContent.orEmpty().isBlank() || saved.displayedQuestionContent == base.content
        val answerOk = saved.displayedQuestionAnswer.orEmpty().isBlank() || saved.displayedQuestionAnswer == base.answer
        return contentOk && answerOk
    }

    fun shouldUseMemoryModeFor(id: String, memoryModeEnabled: Boolean): Boolean = memoryModeEnabled

    fun memoryWrongModeResolved(wrongMode: Int): Int =
        if (wrongMode == MEMORY_WRONG_MODE_REDO_ALL_BLANKS) MEMORY_WRONG_MODE_REDO_ALL_BLANKS else MEMORY_WRONG_MODE_RETRY_WRONG_BLANKS

    fun memoryPoolModeResolved(poolMode: Int): Int =
        if (poolMode == MEMORY_POOL_MODE_ROUND) MEMORY_POOL_MODE_ROUND else MEMORY_POOL_MODE_IN_OUT

    suspend fun buildMemoryRoundStates(
        plan: MemoryRoundPlan,
        memoryWrongMode: Int
    ): List<QuestionWithState> {
        val seed = System.currentTimeMillis()
        val configuredQuestions = applyConfiguredFillQuestions(plan.questions, seed)
        return configuredQuestions.map { question ->
            val savedState = persistentQuestionStateMap[question.id]
            val base = buildStoredQuestionState(question, savedState)
            if (question.id in plan.wrongQuestionIds) {
                if (QuestionTypes.isFill(question.type) && memoryWrongMode == MEMORY_WRONG_MODE_RETRY_WRONG_BLANKS) {
                    val retainedAnswer = retainCorrectFillAnswerParts(
                        userAnswer = savedState?.textAnswer.orEmpty(),
                        correctAnswer = resolveFillCorrectAnswer(question)
                    )
                    base.copy(
                        textAnswer = retainedAnswer,
                        selectedOptions = if (retainedAnswer.isNotBlank()) listOf(-1) else emptyList(),
                        showResult = false,
                        sessionAnswerTime = 0L
                    )
                } else {
                    base.copy(textAnswer = "", selectedOptions = emptyList(), showResult = false, sessionAnswerTime = 0L)
                }
            } else {
                base.copy(showResult = false, sessionAnswerTime = 0L)
            }
        }
    }

    suspend fun refreshMemoryRoundPoolIfNeeded(
        answeredIndex: Int,
        memoryModeActive: Boolean,
        memoryPoolMode: Int,
        memoryModeBatchSize: Int,
        allSourceQuestions: List<Question>,
        randomPracticeEnabled: Boolean
    ): Boolean {
        if (!memoryModeActive) return false
        if (memoryPoolMode == MEMORY_POOL_MODE_ROUND) return false

        val currentState = _sessionState.value
        if (currentState.questionsWithState.isEmpty() || allSourceQuestions.isEmpty()) return false

        updatePersistentStateMapFn(currentState)

        val targetCount = memoryModeBatchSize.coerceIn(1, allSourceQuestions.size.coerceAtLeast(1))
        val currentQuestions = currentState.questionsWithState

        val retained = currentQuestions.filter { qws ->
            val state = UnifiedQuestionState(questionId = qws.question.id, selectedOptions = qws.selectedOptions,
                textAnswer = qws.textAnswer, showResult = qws.showResult, analysis = qws.analysis,
                sparkAnalysis = qws.sparkAnalysis, baiduAnalysis = qws.baiduAnalysis, note = qws.note,
                answerTime = qws.sessionAnswerTime)
            val answered = answerHandler.isQuestionAnswered(state)
            val correct = answerHandler.isQuestionCorrect(qws.question, state)
            !(answered && correct)
        }

        val retainedIds = retained.map { it.question.id }.toSet()
        val needed = (targetCount - retained.size).coerceAtLeast(0)

        val unseenOutside = allSourceQuestions
            .filter { it.id !in retainedIds && it.id !in removedMemoryPoolQuestionIds }
            .filter { q ->
                val saved = persistentQuestionStateMap[q.id]
                saved == null || !answerHandler.isQuestionAnswered(saved)
            }

        val supplements = if (needed <= 0) emptyList()
        else if (randomPracticeEnabled) unseenOutside.shuffled(java.util.Random(System.currentTimeMillis())).take(needed)
        else unseenOutside.take(needed)

        if (supplements.isEmpty() && retained.size == currentQuestions.size) return false

        val supplementById = mutableMapOf<Int, QuestionWithState>()
        for (question in supplements) {
            val built = buildStoredQuestionState(question, persistentQuestionStateMap[question.id]).copy(
                showResult = false, selectedOptions = emptyList(), textAnswer = "", sessionAnswerTime = 0L)
            supplementById[question.id] = built
        }

        val combinedQuestions = retained + supplements.mapNotNull { supplementById[it.id] }
        if (combinedQuestions.isEmpty()) return false

        setCurrentMemoryRoundQuestionIds(combinedQuestions.map { it.question.id }.toSet())

        val oldAnsweredQuestionId = currentQuestions.getOrNull(answeredIndex)?.question?.id
        val nextIndexByOldId = combinedQuestions.indexOfFirst { it.question.id == oldAnsweredQuestionId }
        val newCurrentIndex = if (nextIndexByOldId >= 0) nextIndexByOldId else answeredIndex.coerceIn(0, combinedQuestions.lastIndex)

        _sessionState.value = currentState.copy(
            questionsWithState = combinedQuestions, currentIndex = newCurrentIndex, progressLoaded = true)
        saveProgress()
        return true
    }

    fun removeCurrentQuestionFromMemoryPool(
        memoryModeActive: Boolean,
        memoryPoolMode: Int,
        memoryModeBatchSize: Int,
        allSourceQuestions: List<Question>,
        randomPracticeEnabled: Boolean,
        questionCount: Int
    ) {
        scope.launch {
            if (!memoryModeActive) return@launch
            val currentState = _sessionState.value
            val currentQuestion = currentState.questionsWithState.getOrNull(currentState.currentIndex) ?: return@launch
            if (allSourceQuestions.isEmpty()) return@launch

            updatePersistentStateMapFn(currentState)
            removedMemoryPoolQuestionIds.add(currentQuestion.question.id)

            if (memoryPoolMode == MEMORY_POOL_MODE_ROUND) {
                val remainingQuestions = currentState.questionsWithState.filter { it.question.id != currentQuestion.question.id }
                if (remainingQuestions.isNotEmpty()) {
                    setCurrentMemoryRoundQuestionIds(remainingQuestions.map { it.question.id }.toSet())
                    clearRandomNavigationHistory()
                    _sessionState.value = currentState.copy(
                        questionsWithState = remainingQuestions,
                        currentIndex = currentState.currentIndex.coerceAtMost(remainingQuestions.lastIndex),
                        progressLoaded = true)
                    saveProgress()
                    return@launch
                }

                val nextSeed = System.currentTimeMillis()
                val plan = buildMemoryRoundPlan(allSourceQuestions, nextSeed, questionCount,
                    memoryModeBatchSize, persistentQuestionStateMap, removedMemoryPoolQuestionIds, randomPracticeEnabled)
                if (plan.questions.isEmpty()) {
                    _sessionState.value = currentState.copy(
                        questionsWithState = emptyList(), currentIndex = 0, sessionStartTime = nextSeed, progressLoaded = true)
                    saveProgress()
                    return@launch
                }

                val nextRoundStates = buildMemoryRoundStates(plan, MEMORY_WRONG_MODE_RETRY_WRONG_BLANKS)
                setCurrentMemoryRoundQuestionIds(plan.questions.map { it.id }.toSet())
                _sessionState.value = currentState.copy(
                    questionsWithState = nextRoundStates, currentIndex = 0, sessionStartTime = nextSeed, progressLoaded = true)
                clearRandomNavigationHistory()
                saveProgress()
                return@launch
            }

            val remainingQuestions = currentState.questionsWithState.filter { it.question.id != currentQuestion.question.id }
            val remainingIds = remainingQuestions.map { it.question.id }.toSet()
            val targetCount = memoryModeBatchSize.coerceIn(1, allSourceQuestions.size.coerceAtLeast(1))
            val needed = (targetCount - remainingQuestions.size).coerceAtLeast(0)

            val candidates = allSourceQuestions
                .filter { it.id !in remainingIds && it.id !in removedMemoryPoolQuestionIds }
                .filter { q ->
                    val saved = persistentQuestionStateMap[q.id]
                    saved == null || !answerHandler.isQuestionAnswered(saved)
                }

            val supplements = if (needed <= 0) emptyList()
            else if (randomPracticeEnabled) candidates.shuffled(java.util.Random(System.currentTimeMillis())).take(needed)
            else candidates.take(needed)

            val supplementStates = supplements.map { question ->
                buildStoredQuestionState(question, persistentQuestionStateMap[question.id]).copy(
                    showResult = false, selectedOptions = emptyList(), textAnswer = "", sessionAnswerTime = 0L)
            }

            val combinedQuestions = remainingQuestions + supplementStates
            if (combinedQuestions.isEmpty()) {
                val nextSeed = System.currentTimeMillis()
                val plan = buildMemoryRoundPlan(allSourceQuestions, nextSeed, questionCount,
                    memoryModeBatchSize, persistentQuestionStateMap, removedMemoryPoolQuestionIds, randomPracticeEnabled)
                if (plan.questions.isEmpty()) {
                    _sessionState.value = currentState.copy(
                        questionsWithState = emptyList(), currentIndex = 0, sessionStartTime = nextSeed, progressLoaded = true)
                    saveProgress()
                    return@launch
                }

                val nextRoundStates = buildMemoryRoundStates(plan, MEMORY_WRONG_MODE_RETRY_WRONG_BLANKS)
                setCurrentMemoryRoundQuestionIds(plan.questions.map { it.id }.toSet())
                clearRandomNavigationHistory()
                _sessionState.value = currentState.copy(
                    questionsWithState = nextRoundStates, currentIndex = 0, sessionStartTime = nextSeed, progressLoaded = true)
                saveProgress()
                return@launch
            }

            setCurrentMemoryRoundQuestionIds(combinedQuestions.map { it.question.id }.toSet())
            val newIndex = currentState.currentIndex.coerceAtMost(combinedQuestions.lastIndex)
            _sessionState.value = currentState.copy(
                questionsWithState = combinedQuestions, currentIndex = newIndex, progressLoaded = true)
            saveProgress()
        }
    }

    suspend fun advanceMemoryRoundIfNeeded(
        memoryModeActive: Boolean,
        randomPracticeEnabled: Boolean,
        memoryModeBatchSize: Int,
        allSourceQuestions: List<Question>,
        questionCount: Int,
        memoryWrongMode: Int
    ): Boolean {
        if (!memoryModeActive) return false

        val currentState = _sessionState.value
        if (hasPendingQuestions(currentState.questionsWithState)) return false
        if (allSourceQuestions.isEmpty()) return false

        updatePersistentStateMapFn(currentState)

        val nextSeed = System.currentTimeMillis()
        val plan = buildMemoryRoundPlan(allSourceQuestions, nextSeed, questionCount,
            memoryModeBatchSize, persistentQuestionStateMap, removedMemoryPoolQuestionIds, randomPracticeEnabled)
        if (plan.questions.isEmpty()) return false

        val nextRoundStates = buildMemoryRoundStates(plan, memoryWrongMode)
        setCurrentMemoryRoundQuestionIds(plan.questions.map { it.id }.toSet())
        _sessionState.value = currentState.copy(
            questionsWithState = nextRoundStates, currentIndex = 0, sessionStartTime = nextSeed, progressLoaded = true)
        clearRandomNavigationHistory()
        saveProgress()
        return true
    }

    data class MemoryRoundPlan(
        val questions: List<Question>,
        val wrongQuestionIds: Set<Int>
    )
}

