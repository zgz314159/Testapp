package com.example.testapp.presentation.screen.exam

import com.example.testapp.core.util.resolveFillCorrectAnswer
import com.example.testapp.core.util.retainCorrectFillAnswerParts
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState
import com.example.testapp.domain.model.UnifiedQuestionState
import com.example.testapp.domain.usecase.ExamUseCaseFacade
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class ExamMemoryModeCoordinator(
    private val sessionState: MutableStateFlow<PracticeSessionState>,
    private val facade: ExamUseCaseFacade,
    private val memoryModeEngine: ExamMemoryModeEngine,
    private val answerRules: ExamAnswerRules,
    private val persistentQuestionStateMap: MutableMap<Int, UnifiedQuestionState>,
    private val memoryModeActive: () -> Boolean,
    private val memoryModeBatchSize: () -> Int,
    private val memoryWrongMode: () -> Int,
    private val memoryPoolMode: () -> Int,
    private val randomExamEnabled: () -> Boolean,
    private val allSourceQuestions: () -> List<Question>,
    private val currentMemoryRoundQuestionIds: () -> Set<Int>,
    private val setCurrentMemoryRoundQuestionIds: (Set<Int>) -> Unit,
    private val applyConfiguredFillQuestions: suspend (List<Question>) -> List<Question>,
    private val mergeCurrentStateToPersistentMap: () -> Unit,
    private val saveProgressInternal: suspend () -> Unit
) {

    private fun unansweredCount(): Int = sessionState.value.unansweredCount

    fun buildMemoryRoundPlan(sourceQuestions: List<Question>, seed: Long): MemoryRoundPlan =
        memoryModeEngine.buildMemoryRoundPlan(
            sourceQuestions = sourceQuestions,
            seed = seed,
            batchSize = memoryModeBatchSize(),
            randomEnabled = randomExamEnabled(),
            persistentMap = persistentQuestionStateMap,
            answerRules = answerRules
        )

    suspend fun initializeMemoryModeIfNeeded(seed: Long): Boolean {
        val sourceQuestions = allSourceQuestions()
        if (!memoryModeActive() || sourceQuestions.isEmpty()) return false
        val plan = buildMemoryRoundPlan(sourceQuestions, seed)
        if (plan.questions.isEmpty()) return false
        sessionState.update {
            it.copy(questionsWithState = applyConfiguredFillQuestions(plan.questions).map { q -> QuestionWithState(question = q) })
        }
        restoreStateForMemoryRound(plan)
        saveProgressInternal()
        return true
    }

    suspend fun refreshMemoryRoundPoolIfNeeded(answeredIndex: Int): Boolean {
        val sourceQuestions = allSourceQuestions()
        if (!memoryModeActive() || sourceQuestions.isEmpty()) return false
        if (memoryPoolMode() == ExamMemoryModeEngine.MEMORY_POOL_MODE_ROUND) return false

        mergeCurrentStateToPersistentMap()
        val currentQuestions = sessionState.value.questionsWithState.map { it.question }
        if (currentQuestions.isEmpty()) return false

        val retained = currentQuestions.filter { question ->
            persistentQuestionStateMap[question.id]?.let { state ->
                !(answerRules.isQuestionAnswered(state) && answerRules.isQuestionCorrect(question, state))
            } ?: true
        }
        val targetCount = memoryModeBatchSize().coerceIn(1, sourceQuestions.size.coerceAtLeast(1))
        val needed = (targetCount - retained.size).coerceAtLeast(0)
        val retainedIds = retained.map { it.id }.toSet()
        val unseen = sourceQuestions
            .filter { it.id !in retainedIds }
            .filter { question -> persistentQuestionStateMap[question.id]?.let { !answerRules.isQuestionAnswered(it) } ?: true }
        val supplemental = if (needed <= 0) {
            emptyList()
        } else if (randomExamEnabled()) {
            unseen.shuffled(java.util.Random(System.currentTimeMillis())).take(needed)
        } else {
            unseen.take(needed)
        }
        if (supplemental.isEmpty() && retained.size == currentQuestions.size) return false

        val nextQuestions = retained + supplemental
        if (nextQuestions.isEmpty()) return false

        val configuredQuestions = applyConfiguredFillQuestions(nextQuestions)
        val oldQuestionId = currentQuestions.getOrNull(answeredIndex)?.id
        val nextStates = configuredQuestions.map { question ->
            val state = persistentQuestionStateMap[question.id]
            val inRetained = question.id in retainedIds
            QuestionWithState(
                question = question,
                selectedOptions = if (inRetained) (state?.selectedOptions ?: emptyList()) else emptyList(),
                textAnswer = if (inRetained) (state?.textAnswer ?: "") else "",
                showResult = if (inRetained) (state?.showResult ?: false) && (state?.selectedOptions?.isNotEmpty() ?: false) else false,
                analysis = state?.analysis ?: "",
                sparkAnalysis = state?.sparkAnalysis ?: "",
                baiduAnalysis = state?.baiduAnalysis ?: "",
                note = state?.note ?: "",
                sessionAnswerTime = state?.answerTime ?: 0L
            )
        }
        val nextIndex = oldQuestionId?.let { id ->
            configuredQuestions.indexOfFirst { it.id == id }.let { if (it >= 0) it else answeredIndex.coerceIn(0, configuredQuestions.lastIndex) }
        } ?: answeredIndex.coerceIn(0, configuredQuestions.lastIndex)

        sessionState.value = PracticeSessionState(
            questionsWithState = nextStates,
            currentIndex = nextIndex,
            finished = false,
            progressLoaded = true
        )
        setCurrentMemoryRoundQuestionIds(configuredQuestions.map { it.id }.toSet())
        saveProgressInternal()
        return true
    }

    suspend fun advanceMemoryRoundIfNeeded(): Boolean {
        val sourceQuestions = allSourceQuestions()
        if (!memoryModeActive() || sourceQuestions.isEmpty()) return false
        if (unansweredCount() > 0) return false

        mergeCurrentStateToPersistentMap()
        val plan = buildMemoryRoundPlan(sourceQuestions, System.currentTimeMillis())
        if (plan.questions.isEmpty()) return false
        sessionState.update {
            it.copy(questionsWithState = applyConfiguredFillQuestions(plan.questions).map { q -> QuestionWithState(question = q) })
        }
        restoreStateForMemoryRound(plan)
        saveProgressInternal()
        return true
    }

    fun effectiveCurrentMemoryRoundQuestionIds(): Set<Int> =
        memoryModeEngine.effectiveCurrentMemoryRoundQuestionIds(
            questions = sessionState.value.questionsWithState.map { it.question },
            memoryActive = memoryModeActive(),
            poolMode = memoryPoolMode(),
            currentIds = currentMemoryRoundQuestionIds()
        )

    private suspend fun restoreStateForMemoryRound(plan: MemoryRoundPlan) {
        val questions = plan.questions
        val states = questions.map { question ->
            val state = persistentQuestionStateMap[question.id]
            val analysis = state?.analysis ?: facade.analysis.getDeepSeek(question.id).getOrNull().orEmpty()
            val spark = state?.sparkAnalysis ?: facade.analysis.getSpark(question.id).getOrNull().orEmpty()
            val baidu = state?.baiduAnalysis ?: facade.analysis.getBaidu(question.id).getOrNull().orEmpty()
            val note = state?.note ?: facade.notes.get(question.id).getOrNull().orEmpty()
            val selectedState = if (state != null && question.id in plan.wrongQuestionIds) {
                if (QuestionTypes.isFill(question.type) && memoryWrongMode() == ExamMemoryModeEngine.MEMORY_WRONG_MODE_RETRY_WRONG_BLANKS) {
                    val retained = retainCorrectFillAnswerParts(state.textAnswer, resolveFillCorrectAnswer(question))
                    state.copy(
                        textAnswer = retained,
                        selectedOptions = if (retained.isNotBlank()) listOf(-1) else emptyList()
                    )
                } else {
                    state
                }
            } else {
                state ?: UnifiedQuestionState(questionId = question.id)
            }

            QuestionWithState(
                question = question,
                selectedOptions = selectedState.selectedOptions,
                textAnswer = selectedState.textAnswer,
                showResult = selectedState.showResult && selectedState.selectedOptions.isNotEmpty(),
                analysis = analysis.takeUnless { it.isBlank() } ?: selectedState.analysis,
                sparkAnalysis = spark.takeUnless { it.isBlank() } ?: selectedState.sparkAnalysis,
                baiduAnalysis = baidu.takeUnless { it.isBlank() } ?: selectedState.baiduAnalysis,
                note = note.takeUnless { it.isBlank() } ?: selectedState.note,
                sessionAnswerTime = selectedState.answerTime
            )
        }
        sessionState.value = PracticeSessionState(
            questionsWithState = states,
            currentIndex = 0,
            finished = false,
            progressLoaded = true
        )
        setCurrentMemoryRoundQuestionIds(questions.map { it.id }.toSet())
    }
}
