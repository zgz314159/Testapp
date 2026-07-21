package com.example.testapp.presentation.screen.practice.session

import com.example.testapp.core.util.FillQuestionGenerationMode
import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.UnifiedQuestionState
import com.example.testapp.domain.usecase.GetFavoriteQuestionsUseCase
import com.example.testapp.domain.usecase.GetPracticeProgressFlowUseCase
import com.example.testapp.domain.usecase.GetWrongBookUseCase
import com.example.testapp.domain.usecase.SavePracticeProgressUseCase
import com.example.testapp.presentation.screen.practice.PracticeFullAnswerCoordinator
import com.example.testapp.presentation.screen.practice.PracticeModeCoordinator
import com.example.testapp.presentation.screen.practice.PracticeProgressCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * Question Loader: loadWrongQuestions + loadFavoriteQuestions
 * Extracted from PracticeSessionCoordinator to keep each file <200 lines.
 */
class QuestionLoader(
    private val _sessionState: MutableStateFlow<PracticeSessionState>,
    private val scope: CoroutineScope,
    private val progressPersistence: ProgressPersistence,
    // sub-coordinators
    private val progressCoordinator: PracticeProgressCoordinator,
    private val fullAnswerCoordinator: PracticeFullAnswerCoordinator,
    private val modeCoordinator: PracticeModeCoordinator,
    // use cases
    private val getWrongBookUseCase: GetWrongBookUseCase,
    private val getFavoriteQuestionsUseCase: GetFavoriteQuestionsUseCase,
    private val getPracticeProgressFlowUseCase: GetPracticeProgressFlowUseCase,
    private val savePracticeProgressUseCase: SavePracticeProgressUseCase,
    // lambda callbacks
    private val buildSessionIdWithFillSignature: (String, Long, String) -> String,
    private val currentFillConfigSignature: suspend () -> String,
    private val restoreConfiguredQuestionSnapshot: (Question, UnifiedQuestionState?) -> Question,
    private val getAnalysis: suspend (Int) -> String,
    private val getSpark: suspend (Int) -> String,
    private val getBaidu: suspend (Int) -> String,
    private val getNote: suspend (Int) -> String,
    private val loadProgressFn: (Boolean) -> Unit,
    private val clearRandomNavigationHistory: () -> Unit,
    private val noteTraceTag: String,
    private val fillGenerationModeFn: suspend () -> FillQuestionGenerationMode,
    // mutable VM fields
    private val progressId: () -> String,
    private val memoryModeActive: () -> Boolean,
    private val memoryWrongMode: () -> Int,
    private val memoryModeBatchSize: () -> Int,
    private val randomPracticeEnabled: () -> Boolean,
    private val allSourceQuestionsRef: () -> List<Question>,
    private val setAllSourceQuestions: (List<Question>) -> Unit,
    private val setCurrentMemoryRoundQuestionIds: (Set<Int>) -> Unit,
    private val persistentQuestionStateMap: MutableMap<Int, UnifiedQuestionState>,
    private val removedMemoryPoolQuestionIds: MutableSet<Int>,
    private val persistentQuestionStateMapSnapshot: () -> Map<Int, UnifiedQuestionState>,
    private val extractSourceQuestionId: (Int) -> Int,
    private val shouldApplyDynamicFillTransformFn: (List<Question>, String) -> Boolean,
    private val applyConfiguredFillQuestions: suspend (List<Question>, Long) -> List<Question>
) {
    fun loadWrongQuestions(fileName: String) {
        scope.launch {
            clearRandomNavigationHistory()
            val newSessionStartTime = System.currentTimeMillis()

            val wrongList = getWrongBookUseCase().firstOrNull().orEmpty()
            var existingProgress = getPracticeProgressFlowUseCase(progressId()).firstOrNull()
            val curFillSignature = currentFillConfigSignature()
            val fillGenerationMode = fillGenerationModeFn()
            val filtered = wrongList.filter { it.question.fileName == fileName }
            val list = filtered.map { it.question }
            if (list.isEmpty()) {
                _sessionState.value = PracticeSessionState(progressLoaded = true)
                return@launch
            }
            val fillConfigSensitive = shouldApplyDynamicFillTransformFn(list, fileName)
            val canReuseByFill = progressCoordinator.canReuseByFillSignature(existingProgress?.sessionId, curFillSignature, fillConfigSensitive)
            val savedFillSignature = progressCoordinator.extractFillConfigSignature(existingProgress?.sessionId)
            if (existingProgress != null && savedFillSignature.isNullOrBlank() && canReuseByFill) {
                val upgradedProgress = existingProgress.copy(
                    sessionId = buildSessionIdWithFillSignature(
                        progressId(),
                        progressCoordinator.practiceProgressSeed(existingProgress, newSessionStartTime),
                        curFillSignature
                    )
                )
                savePracticeProgressUseCase(upgradedProgress)
                existingProgress = upgradedProgress
            }
            setAllSourceQuestions(list)
            if (memoryModeActive()) {
                loadMemoryModeQuestions(list, existingProgress, newSessionStartTime)
                return@launch
            }

            val savedSourceOrder = existingProgress?.fixedQuestionOrder?.let { savedOrder ->
                if (fillGenerationMode == FillQuestionGenerationMode.FULL_ANSWER)
                    savedOrder.map(extractSourceQuestionId).distinct()
                else savedOrder
            }?.takeIf { canReuseByFill }.orEmpty()

            val expectedSequentialOrder = list.map { it.id }
            val smartOrderedList = reuseOrUseList(list, savedSourceOrder, expectedSequentialOrder)

            val fillSeed = progressCoordinator.practiceProgressSeed(existingProgress, newSessionStartTime)
            val configured = applyConfiguredFillQuestions(smartOrderedList, fillSeed)
            val configuredQuestions = fullAnswerCoordinator.restoreConfiguredQuestionsForProgress(
                smartOrderedList, configured, existingProgress
            )
            val questionsWithState = configuredQuestions.map { question ->
                progressPersistence.buildStoredQuestionState(
                    question, existingProgress?.questionStateMap?.get(question.id),
                    restoreConfiguredQuestionSnapshot, getAnalysis, getSpark, getBaidu, getNote
                )
            }
            _sessionState.value = _sessionState.value.copy(
                questionsWithState = questionsWithState,
                progressLoaded = false,
                sessionStartTime = newSessionStartTime
            )
            loadProgressFn(false)
        }
    }

    fun loadFavoriteQuestions(fileName: String) {
        scope.launch {
            clearRandomNavigationHistory()
            val newSessionStartTime = System.currentTimeMillis()

            val favList = getFavoriteQuestionsUseCase().firstOrNull().orEmpty()
            var existingProgress = getPracticeProgressFlowUseCase(progressId()).firstOrNull()
            val curFillSignature = currentFillConfigSignature()
            val fillGenerationMode = fillGenerationModeFn()
            val filtered = favList.filter { it.question.fileName == fileName }
            val list = filtered.map { it.question }
            if (list.isEmpty()) {
                _sessionState.value = PracticeSessionState(progressLoaded = true)
                return@launch
            }
            val fillConfigSensitive = shouldApplyDynamicFillTransformFn(list, fileName)
            val canReuseByFill = progressCoordinator.canReuseByFillSignature(existingProgress?.sessionId, curFillSignature, fillConfigSensitive)
            val savedFillSignature = progressCoordinator.extractFillConfigSignature(existingProgress?.sessionId)
            if (existingProgress != null && savedFillSignature.isNullOrBlank() && canReuseByFill) {
                val upgradedProgress = existingProgress.copy(
                    sessionId = buildSessionIdWithFillSignature(
                        progressId(),
                        progressCoordinator.practiceProgressSeed(existingProgress, newSessionStartTime),
                        curFillSignature
                    )
                )
                savePracticeProgressUseCase(upgradedProgress)
                existingProgress = upgradedProgress
            }
            setAllSourceQuestions(list)
            if (memoryModeActive()) {
                loadMemoryModeQuestions(list, existingProgress, newSessionStartTime)
                return@launch
            }

            val savedSourceOrder = existingProgress?.fixedQuestionOrder?.let { savedOrder ->
                if (fillGenerationMode == FillQuestionGenerationMode.FULL_ANSWER)
                    savedOrder.map(extractSourceQuestionId).distinct()
                else savedOrder
            }?.takeIf { canReuseByFill }.orEmpty()

            val expectedSequentialOrder = list.map { it.id }
            val smartOrderedList = reuseOrUseList(list, savedSourceOrder, expectedSequentialOrder)

            val fillSeed = progressCoordinator.practiceProgressSeed(existingProgress, newSessionStartTime)
            val configured = applyConfiguredFillQuestions(smartOrderedList, fillSeed)
            val configuredQuestions = fullAnswerCoordinator.restoreConfiguredQuestionsForProgress(
                smartOrderedList, configured, existingProgress
            )
            val questionsWithState = configuredQuestions.map { question ->
                progressPersistence.buildStoredQuestionState(
                    question, existingProgress?.questionStateMap?.get(question.id),
                    restoreConfiguredQuestionSnapshot, getAnalysis, getSpark, getBaidu, getNote
                )
            }
            _sessionState.value = _sessionState.value.copy(
                questionsWithState = questionsWithState,
                progressLoaded = false,
                sessionStartTime = newSessionStartTime
            )
            loadProgressFn(false)
        }
    }

    private suspend fun loadMemoryModeQuestions(
        list: List<Question>,
        existingProgress: com.example.testapp.domain.model.PracticeProgress?,
        newSessionStartTime: Long
    ) {
        persistentQuestionStateMap.clear()
        persistentQuestionStateMap.putAll(existingProgress?.questionStateMap.orEmpty())
        val plan = modeCoordinator.buildMemoryRoundPlan(
            list, newSessionStartTime, list.size,
            memoryModeBatchSize(),
            persistentQuestionStateMapSnapshot(),
            removedMemoryPoolQuestionIds,
            randomPracticeEnabled()
        )
        setCurrentMemoryRoundQuestionIds(plan.questions.map { it.id }.toSet())
        val questionsWithState = if (plan.questions.isEmpty()) emptyList()
        else modeCoordinator.buildMemoryRoundStates(plan, memoryWrongMode())
        _sessionState.value = _sessionState.value.copy(
            questionsWithState = questionsWithState,
            progressLoaded = true,
            sessionStartTime = newSessionStartTime,
            currentIndex = 0
        )
        progressPersistence.saveProgress()
    }

    private fun reuseOrUseList(
        list: List<Question>,
        savedSourceOrder: List<Int>,
        expectedSequentialOrder: List<Int>
    ): List<Question> {
        if (savedSourceOrder.isNotEmpty() &&
            fullAnswerCoordinator.shouldReuseSavedSourceOrder(
                savedSourceOrder = savedSourceOrder,
                expectedQuestionCount = expectedSequentialOrder.size,
                expectedSequentialOrder = expectedSequentialOrder,
                randomEnabled = false
            )
        ) {
            val questionsMap = list.associateBy { it.id }
            return savedSourceOrder.mapNotNull { questionsMap[it] }
        }
        return list
    }
}
