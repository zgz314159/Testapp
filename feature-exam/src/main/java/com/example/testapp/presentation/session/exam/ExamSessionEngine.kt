package com.example.testapp.presentation.session.exam

import com.example.testapp.core.common.FontSettingsRepository
import com.example.testapp.core.common.LocalizedResult
import com.example.testapp.core.session.NavigationSaveScheduler
import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.UnifiedQuestionState
import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.persistence.SessionPersistenceContext
import com.example.testapp.presentation.screen.exam.*
import com.example.testapp.presentation.screen.exam.ExamProgressPersistencePipeline
import com.example.testapp.uicommon.component.AnswerCardDisplayInfoPipeline
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ExamSessionEngine(
    private val scope: CoroutineScope,
    private val deps: ExamSessionDeps,
) : ExamScreenBindings {
    override val fontSettingsRepository: FontSettingsRepository get() = deps.fontSettings
    private val _sessionState = MutableStateFlow(PracticeSessionState(finished = false))
    val sessionState: StateFlow<PracticeSessionState> = _sessionState.asStateFlow()

    private val sessionFlows = ExamSessionFlows.create(_sessionState, scope)
    override val questions = sessionFlows.questions
    override val currentIndex = sessionFlows.currentIndex
    override val selectedOptions = sessionFlows.selectedOptions
    override val textAnswers = sessionFlows.textAnswers
    override val showResultList = sessionFlows.showResultList
    override val answerTimeList = sessionFlows.answerTimeList
    override val analysisList = sessionFlows.analysisList
    override val sparkAnalysisList = sessionFlows.sparkAnalysisList
    override val baiduAnalysisList = sessionFlows.baiduAnalysisList
    override val noteList = sessionFlows.noteList
    override val progressLoaded = sessionFlows.progressLoaded
    val finished = sessionFlows.finished

    private val _cumulativeCorrect = MutableStateFlow(0)
    override val cumulativeCorrect: StateFlow<Int> = _cumulativeCorrect.asStateFlow()
    private val _cumulativeAnswered = MutableStateFlow(0)
    override val cumulativeAnswered: StateFlow<Int> = _cumulativeAnswered.asStateFlow()
    private val _cumulativeExamCount = MutableStateFlow(0)
    override val cumulativeExamCount: StateFlow<Int> = _cumulativeExamCount.asStateFlow()
    private val _messageResult = MutableStateFlow<LocalizedResult?>(null)
    val messageResult: StateFlow<LocalizedResult?> = _messageResult.asStateFlow()
    private val _emptyQuestionResult = MutableStateFlow<LocalizedResult?>(null)
    override val emptyQuestionResult: StateFlow<LocalizedResult?> = _emptyQuestionResult.asStateFlow()
    private val _saveSuccess = MutableSharedFlow<Unit>()
    override val saveSuccess = _saveSuccess.asSharedFlow()
    private val _editableQuestion = MutableStateFlow<Question?>(null)
    override val editableQuestion: StateFlow<Question?> = _editableQuestion.asStateFlow()

    val totalCount: Int get() = _sessionState.value.totalCount
    val answeredCount: Int get() = _sessionState.value.answeredCount
    val correctCount: Int get() = _sessionState.value.questionsWithState.count { it.isCorrect == true }
    val wrongCount: Int get() = answeredCount - correctCount
    val unansweredCount: Int get() = totalCount - answeredCount

    private val runtime = ExamSessionRuntimeState()
    var progressId: String
        get() = runtime.progressId
        private set(value) {
            runtime.progressId = value
        }
    override val currentProgressId: String get() = runtime.progressId
    var progressSeed: Long
        get() = runtime.progressSeed
        private set(value) {
            runtime.progressSeed = value
        }
    var fullAnswerRequireCorrect: Boolean
        get() = runtime.fullAnswerRequireCorrect
        private set(value) {
            runtime.fullAnswerRequireCorrect = value
        }
    var quizIdInternal: String
        get() = runtime.quizIdInternal
        set(value) {
            runtime.quizIdInternal = value
        }
    var randomExamEnabled: Boolean
        get() = runtime.randomExamEnabled
        private set(value) {
            runtime.randomExamEnabled = value
        }
    var memoryModeEnabled: Boolean
        get() = runtime.memory.enabled
        private set(value) {
            runtime.memory.enabled = value
        }
    var memoryModeBatchSize: Int
        get() = runtime.memory.batchSize
        private set(value) {
            runtime.memory.batchSize = value
        }
    var memoryWrongMode: Int
        get() = runtime.memory.wrongMode
        private set(value) {
            runtime.memory.wrongMode = value
        }
    var memoryPoolMode: Int
        get() = runtime.memory.poolMode
        private set(value) {
            runtime.memory.poolMode = value
        }
    var memoryModeActive: Boolean
        get() = runtime.memory.active
        private set(value) {
            runtime.memory.active = value
        }
    var currentMemoryRoundQuestionIds: Set<Int>
        get() = runtime.currentMemoryRoundQuestionIds
        private set(value) {
            runtime.currentMemoryRoundQuestionIds = value
        }
    var allSourceQuestions: List<Question>
        get() = runtime.allSourceQuestions
        private set(value) {
            runtime.allSourceQuestions = value
        }
    val persistentQuestionStateMap = mutableMapOf<Int, UnifiedQuestionState>()
    val editedQuestionSnapshotMap = mutableMapOf<Int, Question>()

    private var activeFillConfig: ExamFillConfig
        get() = runtime.activeFillConfig
        set(value) {
            runtime.activeFillConfig = value
        }
    private val strategyCoordinator =
        ExamSessionStrategyCoordinator(
            progressId = { runtime.progressId },
            onStrategyApplied = {},
        )
    private val _reviewModeActive = MutableStateFlow(false)
    val reviewModeActive: StateFlow<Boolean> = _reviewModeActive.asStateFlow()

    private val navigationSaveScheduler = NavigationSaveScheduler(scope)

    private lateinit var progressCoordinator: ExamSessionProgressCoordinator
    private lateinit var reviewCoordinator: ExamReviewSessionCoordinator
    private lateinit var navigationCoordinator: ExamNavigationCoordinator
    private lateinit var navigationDelegate: ExamSessionNavigationDelegate
    private lateinit var answerCoordinator: ExamAnswerCoordinator
    private lateinit var memoryModeCoordinator: ExamMemoryModeCoordinator
    private lateinit var editCoordinator: ExamQuestionEditCoordinator
    private lateinit var statisticsCoordinator: ExamStatisticsCoordinator
    private lateinit var gradeCoordinator: ExamGradeCoordinator
    private lateinit var artifactCoordinator: ExamArtifactStateCoordinator
    private lateinit var progressResetCoordinator: ExamProgressResetCoordinator

    init {
        val hub =
            ExamSessionCoordinatorAssembly.assemble(
                ExamSessionWireContext(
                    scope = scope,
                    deps = deps,
                    sessionState = _sessionState,
                    strategyCoordinator = strategyCoordinator,
                    runtime = runtime,
                    reviewModeActive = _reviewModeActive,
                    cumulativeCorrect = _cumulativeCorrect,
                    cumulativeAnswered = _cumulativeAnswered,
                    cumulativeExamCount = _cumulativeExamCount,
                    messageResult = _messageResult,
                    saveSuccess = _saveSuccess,
                    editableQuestion = _editableQuestion,
                    persistentQuestionStateMap = persistentQuestionStateMap,
                    editedQuestionSnapshotMap = editedQuestionSnapshotMap,
                    buildExamQuestionState = { index ->
                        ExamQuestionStatePipeline.toUnified(_sessionState.value, index)
                    },
                    fullAnswerModeActiveNow = ::fullAnswerModeActiveNow,
                    initializeMemoryModeIfNeeded = ::initializeMemoryModeIfNeeded,
                    applyConfiguredFillQuestions = ::applyConfiguredFillQuestions,
                    calculateCumulativeStats = ::calculateCumulativeStats,
                    incrementExamCount = ::incrementExamCount,
                    saveProgress = ::saveProgress,
                    saveProgressInternal = ::saveProgressInternal,
                    scheduleNavigationSave = ::scheduleNavigationSave,
                    loadProgress = ::loadProgress,
                    reopenQuestionForFullAnswerRetry = ::reopenQuestionForFullAnswerRetry,
                    refreshMemoryRoundPoolIfNeeded = ::refreshMemoryRoundPoolIfNeeded,
                    advanceMemoryRoundIfNeeded = ::advanceMemoryRoundIfNeeded,
                    effectiveCurrentMemoryRoundQuestionIds = ::effectiveCurrentMemoryRoundQuestionIdsB,
                    currentFullAnswerCandidateIndices = ::currentFullAnswerCandidateIndicesB,
                    navigateToRandomUnansweredOrAdvanceRound = ::navigateToRandomUnansweredOrAdvanceRound,
                ),
            )
        progressCoordinator = hub.progressCoordinator
        reviewCoordinator = hub.reviewCoordinator
        navigationCoordinator = hub.navigationCoordinator
        navigationDelegate = hub.navigationDelegate
        answerCoordinator = hub.answerCoordinator
        memoryModeCoordinator = hub.memoryModeCoordinator
        editCoordinator = hub.editCoordinator
        statisticsCoordinator = hub.statisticsCoordinator
        gradeCoordinator = hub.gradeCoordinator
        artifactCoordinator = hub.artifactCoordinator
        progressResetCoordinator = hub.progressResetCoordinator

        scope.launch(Dispatchers.IO) {
            try {
                _cumulativeExamCount.value = deps.fontSettings.cumulativeExamCount.firstOrNull() ?: 0
            } catch (_: Exception) {
                _cumulativeExamCount.value = 0
            }
        }
        ExamSessionLoadDelegateWiring.attach(
            deps = deps,
            scope = scope,
            sessionState = _sessionState,
            runtime = runtime,
            persistentQuestionStateMap = persistentQuestionStateMap,
            initializeMemoryModeIfNeeded = ::initializeMemoryModeIfNeeded,
            loadProgress = ::loadProgress,
        )
    }

    override fun buildAnswerCardDisplayInfo(qs: List<Question>) =
        deps.navHelper.buildAnswerCardDisplayInfo(qs, allSourceQuestions, isFullAnswerMode)

    override fun answerCardEntryGrouped(qs: List<Question>): Boolean =
        AnswerCardDisplayInfoPipeline.useEntryGroupedLayout(buildAnswerCardDisplayInfo(qs), isFullAnswerMode)

    fun isMemoryModeActiveB(): Boolean = memoryModeActive

    private suspend fun applyConfiguredFillQuestions(qs: List<Question>) =
        deps.fillTransform.applyConfiguredFillQuestions(
            qs,
            progressSeed,
            { config ->
                activeFillConfig = config
                fullAnswerRequireCorrect = config.fullAnswerRequireCorrect
            },
        ) { _emptyQuestionResult.value = it }

    override fun reloadForFillConfig() {
        _sessionState.update { it.copy(progressLoaded = false) }
        deps.loadDelegate.reloadForFillConfig()
    }

    override fun setRandomExam(enabled: Boolean) {
        randomExamEnabled = enabled
    }

    override fun resetLoadState() {
        _sessionState.update { it.copy(progressLoaded = false) }
    }

    override fun setMemoryModeConfig(
        enabled: Boolean,
        batchSize: Int,
        wrongMode: Int,
        poolMode: Int,
    ) {
        memoryModeEnabled = enabled
        if (!enabled) memoryModeActive = false
        memoryModeBatchSize = batchSize.coerceIn(1, 100)
        memoryWrongMode = wrongMode
        memoryPoolMode = poolMode
    }

    private suspend fun initializeMemoryModeIfNeeded(seed: Long): Boolean =
        memoryModeCoordinator.initializeMemoryModeIfNeeded(seed)

    private suspend fun refreshMemoryRoundPoolIfNeeded(answeredIndex: Int): Boolean =
        memoryModeCoordinator.refreshMemoryRoundPoolIfNeeded(answeredIndex)

    private suspend fun advanceMemoryRoundIfNeeded(): Boolean = memoryModeCoordinator.advanceMemoryRoundIfNeeded()

    private fun effectiveCurrentMemoryRoundQuestionIdsB() =
        memoryModeCoordinator.effectiveCurrentMemoryRoundQuestionIds()

    private fun currentFullAnswerCandidateIndicesB(candidates: List<Int>): List<Int> =
        navigationCoordinator.currentFullAnswerCandidateIndices(candidates)

    private suspend fun navigateToRandomUnansweredOrAdvanceRound() =
        navigationCoordinator.navigateToRandomUnansweredOrAdvanceRound()

    fun bindStrategy(
        kind: QuestionSessionKind,
        persistenceContext: SessionPersistenceContext = SessionPersistenceContext(),
    ) = strategyCoordinator.bindStrategy(kind, persistenceContext)

    override fun bindSessionStrategy(kind: QuestionSessionKind) = bindStrategy(kind)

    override fun persistenceConfig() = strategyCoordinator.persistenceConfig()

    override fun navigationConfig() = strategyCoordinator.navigationConfig()

    override fun exitConfig() = strategyCoordinator.exitConfig()

    override fun revealConfig() = strategyCoordinator.revealConfig()

    override fun sessionStrategyConfig() = strategyCoordinator.sessionStrategyConfig()

    override fun nextQuestion() = navigationDelegate.nextQuestion()

    override fun prevQuestion() = navigationDelegate.prevQuestion()

    override fun prevQuestionViaIcon() = navigationDelegate.prevQuestionViaIcon()

    override fun nextQuestionViaIcon() = navigationDelegate.nextQuestionViaIcon()

    override fun prevQuestionViaIconDoubleClick(): Boolean = navigationDelegate.prevQuestionViaIconDoubleClick()

    override fun nextQuestionViaIconDoubleClick(): Boolean = navigationDelegate.nextQuestionViaIconDoubleClick()

    override fun retryCurrentQuestion(index: Int) {
        _sessionState.update { ExamQuestionStatePipeline.retryCurrent(it, index) }
        scope.launch { saveProgressInternal() }
    }

    override fun retryWrongFillBlanks(index: Int) {
        _sessionState.update { ExamQuestionStatePipeline.retryWrongFillBlanks(it, index) }
        scope.launch { saveProgressInternal() }
    }

    private fun reopenQuestionForFullAnswerRetry(index: Int) = retryWrongFillBlanks(index)

    override fun hasPendingQuestions(): Boolean = navigationDelegate.hasPendingQuestions()

    private fun fullAnswerModeActiveNow(): Boolean =
        ExamFullAnswerModeActivePipeline.isActive(activeFillConfig, _sessionState.value.questions)

    override val isFullAnswerMode: Boolean get() = fullAnswerModeActiveNow()

    override fun canNavigateToNextUnanswered(): Boolean = navigationDelegate.canNavigateToNextUnanswered()

    override fun canNavigateToPrevUnanswered(): Boolean = navigationDelegate.canNavigateToPrevUnanswered()

    override fun canSkipToAdjacentSource(forward: Boolean): Boolean =
        navigationDelegate.canSkipToAdjacentSource(forward)

    override fun skipToAdjacentSource(forward: Boolean) = navigationDelegate.skipToAdjacentSource(forward)

    override fun loadQuestions(quizId: String, count: Int, random: Boolean) {
        quizIdInternal = quizId
        deps.loadDelegate.loadNormalExam(quizId, count, random)
    }

    override fun loadWrongQuestions(fileName: String, count: Int, random: Boolean) {
        quizIdInternal = fileName
        deps.loadDelegate.loadWrongExam(fileName, count, random)
    }

    override fun loadFavoriteQuestions(fileName: String, count: Int, random: Boolean) {
        quizIdInternal = fileName
        deps.loadDelegate.loadFavoriteExam(fileName, count, random)
    }

    override fun selectOption(option: Int, skipAfterChanged: Boolean) =
        answerCoordinator.selectOption(option, skipAfterChanged)

    override fun updateTextAnswer(answer: String) = answerCoordinator.updateTextAnswer(answer)

    override fun prepareEditableQuestion(index: Int) = editCoordinator.prepareEditableQuestion(index)

    override fun clearEditableQuestion() = editCoordinator.clearEditableQuestion()

    fun normalizeEditedSelectedOptions(sel: List<Int>, q: Question) =
        editCoordinator.normalizeEditedSelectedOptions(sel, q)

    override fun saveEditedQuestion(
        index: Int,
        newContent: String,
        newAnswer: String,
        newOptions: List<String>,
    ) = editCoordinator.saveEditedQuestion(index, newContent, newAnswer, newOptions)

    private suspend fun saveProgressInternal() {
        if (!ExamProgressPersistencePipeline.shouldPersist(strategyCoordinator.persistenceConfig())) return
        progressCoordinator.saveProgressInternal()
    }

    private fun saveProgress() {
        if (!ExamProgressPersistencePipeline.shouldPersist(strategyCoordinator.persistenceConfig())) return
        navigationSaveScheduler.flushAndSave { saveProgressInternal() }
    }

    private fun scheduleNavigationSave() {
        if (!ExamProgressPersistencePipeline.shouldSaveOnNavigation(strategyCoordinator.persistenceConfig())) return
        navigationSaveScheduler.schedule { saveProgressInternal() }
    }

    private fun loadProgress() = progressCoordinator.loadProgress(onSaveProgress = ::saveProgress)

    override fun prevQuestionSequential() = navigationDelegate.prevQuestionSequential()

    override fun nextQuestionSequential() = navigationDelegate.nextQuestionSequential()

    override fun canGoPrevSequential(): Boolean = navigationDelegate.canGoPrevSequential()

    override fun canGoNextSequential(): Boolean = navigationDelegate.canGoNextSequential()

    override fun goToQuestion(index: Int) = navigationDelegate.goToQuestion(index)

    override fun updateShowResult(
        index: Int,
        value: Boolean,
    ) {
        _sessionState.update { ExamQuestionStatePipeline.updateShowResult(it, index, value) }
        saveProgress()
    }

    override fun updateAnalysis(
        index: Int,
        text: String,
    ) = artifactCoordinator.updateAnalysis(index, text)

    override fun updateSparkAnalysis(
        index: Int,
        text: String,
    ) = artifactCoordinator.updateSparkAnalysis(index, text)

    override fun updateBaiduAnalysis(
        index: Int,
        text: String,
    ) = artifactCoordinator.updateBaiduAnalysis(index, text)

    override suspend fun saveNoteAndWait(
        questionId: Int,
        index: Int,
        text: String,
    ): Boolean = artifactCoordinator.saveNoteAndWait(questionId, index, text)

    override fun saveNote(
        questionId: Int,
        index: Int,
        text: String,
    ) = artifactCoordinator.saveNote(questionId, index, text)

    override fun appendNote(
        questionId: Int,
        index: Int,
        text: String,
    ) = artifactCoordinator.appendNote(questionId, index, text)

    override suspend fun appendNoteSuspend(
        questionId: Int,
        index: Int,
        text: String,
    ): Boolean = artifactCoordinator.appendNoteSuspend(questionId, index, text)

    suspend fun getNote(questionId: Int): String? = artifactCoordinator.getNote(questionId)

    override fun scheduleGradeExamAfterDispose() = gradeCoordinator.scheduleGradeExamAfterDispose()

    override suspend fun gradeExam(): Int = gradeCoordinator.gradeExam()

    override fun enterReviewSession(
        targetProgressId: String,
        quizFile: String,
        questionCount: Int,
        random: Boolean,
        wrongBook: Boolean,
        favorite: Boolean,
    ) {
        strategyCoordinator.capturePreReviewExamKind(quizFile, wrongBook, favorite)
        strategyCoordinator.bindReviewStrategy(targetProgressId, quizFile, wrongBook, favorite)
        reviewCoordinator.enterReviewSession(
            targetProgressId,
            quizFile,
            questionCount,
            random,
            wrongBook,
            favorite,
        )
    }

    override fun leaveReviewSession() {
        val restore = strategyCoordinator.restorePreReviewExamKindOrNull() ?: return
        _reviewModeActive.value = false
        bindStrategy(restore)
    }

    override fun canReviewBrowseBack(): Boolean = reviewCoordinator.canReviewBrowseBack()

    override fun canReviewBrowseForward(): Boolean = reviewCoordinator.canReviewBrowseForward()

    override fun browseReviewAnsweredOlder() = navigationDelegate.browseReviewAnsweredOlder()

    override fun browseReviewAnsweredNewer() = navigationDelegate.browseReviewAnsweredNewer()

    fun clearProgressAndReload() = progressResetCoordinator.clearProgressAndReload()

    fun resetAllStates() = progressResetCoordinator.resetAllStates()

    fun clearProgress() = progressResetCoordinator.clearProgress()

    private fun calculateCumulativeStats() = statisticsCoordinator.calculateCumulativeStats()

    private fun incrementExamCount() = statisticsCoordinator.incrementExamCount()
}
