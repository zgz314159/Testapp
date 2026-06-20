package com.example.testapp.presentation.screen.practice

import com.example.testapp.core.util.FillQuestionGenerationMode
import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.UnifiedQuestionState
import com.example.testapp.domain.model.QuestionWithState
import com.example.testapp.domain.usecase.ClearPracticeProgressUseCase
import com.example.testapp.domain.usecase.GetBaiduAnalysisUseCase
import com.example.testapp.domain.usecase.GetFavoriteQuestionsUseCase
import com.example.testapp.domain.usecase.GetPracticeProgressFlowUseCase
import com.example.testapp.domain.usecase.GetQuestionAnalysisUseCase
import com.example.testapp.domain.usecase.GetQuestionNoteUseCase
import com.example.testapp.domain.usecase.GetSparkAnalysisUseCase
import com.example.testapp.domain.usecase.GetWrongBookUseCase
import com.example.testapp.domain.usecase.SavePracticeProgressUseCase
import com.example.testapp.presentation.screen.practice.session.AnalysisManager
import com.example.testapp.presentation.screen.practice.session.ProgressPersistence
import com.example.testapp.presentation.screen.practice.session.QuestionLoader
import com.example.testapp.core.common.LocalizedResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex

/**
 * PracticeSessionCoordinator — 练习会话协调器，组合 3 个子模块：
 *   - ProgressPersistence : saveProgress / clearProgress / buildStoredQuestionState
 *   - AnalysisManager      : 4 类分析内容 (analysis/spark/baidu/note)
 *   - QuestionLoader       : loadWrongQuestions / loadFavoriteQuestions
 *
 * 对外暴露统一 API，内部按职责拆分，便于维护与测试。
 */
class PracticeSessionCoordinator(
    private val _sessionState: MutableStateFlow<PracticeSessionState>,
    private val _messageResult: MutableStateFlow<LocalizedResult?>,
    private val scope: CoroutineScope,
    private val saveProgressMutex: Mutex,
    // use cases
    private val savePracticeProgressUseCase: SavePracticeProgressUseCase,
    private val clearPracticeProgressUseCase: ClearPracticeProgressUseCase,
    private val getPracticeProgressFlowUseCase: GetPracticeProgressFlowUseCase,
    private val getQuestionAnalysisUseCase: GetQuestionAnalysisUseCase,
    private val getSparkAnalysisUseCase: GetSparkAnalysisUseCase,
    private val getBaiduAnalysisUseCase: GetBaiduAnalysisUseCase,
    private val getQuestionNoteUseCase: GetQuestionNoteUseCase,
    private val getWrongBookUseCase: GetWrongBookUseCase,
    private val getFavoriteQuestionsUseCase: GetFavoriteQuestionsUseCase,
    // lambda callbacks
    private val buildSessionIdWithFillSignature: (String, Long, String) -> String,
    private val currentFillConfigSignature: suspend () -> String,
    private val restoreConfiguredQuestionSnapshot: (Question, UnifiedQuestionState?) -> Question,
    private val clearRandomNavigationHistory: () -> Unit,
    private val resetLocalStateFn: () -> Unit,
    private val noteTraceTag: String,
    private val fillGenerationModeFn: suspend () -> FillQuestionGenerationMode,
    // mutable VM fields
    private val progressId: () -> String,
    private val memoryModeActive: () -> Boolean,
    private val memoryWrongMode: () -> Int,
    private val memoryModeBatchSize: () -> Int,
    private val randomPracticeEnabled: () -> Boolean,
    private val allSourceQuestions: () -> List<Question>,
    private val setAllSourceQuestions: (List<Question>) -> Unit,
    private val setCurrentMemoryRoundQuestionIds: (Set<Int>) -> Unit,
    private val persistentQuestionStateMap: MutableMap<Int, UnifiedQuestionState>,
    private val removedMemoryPoolQuestionIds: MutableSet<Int>,
    private val persistentQuestionStateMapSnapshot: () -> Map<Int, UnifiedQuestionState>,
    // sub-coordinators (injected)
    private val progressCoordinator: PracticeProgressCoordinator,
    private val fullAnswerCoordinator: PracticeFullAnswerCoordinator,
    private val modeCoordinator: PracticeModeCoordinator,
    // utility function refs
    private val extractSourceQuestionId: (Int) -> Int,
    private val shouldApplyDynamicFillTransformFn: (List<Question>, String) -> Boolean,
    private val applyConfiguredFillQuestions: suspend (List<Question>, Long) -> List<Question>,
    // navigation / answer lambdas
    private val seedRandomNavigationHistory: (List<QuestionWithState>, Int) -> Unit,
    private val findFirstPendingIndex: (List<QuestionWithState>) -> Int,
    private val findResumeIndex: (List<QuestionWithState>, Int) -> Int,
    private val currentFullAnswerCandidateIndices: (List<QuestionWithState>, Int, List<Int>) -> List<Int>,
    private val fallbackAnswerTime: (Int, Int, Long) -> Long
) {
    // === 子模块 ===
    private val persistence = ProgressPersistence(
        _sessionState, scope, saveProgressMutex,
        savePracticeProgressUseCase, clearPracticeProgressUseCase,
        buildSessionIdWithFillSignature, currentFillConfigSignature,
        resetLocalStateFn, noteTraceTag,
        progressId, memoryModeActive, allSourceQuestions,
        persistentQuestionStateMap
    )

    private val analysisManager = AnalysisManager(
        _sessionState, _messageResult, scope,
        getQuestionAnalysisUseCase, getSparkAnalysisUseCase,
        getBaiduAnalysisUseCase, getQuestionNoteUseCase
    ) { persistence.saveProgress() }

    private val questionLoader = QuestionLoader(
        _sessionState, scope, persistence,
        progressCoordinator, fullAnswerCoordinator, modeCoordinator,
        getWrongBookUseCase, getFavoriteQuestionsUseCase,
        getPracticeProgressFlowUseCase, savePracticeProgressUseCase,
        buildSessionIdWithFillSignature, currentFillConfigSignature,
        restoreConfiguredQuestionSnapshot,
        { id -> getQuestionAnalysisUseCase(id).getOrNull().orEmpty() },
        { id -> getSparkAnalysisUseCase(id).getOrNull().orEmpty() },
        { id -> getBaiduAnalysisUseCase(id).getOrNull().orEmpty() },
        { id -> getQuestionNoteUseCase(id).getOrNull().orEmpty() },
        ::loadProgress, clearRandomNavigationHistory,
        noteTraceTag, fillGenerationModeFn, progressId,
        memoryModeActive, memoryWrongMode, memoryModeBatchSize,
        randomPracticeEnabled, allSourceQuestions, setAllSourceQuestions,
        setCurrentMemoryRoundQuestionIds,
        persistentQuestionStateMap, removedMemoryPoolQuestionIds,
        persistentQuestionStateMapSnapshot,
        extractSourceQuestionId, shouldApplyDynamicFillTransformFn,
        applyConfiguredFillQuestions
    )

    // === 对外 API（委托） ===

    internal var progressLoadJob: Job? = null
    internal var supplementaryLoadJob: Job? = null

    fun cancelLoadJobs() {
        progressLoadJob?.cancel()
        progressLoadJob = null
        analysisManager.cancelSupplementaryJobs()
    }

    suspend fun buildStoredQuestionState(
        question: Question,
        savedState: UnifiedQuestionState?,
        includeRepositoryExtras: Boolean = true
    ): QuestionWithState = persistence.buildStoredQuestionState(
        question, savedState, restoreConfiguredQuestionSnapshot,
        { id -> if (includeRepositoryExtras) getQuestionAnalysisUseCase(id).getOrNull().orEmpty() else "" },
        { id -> if (includeRepositoryExtras) getSparkAnalysisUseCase(id).getOrNull().orEmpty() else "" },
        { id -> if (includeRepositoryExtras) getBaiduAnalysisUseCase(id).getOrNull().orEmpty() else "" },
        { id -> if (includeRepositoryExtras) getQuestionNoteUseCase(id).getOrNull().orEmpty() else "" }
    )

    fun saveProgress() = persistence.saveProgress()
    fun clearProgress() = persistence.clearProgress()

    // ============================================================
    // loadProgress — 从持久化恢复进度，并在必要时强制还原会话状态
    // ============================================================

    fun loadProgress(forceRestore: Boolean = false) {
        progressLoadJob?.cancel()
        progressLoadJob = scope.launch(Dispatchers.Default) {
            var pendingForcedRestore = forceRestore
            getPracticeProgressFlowUseCase(progressId()).collectLatest { progress ->
                val currentState = _sessionState.value
                val shouldRestoreNow = pendingForcedRestore || !currentState.progressLoaded

                if (progress != null && shouldRestoreNow) {
                    val updatedQuestionsWithState = if (progress.questionStateMap.isNotEmpty()) {
                        currentState.questionsWithState.mapIndexed { index, questionWithState ->
                            val questionId = questionWithState.question.id
                            val savedState = progress.questionStateMap[questionId]

                            if (savedState != null) {
                                val hasAnswerContent = if (com.example.testapp.domain.QuestionTypes.isFill(questionWithState.question.type)) {
                                    savedState.textAnswer.isNotBlank()
                                } else {
                                    savedState.selectedOptions.isNotEmpty()
                                }
                                val isAnswered = savedState.showResult || hasAnswerContent

                                val recoveredAnswerTime = if (isAnswered && savedState.answerTime == 0L) {
                                    fallbackAnswerTime(index, currentState.questionsWithState.size, currentState.sessionStartTime)
                                } else {
                                    savedState.answerTime
                                }

                                val shouldShowResult = savedState.showResult && isAnswered

                                questionWithState.copy(
                                    selectedOptions = savedState.selectedOptions,
                                    textAnswer = savedState.textAnswer,
                                    showResult = shouldShowResult,
                                    analysis = savedState.analysis.takeIf { it.isNotBlank() } ?: questionWithState.analysis,
                                    sparkAnalysis = savedState.sparkAnalysis.takeIf { it.isNotBlank() } ?: questionWithState.sparkAnalysis,
                                    baiduAnalysis = savedState.baiduAnalysis.takeIf { it.isNotBlank() } ?: questionWithState.baiduAnalysis,
                                    note = savedState.note.takeIf { it.isNotBlank() } ?: questionWithState.note,
                                    sessionAnswerTime = recoveredAnswerTime
                                )
                            } else {
                                questionWithState
                            }
                        }
                    } else {
                        currentState.questionsWithState.mapIndexed { index, questionWithState ->
                            val selectedOptions = progress.selectedOptions.getOrElse(index) { emptyList() }
                            val originalShowResult = progress.showResultList.getOrElse(index) { false }
                            val analysis = progress.analysisList.getOrElse(index) { "" }
                            val sparkAnalysis = progress.sparkAnalysisList.getOrElse(index) { "" }
                            val baiduAnalysis = progress.baiduAnalysisList.getOrElse(index) { "" }
                            val note = progress.noteList.getOrElse(index) { "" }

                            val shouldShowResult = originalShowResult && selectedOptions.isNotEmpty()
                            val sessionAnswerTime = if (shouldShowResult && questionWithState.sessionAnswerTime == 0L && selectedOptions.isNotEmpty()) {
                                currentState.sessionStartTime - 1000L
                            } else {
                                questionWithState.sessionAnswerTime
                            }

                            questionWithState.copy(
                                selectedOptions = selectedOptions,
                                textAnswer = "",
                                showResult = shouldShowResult,
                                analysis = analysis,
                                sparkAnalysis = sparkAnalysis,
                                baiduAnalysis = baiduAnalysis,
                                note = note.takeIf { it.isNotBlank() } ?: questionWithState.note,
                                sessionAnswerTime = sessionAnswerTime
                            )
                        }
                    }

                    val newCurrentIndex = progress.currentIndex.coerceAtMost(currentState.questionsWithState.size - 1)
                    val smartCurrentIndex = if (randomPracticeEnabled()) {
                        val candidateIndices = currentFullAnswerCandidateIndices(
                            updatedQuestionsWithState, newCurrentIndex,
                            updatedQuestionsWithState.indices.filter { it != newCurrentIndex }
                        )
                        if (candidateIndices.isNotEmpty()) candidateIndices.random(kotlin.random.Random(currentState.sessionStartTime))
                        else newCurrentIndex
                    } else {
                        findResumeIndex(updatedQuestionsWithState, newCurrentIndex)
                    }

                    _sessionState.value = currentState.copy(
                        currentIndex = smartCurrentIndex,
                        questionsWithState = updatedQuestionsWithState,
                        progressLoaded = true
                    )
                    seedRandomNavigationHistory(updatedQuestionsWithState, smartCurrentIndex)
                    pendingForcedRestore = false

                } else if (progress == null && shouldRestoreNow) {
                    val smartStartIndex = if (randomPracticeEnabled() && currentState.questionsWithState.isNotEmpty()) {
                        (0 until currentState.questionsWithState.size).random(kotlin.random.Random(currentState.sessionStartTime))
                    } else {
                        findFirstPendingIndex(currentState.questionsWithState)
                    }

                    _sessionState.value = currentState.copy(
                        currentIndex = smartStartIndex,
                        progressLoaded = true
                    )
                    clearRandomNavigationHistory()
                    saveProgress()
                    pendingForcedRestore = false
                }
            }
        }
    }

    fun enqueueSupplementaryRepositoryLoads() = analysisManager.enqueueSupplementaryRepositoryLoads()
    fun loadWrongQuestions(fileName: String) = questionLoader.loadWrongQuestions(fileName)
    fun loadFavoriteQuestions(fileName: String) = questionLoader.loadFavoriteQuestions(fileName)
}

