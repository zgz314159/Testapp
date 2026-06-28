package com.example.testapp.presentation.screen.exam

import com.example.testapp.domain.review.ReviewBrowseSession
import com.example.testapp.domain.review.ReviewAnsweredSwipePipeline
import com.example.testapp.domain.review.SessionReviewPresentation
import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState
import com.example.testapp.domain.model.SessionMode
import com.example.testapp.domain.model.UnifiedQuestionState
import com.example.testapp.domain.model.UnifiedSessionState
import com.example.testapp.domain.model.updateAt
import com.example.testapp.domain.usecase.ExamUseCaseFacade
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.core.common.FontSettingsRepository
import com.example.testapp.core.common.LocalizedResult
import com.example.testapp.core.session.NavigationSaveScheduler
import com.example.testapp.core.session.SessionEngine
import com.example.testapp.core.util.*
import com.example.testapp.uicommon.component.AnswerCardDisplayInfoPipeline
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ExamViewModel @Inject constructor(
    val fontSettingsRepository: FontSettingsRepository,
    private val sessionEngine: SessionEngine,
    private val facade: ExamUseCaseFacade,
    val answerRules: ExamAnswerRules,
    private val fillTransform: ExamFillTransform,
    val memoryModeEngine: ExamMemoryModeEngine,
    val navHelper: ExamNavigationHelper,
    private val loadDelegate: ExamLoadDelegate
) : ViewModel() {

    // ================================================================
    // 核心会话状态
    // ================================================================

    private val _sessionState = MutableStateFlow(PracticeSessionState(finished = false))
    val sessionState: StateFlow<PracticeSessionState> = _sessionState.asStateFlow()

    // 派生 StateFlow，供界面订阅
    val questions: StateFlow<List<Question>> = _sessionState.map { it.questions }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val currentIndex: StateFlow<Int> = _sessionState.map { it.currentIndex }.stateIn(viewModelScope, SharingStarted.Lazily, 0)
    val selectedOptions: StateFlow<List<List<Int>>> = _sessionState.map { s -> s.questionsWithState.map { it.selectedOptions } }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val textAnswers: StateFlow<List<String>> = _sessionState.map { s -> s.questionsWithState.map { it.textAnswer } }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val showResultList: StateFlow<List<Boolean>> = _sessionState.map { s -> s.questionsWithState.map { it.showResult } }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val answerTimeList: StateFlow<List<Long>> = _sessionState.map { s -> s.questionsWithState.map { it.sessionAnswerTime } }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val analysisList: StateFlow<List<String>> = _sessionState.map { s -> s.questionsWithState.map { it.analysis } }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val sparkAnalysisList: StateFlow<List<String>> = _sessionState.map { s -> s.questionsWithState.map { it.sparkAnalysis } }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val baiduAnalysisList: StateFlow<List<String>> = _sessionState.map { s -> s.questionsWithState.map { it.baiduAnalysis } }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val noteList: StateFlow<List<String>> = _sessionState.map { s -> s.questionsWithState.map { it.note } }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val progressLoaded: StateFlow<Boolean> = _sessionState.map { it.progressLoaded }.stateIn(viewModelScope, SharingStarted.Lazily, false)
    val finished: StateFlow<Boolean> = _sessionState.map { it.finished }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    // 累计考试统计
    private val _cumulativeCorrect = MutableStateFlow(0)
    val cumulativeCorrect: StateFlow<Int> = _cumulativeCorrect.asStateFlow()
    private val _cumulativeAnswered = MutableStateFlow(0)
    val cumulativeAnswered: StateFlow<Int> = _cumulativeAnswered.asStateFlow()
    private val _cumulativeExamCount = MutableStateFlow(0)
    val cumulativeExamCount: StateFlow<Int> = _cumulativeExamCount.asStateFlow()
    private val _messageResult = MutableStateFlow<LocalizedResult?>(null)
    val messageResult: StateFlow<LocalizedResult?> = _messageResult.asStateFlow()
    private val _emptyQuestionResult = MutableStateFlow<LocalizedResult?>(null)
    val emptyQuestionResult: StateFlow<LocalizedResult?> = _emptyQuestionResult.asStateFlow()
    private val _saveSuccess = MutableSharedFlow<Unit>()
    val saveSuccess = _saveSuccess.asSharedFlow()
    private val _editableQuestion = MutableStateFlow<Question?>(null)
    val editableQuestion: StateFlow<Question?> = _editableQuestion.asStateFlow()

    val totalCount: Int get() = _sessionState.value.totalCount
    val answeredCount: Int get() = _sessionState.value.answeredCount
    val correctCount: Int get() {
        val s = _sessionState.value
        return s.questionsWithState.count { it.isCorrect == true }
    }
    val wrongCount: Int get() = answeredCount - correctCount
    val unansweredCount: Int get() = totalCount - answeredCount

    var progressId: String = "exam_default"; private set
    val currentProgressId: String get() = progressId
    var progressSeed: Long = System.currentTimeMillis(); private set
    var fullAnswerRequireCorrect: Boolean = false; private set
    var quizIdInternal: String = ""
    private var notesLoaded: Boolean = false
    private var analysisLoaded: Boolean = false
    private var sparkAnalysisLoaded: Boolean = false
    private var baiduAnalysisLoaded: Boolean = false
    var randomExamEnabled: Boolean = false; private set
    var memoryModeEnabled: Boolean = false; private set
    var memoryModeBatchSize: Int = 10; private set
    var memoryWrongMode: Int = ExamMemoryModeEngine.MEMORY_WRONG_MODE_RETRY_WRONG_BLANKS; private set
    var memoryPoolMode: Int = ExamMemoryModeEngine.MEMORY_POOL_MODE_IN_OUT; private set
    var memoryModeActive: Boolean = false; private set
    var currentMemoryRoundQuestionIds: Set<Int> = emptySet(); private set
    var allSourceQuestions: List<Question> = emptyList(); private set
    val persistentQuestionStateMap = mutableMapOf<Int, UnifiedQuestionState>()
    val editedQuestionSnapshotMap = mutableMapOf<Int, Question>()

    private var activeFillConfig: ExamFillConfig = ExamFillConfig.default
    private val _reviewModeActive = MutableStateFlow(false)
    val reviewModeActive: StateFlow<Boolean> = _reviewModeActive.asStateFlow()
    private var reviewBrowseSession: ReviewBrowseSession? = null
    private var reviewAnsweredSwipeOrder: List<Int> = emptyList()

    private val navigationSaveScheduler = NavigationSaveScheduler(viewModelScope)

    private val navigationCoordinator = ExamNavigationCoordinator(
        sessionState = _sessionState,
        scope = viewModelScope,
        navHelper = navHelper,
        answerRules = answerRules,
        fullAnswerModeActive = { activeFillConfig.isFullAnswerMode },
        fullAnswerRequireCorrect = { fullAnswerRequireCorrect },
        randomExamEnabled = { randomExamEnabled },
        memoryModeActive = { memoryModeActive },
        effectiveCurrentMemoryRoundQuestionIds = ::effectiveCurrentMemoryRoundQuestionIdsB,
        buildExamQuestionState = ::buildExamQuestionState,
        advanceMemoryRoundIfNeeded = ::advanceMemoryRoundIfNeeded,
        reopenQuestionForFullAnswerRetry = ::reopenQuestionForFullAnswerRetry,
        scheduleNavigationSave = ::scheduleNavigationSave,
        saveProgressInternal = ::saveProgressInternal
    )
    private val answerCoordinator = ExamAnswerCoordinator(
        sessionState = _sessionState,
        scope = viewModelScope,
        memoryModeActive = { memoryModeActive },
        randomExamEnabled = { randomExamEnabled },
        currentFullAnswerCandidateIndices = ::currentFullAnswerCandidateIndicesB,
        refreshMemoryRoundPoolIfNeeded = ::refreshMemoryRoundPoolIfNeeded,
        navigateToRandomUnansweredOrAdvanceRound = ::navigateToRandomUnansweredOrAdvanceRound,
        calculateCumulativeStats = ::calculateCumulativeStats,
        saveProgress = ::saveProgress,
        saveProgressInternal = ::saveProgressInternal
    )
    private val memoryModeCoordinator = ExamMemoryModeCoordinator(
        sessionState = _sessionState,
        facade = facade,
        memoryModeEngine = memoryModeEngine,
        answerRules = answerRules,
        persistentQuestionStateMap = persistentQuestionStateMap,
        memoryModeActive = { memoryModeActive },
        memoryModeBatchSize = { memoryModeBatchSize },
        memoryWrongMode = { memoryWrongMode },
        memoryPoolMode = { memoryPoolMode },
        randomExamEnabled = { randomExamEnabled },
        allSourceQuestions = { allSourceQuestions },
        currentMemoryRoundQuestionIds = { currentMemoryRoundQuestionIds },
        setCurrentMemoryRoundQuestionIds = { currentMemoryRoundQuestionIds = it },
        applyConfiguredFillQuestions = ::applyConfiguredFillQuestions,
        mergeCurrentStateToPersistentMap = ::mergeCurrentStateToPersistentMap,
        saveProgressInternal = ::saveProgressInternal
    )
    private val editCoordinator = ExamQuestionEditCoordinator(
        sessionState = _sessionState,
        editableQuestion = _editableQuestion,
        messageResult = _messageResult,
        saveSuccess = _saveSuccess,
        scope = viewModelScope,
        facade = facade,
        fillTransform = fillTransform,
        navHelper = navHelper,
        editedQuestionSnapshotMap = editedQuestionSnapshotMap,
        allSourceQuestions = { allSourceQuestions },
        setAllSourceQuestions = { allSourceQuestions = it },
        progressSeed = { progressSeed },
        saveProgressInternal = ::saveProgressInternal
    )
    private val statisticsCoordinator = ExamStatisticsCoordinator(
        sessionState = _sessionState,
        cumulativeCorrect = _cumulativeCorrect,
        cumulativeAnswered = _cumulativeAnswered,
        cumulativeExamCount = _cumulativeExamCount,
        scope = viewModelScope,
        fontSettingsRepository = fontSettingsRepository
    )
    private val gradeCoordinator = ExamGradeCoordinator(
        sessionState = _sessionState,
        messageResult = _messageResult,
        facade = facade,
        progressSeed = { progressSeed },
        quizIdInternal = { quizIdInternal },
        memoryModeActive = { memoryModeActive },
        advanceMemoryRoundIfNeeded = ::advanceMemoryRoundIfNeeded,
        incrementExamCount = ::incrementExamCount,
        saveProgressInternal = ::saveProgressInternal
    )
    private val artifactCoordinator = ExamArtifactStateCoordinator(
        scope = viewModelScope,
        facade = facade,
        sessionEngine = sessionEngine,
        sessionState = _sessionState,
        messageResult = _messageResult,
        saveProgress = ::saveProgress
    )
    private val progressResetCoordinator = ExamProgressResetCoordinator(
        scope = viewModelScope,
        sessionEngine = sessionEngine,
        sessionState = _sessionState,
        progressId = { progressId },
        setProgressSeed = { progressSeed = it },
        resetArtifactLoadedFlags = ::resetArtifactLoadedFlags,
        loadProgress = ::loadProgress
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _cumulativeExamCount.value = fontSettingsRepository.cumulativeExamCount.firstOrNull() ?: 0
            } catch (_: Exception) { _cumulativeExamCount.value = 0 }
        }
        loadDelegate.init(
            vmScope = viewModelScope,
            progressIdRef = { progressId }, setProgressId = { progressId = it },
            progressSeedRef = { progressSeed }, setProgressSeed = { progressSeed = it },
            setFullAnswerRequireCorrect = { fullAnswerRequireCorrect = it },
            onFillConfigApplied = { activeFillConfig = it },
            memoryModeActiveRef = { memoryModeActive }, setMemoryModeActive = { memoryModeActive = it },
            memoryModeEnabledRef = { memoryModeEnabled }, memoryModeBatchSizeRef = { memoryModeBatchSize },
            memoryWrongModeRef = { memoryWrongMode }, memoryPoolModeRef = { memoryPoolMode },
            allSourceQuestionsRef = { allSourceQuestions }, setAllSourceQuestions = { allSourceQuestions = it },
            setCurrentMemoryRoundQuestionIds = { currentMemoryRoundQuestionIds = it },
            persistentQuestionStateMap = persistentQuestionStateMap,
            onQuestions = { qs -> _sessionState.update { it.copy(questionsWithState = qs.map { q -> QuestionWithState(question = q) }) } },
            onProgressLoaded = { loaded -> _sessionState.update { it.copy(progressLoaded = loaded) } },
            onPostArtifacts = { arts ->
                _sessionState.update { s ->
                    val upd = s.questionsWithState.mapIndexed { i, qws ->
                        qws.copy(analysis = arts.analysis.getOrElse(i) { qws.analysis },
                            sparkAnalysis = arts.sparkAnalysis.getOrElse(i) { qws.sparkAnalysis },
                            baiduAnalysis = arts.baiduAnalysis.getOrElse(i) { qws.baiduAnalysis },
                            note = arts.notes.getOrElse(i) { qws.note })
                    }
                    s.copy(questionsWithState = upd)
                }
            },
            onInitMemoryMode = { seed -> initializeMemoryModeIfNeeded(seed) },
            onLoadProgress = { loadProgress() }
        )
    }

    // ================================================================
    // State builders
    // ================================================================

    private fun buildExamQuestionState(idx: Int): UnifiedQuestionState {
        val qws = _sessionState.value.questionsWithState.getOrNull(idx)
            ?: return UnifiedQuestionState(questionId = -1)
        return UnifiedQuestionState(
            questionId = qws.question.id,
            selectedOptions = qws.selectedOptions,
            textAnswer = qws.textAnswer,
            showResult = qws.showResult,
            analysis = qws.analysis,
            sparkAnalysis = qws.sparkAnalysis,
            baiduAnalysis = qws.baiduAnalysis,
            note = qws.note,
            answerTime = qws.sessionAnswerTime
        )
    }

    fun buildAnswerCardDisplayInfo(qs: List<Question>) =
        navHelper.buildAnswerCardDisplayInfo(qs, allSourceQuestions, isFullAnswerMode)

    fun answerCardEntryGrouped(qs: List<Question>): Boolean =
        AnswerCardDisplayInfoPipeline.useEntryGroupedLayout(buildAnswerCardDisplayInfo(qs), isFullAnswerMode)

    fun isMemoryModeActiveB(): Boolean = memoryModeActive

    private suspend fun applyConfiguredFillQuestions(qs: List<Question>) =
        fillTransform.applyConfiguredFillQuestions(
            qs,
            progressSeed,
            { config ->
                activeFillConfig = config
                fullAnswerRequireCorrect = config.fullAnswerRequireCorrect
            }
        ) { _emptyQuestionResult.value = it }

    fun reloadForFillConfig() {
        _sessionState.update { it.copy(progressLoaded = false) }
        loadDelegate.reloadForFillConfig()
    }

    // ================================================================
    // Mode config
    // ================================================================

    fun setRandomExam(enabled: Boolean) { randomExamEnabled = enabled }

    fun resetLoadState() {
        _sessionState.update { it.copy(progressLoaded = false) }
    }
    fun setMemoryModeConfig(enabled: Boolean, batchSize: Int, wrongMode: Int, poolMode: Int) {
        memoryModeEnabled = enabled
        if (!enabled) memoryModeActive = false
        memoryModeBatchSize = batchSize.coerceIn(1, 100)
        memoryWrongMode = wrongMode
        memoryPoolMode = poolMode
    }

    // ================================================================
    // Memory mode
    // ================================================================

    private fun unansweredCountB(): Int = _sessionState.value.unansweredCount

    private fun buildMemoryRoundPlan(src: List<Question>, seed: Long) =
        memoryModeCoordinator.buildMemoryRoundPlan(src, seed)

    private suspend fun initializeMemoryModeIfNeeded(seed: Long): Boolean =
        memoryModeCoordinator.initializeMemoryModeIfNeeded(seed)

    suspend fun refreshMemoryRoundPoolIfNeeded(answeredIndex: Int): Boolean {
        return memoryModeCoordinator.refreshMemoryRoundPoolIfNeeded(answeredIndex)
    }

    suspend fun advanceMemoryRoundIfNeeded(): Boolean {
        return memoryModeCoordinator.advanceMemoryRoundIfNeeded()
    }

    private fun effectiveCurrentMemoryRoundQuestionIdsB() =
        memoryModeCoordinator.effectiveCurrentMemoryRoundQuestionIds()

    // ================================================================
    // Navigation
    // ================================================================

    private fun currentFullAnswerCandidateIndicesB(candidates: List<Int>): List<Int> =
        navigationCoordinator.currentFullAnswerCandidateIndices(candidates)

    private fun navigateCandidateIndicesB(): List<Int> =
        navigationCoordinator.navigateCandidateIndices()

    private suspend fun navigateToRandomUnansweredOrAdvanceRound() =
        navigationCoordinator.navigateToRandomUnansweredOrAdvanceRound()

    fun nextQuestion() {
        if (tryNavigateReviewBrowse(1)) return
        navigationCoordinator.nextQuestion()
    }

    fun prevQuestion() {
        if (tryNavigateReviewBrowse(-1)) return
        navigationCoordinator.prevQuestion()
    }

    fun hasPendingQuestions(): Boolean = navigationCoordinator.hasPendingQuestions()

    fun canNavigateToNextUnanswered(): Boolean =
        navigationCoordinator.canNavigateToNextUnanswered() ||
            (isFullAnswerMode && canSkipToAdjacentSource(forward = true))

    fun canNavigateToPrevUnanswered(): Boolean =
        navigationCoordinator.canNavigateToPrevUnanswered() ||
            (isFullAnswerMode && canSkipToAdjacentSource(forward = false))

    val isFullAnswerMode: Boolean get() = activeFillConfig.isFullAnswerMode

    fun canSkipToAdjacentSource(forward: Boolean): Boolean =
        navigationCoordinator.canSkipToAdjacentSource(forward)

    fun skipToAdjacentSource(forward: Boolean) =
        navigationCoordinator.skipToAdjacentSource(forward)

    // ================================================================
    // Load entry points
    // ================================================================

    fun loadQuestions(quizId: String, count: Int, random: Boolean) {
        quizIdInternal = quizId
        loadDelegate.loadNormalExam(quizId, count, random)
    }

    fun loadWrongQuestions(fileName: String, count: Int, random: Boolean) {
        quizIdInternal = fileName
        loadDelegate.loadWrongExam(fileName, count, random)
    }

    fun loadFavoriteQuestions(fileName: String, count: Int, random: Boolean) {
        quizIdInternal = fileName
        loadDelegate.loadFavoriteExam(fileName, count, random)
    }

    // ================================================================
    // Answer interaction
    // ================================================================

    fun selectOption(option: Int, skipAfterChanged: Boolean = false) = answerCoordinator.selectOption(option, skipAfterChanged)

    fun updateTextAnswer(answer: String) = answerCoordinator.updateTextAnswer(answer)

    // ================================================================
    // Edit question
    // ================================================================

    fun prepareEditableQuestion(index: Int) = editCoordinator.prepareEditableQuestion(index)

    fun clearEditableQuestion() = editCoordinator.clearEditableQuestion()

    fun normalizeEditedSelectedOptions(sel: List<Int>, q: Question) =
        editCoordinator.normalizeEditedSelectedOptions(sel, q)

    fun saveEditedQuestion(index: Int, newContent: String, newAnswer: String, newOptions: List<String>) =
        editCoordinator.saveEditedQuestion(index, newContent, newAnswer, newOptions)

    // ================================================================
    // Progress persistence
    // ================================================================

    private fun mergeCurrentStateToPersistentMap() {
        val s = _sessionState.value
        persistentQuestionStateMap.putAll(navHelper.buildCurrentStateMapByQuestionId(
            s.questions, s.questionsWithState.map { it.selectedOptions }, s.questionsWithState.map { it.textAnswer },
            s.questionsWithState.map { it.showResult }, s.questionsWithState.map { it.analysis },
            s.questionsWithState.map { it.sparkAnalysis }, s.questionsWithState.map { it.baiduAnalysis },
            s.questionsWithState.map { it.note }
        ))
    }

    private suspend fun saveProgressInternal() = withContext(Dispatchers.Default) {
        mergeCurrentStateToPersistentMap()
        val fs = fillTransform.currentFillConfigSignature()
        val s = _sessionState.value
        val fo = s.questionsWithState.map { it.question.id }
        val ffo = if (memoryModeActive && allSourceQuestions.isNotEmpty()) {
            allSourceQuestions.map { it.id }
        } else {
            fo
        }
        val fsm = persistentQuestionStateMap.toMap()
        val state = UnifiedSessionState(
            questionsWithState = s.questionsWithState,
            currentIndex = s.currentIndex,
            sessionStartTime = s.sessionStartTime,
            mode = SessionMode.EXAM,
            progressId = progressId,
            questionsSource = "",
            isRandomMode = randomExamEnabled,
            progressLoaded = s.progressLoaded,
            finished = s.finished,
            sessionId = fillTransform.buildSessionIdWithFillSignature(progressId, progressSeed, fs)
        )
        ExamPipelineLog.save(s.questionsWithState.map { it.sessionAnswerTime })
        sessionEngine.progressManager.saveProgress(
            progressId = progressId,
            state = state,
            memoryActive = memoryModeActive,
            allSourceQuestions = allSourceQuestions,
            fillSignature = fs,
            extras = mapOf("questionStateMap" to fsm, "fixedQuestionOrder" to ffo)
        )
        ExamRoundLoadLog.save(mapSize = fsm.size, finished = s.finished, fixedOrderSize = ffo.size)
        _messageResult.value = LocalizedResult(com.example.testapp.domain.IOConstants.SAVE_SUCCESS)
    }

    private fun saveProgress() {
        navigationSaveScheduler.flushAndSave { saveProgressInternal() }
    }

    private fun scheduleNavigationSave() {
        navigationSaveScheduler.schedule { saveProgressInternal() }
    }

    // ================================================================
    // Load progress
    // ================================================================

    private fun loadProgress() {
        viewModelScope.launch {
            facade.progress.getFlow(progressId).collect { progress ->
                val s = _sessionState.value
                if (progress != null && !s.progressLoaded) {
                    val size = s.questionsWithState.size; if (size == 0) return@collect
                    progressSeed = progress.timestamp
                    val nci = progress.currentIndex.coerceAtMost(size - 1)
                    val sci = if (randomExamEnabled && !progress.finished) {
                        val cand = currentFullAnswerCandidateIndicesB(s.questionsWithState.indices.filter { it != nci })
                        if (cand.isNotEmpty()) cand.random(kotlin.random.Random(progressSeed)) else nci
                    } else nci
                    val restoreFromMap = ExamSessionRestorePipeline.shouldRestoreAnswersFromStateMap(
                        progress = progress,
                        reviewMode = _reviewModeActive.value
                    )
                    ExamRoundLoadLog.restore(
                        restoreFromMap = restoreFromMap,
                        reviewMode = _reviewModeActive.value,
                        progressFinished = progress.finished,
                        mapSize = progress.questionStateMap.size
                    )
                    persistentQuestionStateMap.clear()
                    persistentQuestionStateMap.putAll(progress.questionStateMap)
                    val qws = ExamSessionRestorePipeline.resolveSessionQuestions(
                        sessionQuestions = s.questionsWithState,
                        progress = progress,
                        restoreFromMap = restoreFromMap
                    )
                    ExamPipelineLog.load(qws.map { it.sessionAnswerTime })
                    val resumeIndex = if (randomExamEnabled && s.questionsWithState.isNotEmpty() && !restoreFromMap) {
                        (0 until s.questionsWithState.size).random(kotlin.random.Random(progressSeed))
                    } else if (restoreFromMap) {
                        sci
                    } else {
                        0
                    }
                    _sessionState.value = s.copy(
                        questionsWithState = qws,
                        currentIndex = resumeIndex,
                        finished = false
                    )
                    if (progress.selectedOptions.size < size) saveProgressInternal()
                } else if (progress == null && !s.progressLoaded) {
                    val nci = if (randomExamEnabled && s.questionsWithState.isNotEmpty()) (0 until s.questionsWithState.size).random(kotlin.random.Random(progressSeed)) else 0
                    _sessionState.update { it.copy(currentIndex = nci) }
                    saveProgress()
                }
                _sessionState.update { it.copy(progressLoaded = true) }; calculateCumulativeStats()
                if (!analysisLoaded) { loadAnalysisFromRepository(); analysisLoaded = true }
                if (!sparkAnalysisLoaded) { loadSparkAnalysisFromRepository(); sparkAnalysisLoaded = true }
                if (!baiduAnalysisLoaded) { loadBaiduAnalysisFromRepository(); baiduAnalysisLoaded = true }
                if (!notesLoaded) { loadNotesFromRepository(); notesLoaded = true }
            }
        }
    }

    // ================================================================
    // GoTo + show result
    // ================================================================

    fun goToQuestion(index: Int) = navigationCoordinator.goToQuestion(index)

    fun updateShowResult(index: Int, value: Boolean) {
        _sessionState.update { s -> s.updateAt(index) { qws ->
            if (value && qws.sessionAnswerTime == 0L) qws.copy(showResult = true, sessionAnswerTime = System.currentTimeMillis())
            else qws.copy(showResult = value)
        }}
        saveProgress()
    }

    // ================================================================
    // Analysis update
    // ================================================================

    fun updateAnalysis(index: Int, text: String) =
        artifactCoordinator.updateAnalysis(index, text)

    fun updateSparkAnalysis(index: Int, text: String) =
        artifactCoordinator.updateSparkAnalysis(index, text)

    fun updateBaiduAnalysis(index: Int, text: String) =
        artifactCoordinator.updateBaiduAnalysis(index, text)

    // ================================================================
    // Note CRUD
    // ================================================================

    suspend fun saveNoteAndWait(questionId: Int, index: Int, text: String): Boolean =
        artifactCoordinator.saveNoteAndWait(questionId, index, text)

    fun saveNote(questionId: Int, index: Int, text: String) =
        artifactCoordinator.saveNote(questionId, index, text)

    fun appendNote(questionId: Int, index: Int, text: String) =
        artifactCoordinator.appendNote(questionId, index, text)

    suspend fun appendNoteSuspend(questionId: Int, index: Int, text: String): Boolean =
        artifactCoordinator.appendNoteSuspend(questionId, index, text)

    suspend fun getNote(questionId: Int): String? =
        artifactCoordinator.getNote(questionId)

    // ================================================================
    // Grade exam
    // ================================================================

    fun scheduleGradeExamAfterDispose() = gradeCoordinator.scheduleGradeExamAfterDispose()

    suspend fun gradeExam(): Int = gradeCoordinator.gradeExam()

    fun enterReviewSession(
        targetProgressId: String,
        quizFile: String,
        questionCount: Int,
        random: Boolean,
        wrongBook: Boolean = false,
        favorite: Boolean = false
    ) {
        viewModelScope.launch {
            _reviewModeActive.value = true
            if (progressId == targetProgressId && _sessionState.value.questionsWithState.isNotEmpty()) {
                applyReviewPresentation()
                return@launch
            }
            progressId = targetProgressId
            resetArtifactLoadedFlags()
            _sessionState.update { it.copy(progressLoaded = false) }
            loadDelegate.loadReviewSession(
                targetProgressId, quizFile, questionCount, random, wrongBook, favorite
            )
            _sessionState.first { it.progressLoaded }
            applyReviewPresentation()
        }
    }

    private fun applyReviewPresentation() {
        val state = _sessionState.value
        val presentation = SessionReviewPresentation.prepare(state.questionsWithState)
        reviewBrowseSession = ReviewBrowseSession(presentation.displayOrder)
        reviewAnsweredSwipeOrder = ReviewAnsweredSwipePipeline.buildOrder(presentation.questionsWithState)
        _sessionState.value = state.copy(
            questionsWithState = presentation.questionsWithState,
            currentIndex = reviewBrowseSession!!.currentIndex,
            finished = true,
            progressLoaded = true
        )
    }

    fun canReviewBrowseBack(): Boolean = reviewBrowseSession?.canStepBack() == true

    fun canReviewBrowseForward(): Boolean = reviewBrowseSession?.canStepForward() == true

    private fun tryNavigateReviewBrowse(delta: Int): Boolean {
        val session = reviewBrowseSession ?: return false
        val stepped = session.step(delta) ?: return true
        reviewBrowseSession = stepped
        _sessionState.update { it.copy(currentIndex = stepped.currentIndex) }
        scheduleNavigationSave()
        return true
    }

    fun browseReviewAnsweredOlder(): ExamReviewSwipeOutcome {
        if (reviewBrowseSession == null) return ExamReviewSwipeOutcome.NoHistory
        val (outcome, target) = ExamReviewSwipePipeline.browseOlder(
            reviewAnsweredSwipeOrder,
            _sessionState.value.currentIndex
        )
        if (target != null) {
            _sessionState.update { it.copy(currentIndex = target) }
            scheduleNavigationSave()
        }
        return outcome
    }

    fun browseReviewAnsweredNewer(): ExamReviewSwipeOutcome {
        if (reviewBrowseSession == null) return ExamReviewSwipeOutcome.AtLatest
        val (outcome, target) = ExamReviewSwipePipeline.browseNewer(
            reviewAnsweredSwipeOrder,
            _sessionState.value.currentIndex
        )
        if (target != null) {
            _sessionState.update { it.copy(currentIndex = target) }
            scheduleNavigationSave()
        }
        return outcome
    }

    // ================================================================
    // Analysis loaders
    // ================================================================

    private suspend fun loadNotesFromRepository() =
        artifactCoordinator.loadNotesFromRepository()

    private suspend fun loadAnalysisFromRepository() =
        artifactCoordinator.loadAnalysisFromRepository()

    private suspend fun loadSparkAnalysisFromRepository() =
        artifactCoordinator.loadSparkAnalysisFromRepository()

    private suspend fun loadBaiduAnalysisFromRepository() =
        artifactCoordinator.loadBaiduAnalysisFromRepository()

    // ================================================================
    // Clear / reset
    // ================================================================

    fun clearProgressAndReload() = progressResetCoordinator.clearProgressAndReload()

    fun resetAllStates() = progressResetCoordinator.resetAllStates()

    fun clearProgress() = progressResetCoordinator.clearProgress()

    private fun resetArtifactLoadedFlags() {
        analysisLoaded = false
        sparkAnalysisLoaded = false
        baiduAnalysisLoaded = false
        notesLoaded = false
    }

    fun retryWrongFillBlanks(index: Int) {
        reopenQuestionForFullAnswerRetry(index)
    }

    private fun reopenQuestionForFullAnswerRetry(index: Int) {
        _sessionState.update { s ->
            val updated = ExamFullAnswerReopenPipeline.reopenAt(s.questionsWithState, index) ?: return@update s
            s.copy(questionsWithState = updated, currentIndex = index, finished = false)
        }
        viewModelScope.launch { saveProgressInternal() }
    }

    // ================================================================
    // Statistics
    // ================================================================

    private fun calculateCumulativeStats() = statisticsCoordinator.calculateCumulativeStats()

    private fun incrementExamCount() = statisticsCoordinator.incrementExamCount()
}


