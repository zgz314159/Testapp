package com.example.testapp.presentation.session.practice

import com.example.testapp.core.common.FontSettingsRepository
import com.example.testapp.core.util.FillQuestionGenerationMode
import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.presentation.screen.practice.*
import com.example.testapp.uicommon.component.AnswerCardDisplayInfo
import com.example.testapp.uicommon.component.AnswerCardDisplayInfoPipeline
import com.example.testapp.uicommon.model.QuestionUiModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PracticeSessionEngine(
    private val scope: CoroutineScope,
    private val deps: PracticeSessionDeps,
) : PracticeScreenBindings {
    override val fontSettingsRepository: FontSettingsRepository get() = deps.fontSettings
    private val _sessionState = MutableStateFlow(PracticeSessionState())
    override val sessionState: StateFlow<PracticeSessionState> = _sessionState.asStateFlow()
    private val runtime = PracticeSessionRuntimeState()
    private val answerHandler = PracticeAnswerHandler()
    private val stateUpdater = PracticeStateUpdater(_sessionState, ::saveProgress)
    private val repositoryContentLoader =
        PracticeRepositoryContentLoader(
            sessionEngine = deps.sessionEngine,
            sessionState = _sessionState,
            saveProgress = ::saveProgress,
        )
    private val sessionFlows = PracticeSessionFlows.create(_sessionState, scope)
    override val questions = sessionFlows.questions
    override val uiQuestions: StateFlow<List<QuestionUiModel>> = sessionFlows.uiQuestions
    override val currentIndex = sessionFlows.currentIndex
    override val sessionAnsweredCountFlow = sessionFlows.sessionAnsweredCountFlow
    override val sessionCorrectCountFlow = sessionFlows.sessionCorrectCountFlow
    override val hasAnyInputInSessionFlow = sessionFlows.hasAnyInputInSessionFlow
    override val sessionInputCountFlow = sessionFlows.sessionInputCountFlow
    override val currentQuestionUi: StateFlow<PracticeCurrentQuestionUi?> = sessionFlows.currentQuestionUi
    override val answeredList = sessionFlows.answeredList
    override val selectedOptions = sessionFlows.selectedOptions
    override val progressLoaded = sessionFlows.progressLoaded
    override val showResultList = sessionFlows.showResultList
    override val analysisList = sessionFlows.analysisList
    override val sparkAnalysisList = sessionFlows.sparkAnalysisList
    override val baiduAnalysisList = sessionFlows.baiduAnalysisList
    override val noteList = sessionFlows.noteList
    override val textAnswers = sessionFlows.textAnswers
    override val totalCount: Int get() = _sessionState.value.totalCount
    override val answeredCount: Int get() = _sessionState.value.answeredCount
    override val correctCount: Int get() = _sessionState.value.correctCount
    override val wrongCount: Int get() = _sessionState.value.wrongCount
    override val unansweredCount: Int get() = _sessionState.value.unansweredCount
    private var progressId: String
        get() = runtime.progressId
        private set(value) {
            runtime.progressId = value
        }
    override val currentProgressId: String get() = runtime.progressId
    private var questionSourceId: String
        get() = runtime.questionSourceId
        private set(value) {
            runtime.questionSourceId = value
        }
    private var randomPracticeEnabled: Boolean
        get() = runtime.randomPracticeEnabled
        private set(value) {
            runtime.randomPracticeEnabled = value
        }
    private var activeFillConfig: PracticeFillConfig
        get() = runtime.activeFillConfig
        private set(value) {
            runtime.activeFillConfig = value
        }
    private val _reviewModeActive = MutableStateFlow(false)
    val reviewModeActive: StateFlow<Boolean> = _reviewModeActive.asStateFlow()
    private val _reviewReady = MutableStateFlow(false)
    override val reviewReady: StateFlow<Boolean> = _reviewReady.asStateFlow()

    private lateinit var strategyCoordinator: PracticeSessionStrategyCoordinator
    private lateinit var progressLifecycle: PracticeProgressLifecycleCoordinator
    private lateinit var reviewCoordinator: PracticeReviewSessionCoordinator
    private lateinit var navigationCoordinator: PracticeNavigationCoordinator
    private lateinit var navigationDelegate: PracticeSessionNavigationDelegate
    private lateinit var gradeDelegate: PracticeSessionGradeDelegate
    private lateinit var questionContentDelegate: PracticeSessionQuestionContentDelegate
    private lateinit var specialQuestionLoader: PracticeSpecialQuestionLoader
    private lateinit var questionEditCoordinator: PracticeQuestionEditCoordinator
    private lateinit var noteCoordinator: PracticeNoteCoordinator

    init {
        val hub =
            PracticeSessionCoordinatorAssembly.assemble(
                PracticeSessionWireContext(
                    scope = scope,
                    deps = deps,
                    sessionState = _sessionState,
                    runtime = runtime,
                    reviewModeActive = _reviewModeActive,
                    reviewReady = _reviewReady,
                    stateUpdater = stateUpdater,
                    repositoryContentLoader = repositoryContentLoader,
                    answerHandler = answerHandler,
                    saveProgress = ::saveProgress,
                    scheduleNavigationSave = ::scheduleNavigationSave,
                    rememberAnsweredHistorySnapshot = ::rememberAnsweredHistorySnapshot,
                    setRandomPracticeOnNav = { navigationCoordinator.randomPracticeEnabled = it },
                ),
            )
        strategyCoordinator = hub.strategyCoordinator
        progressLifecycle = hub.progressLifecycle
        reviewCoordinator = hub.reviewCoordinator
        navigationCoordinator = hub.navigationCoordinator
        navigationDelegate = hub.navigationDelegate
        gradeDelegate = hub.gradeDelegate
        questionContentDelegate = hub.questionContentDelegate
        specialQuestionLoader = hub.specialQuestionLoader
        questionEditCoordinator = hub.questionEditCoordinator
        noteCoordinator = hub.noteCoordinator

        PracticeSessionNavigationWiring.attach(
            navigationCoordinator = navigationCoordinator,
            sessionState = _sessionState,
            scope = scope,
            answerHandler = answerHandler,
            activeFillConfig = { activeFillConfig },
            reopenQuestionForPendingRetry = ::reopenQuestionForPendingRetry,
            reopenQuestionForFullAnswerRetry = ::reopenQuestionForFullAnswerRetry,
            scheduleNavigationSave = ::scheduleNavigationSave,
            randomPracticeEnabled = randomPracticeEnabled,
        )
        scope.launch {
            deps.facade.progress.clear("practice_default")
        }
        scope.launch {
            var prevIndex = _sessionState.value.currentIndex
            _sessionState.collect { state ->
                val cur = state.currentIndex
                if (cur != prevIndex) {
                    prevIndex = cur
                }
            }
        }
    }

    fun bindStrategy(kind: QuestionSessionKind) = strategyCoordinator.bindStrategy(kind)

    override fun bindSessionStrategy(kind: QuestionSessionKind) = bindStrategy(kind)

    override fun persistenceConfig() = strategyCoordinator.persistenceConfig()

    override fun navigationConfig() = strategyCoordinator.navigationConfig()

    override fun exitConfig() = strategyCoordinator.exitConfig()

    override fun revealConfig() = strategyCoordinator.revealConfig()

    override fun sessionStrategyConfig() = strategyCoordinator.sessionStrategyConfig()

    override fun reloadForFillConfig(
        questionCount: Int?,
        initKey: String?,
    ) {
        progressLifecycle.reloadForFillConfig(
            questionCount ?: progressLifecycle.lastAppliedQuestionCount(),
            initKey,
        )
    }

    override fun shouldReloadForQuizInit(initKey: String): Boolean = progressLifecycle.shouldReloadForQuizInit(initKey)

    override fun enterReviewSession(targetProgressId: String) {
        strategyCoordinator.capturePreReviewIfNeeded()
        strategyCoordinator.bindReviewStrategy(targetProgressId)
        reviewCoordinator.enterReviewSession(targetProgressId)
    }

    override fun leaveReviewSession() {
        val snapshot = strategyCoordinator.restorePreReviewOrNull() ?: return
        _reviewModeActive.value = false
        _reviewReady.value = false
        strategyCoordinator.bindStrategy(snapshot.kind)
    }

    override fun canReviewBrowseBack(): Boolean = reviewCoordinator.canReviewBrowseBack()

    override fun canReviewBrowseForward(): Boolean = reviewCoordinator.canReviewBrowseForward()

    override fun setRandomPractice(enabled: Boolean) {
        randomPracticeEnabled = enabled
        navigationCoordinator.randomPracticeEnabled = enabled
    }

    override fun setProgressId(
        id: String,
        questionsId: String,
        loadQuestions: Boolean,
        questionCount: Int,
        random: Boolean,
        pinnedQuestionId: Int?,
    ) {
        progressLifecycle.setProgressId(
            id = id,
            questionsId = questionsId,
            loadQuestions = loadQuestions,
            questionCount = questionCount,
            random = random,
            pinnedQuestionId = pinnedQuestionId,
        )
    }

    override fun loadPreparedAdaptiveQuestions(
        sourceId: String,
        questions: List<Question>,
    ) = progressLifecycle.loadPreparedAdaptiveQuestions(sourceId, questions)

    override fun goToQuestionById(
        questionId: Int,
        source: String,
    ) {
        val index =
            PracticePinnedQuestionPipeline.indexInSession(
                questions = _sessionState.value.questions,
                questionId = questionId,
            ) ?: return
        navigationDelegate.goToQuestion(index, source)
    }

    override fun answerQuestion(option: Int) {
        stateUpdater.answerQuestion(option)
    }

    override fun toggleOption(option: Int) {
        stateUpdater.toggleOption(option)
    }

    override fun updateTextAnswer(answer: String) {
        stateUpdater.updateTextAnswer(answer)
    }

    override fun nextQuestion() = navigationDelegate.nextQuestion()

    override fun prevQuestionViaIcon(): UnansweredNavResult = navigationDelegate.prevQuestionViaIcon()

    override fun nextQuestionViaIcon(): UnansweredNavResult = navigationDelegate.nextQuestionViaIcon()

    override fun prevQuestionViaIconDoubleClick(): Boolean = navigationDelegate.prevQuestionViaIconDoubleClick()

    override fun nextQuestionViaIconDoubleClick(): Boolean = navigationDelegate.nextQuestionViaIconDoubleClick()

    override fun canNavigateToPrevUnanswered(): Boolean = navigationDelegate.canNavigateToPrevUnanswered()

    override fun canNavigateToNextUnanswered(): Boolean = navigationDelegate.canNavigateToNextUnanswered()

    override fun hasPendingQuestions(): Boolean =
        answerHandler.hasPendingQuestions(
            _sessionState.value.questionsWithState,
            fullAnswerModeActive = activeFillConfig.generationMode == FillQuestionGenerationMode.FULL_ANSWER,
            fullAnswerRequireCorrect = activeFillConfig.fullAnswerRequireCorrect,
        )

    override fun prevQuestion() = navigationDelegate.prevQuestion()

    override fun isInAnsweredHistory(): Boolean = navigationDelegate.isInAnsweredHistory()

    override fun browseAnsweredHistoryOlder() = navigationDelegate.browseAnsweredHistoryOlder()

    override fun browseAnsweredHistoryNewer() = navigationDelegate.browseAnsweredHistoryNewer()

    override fun goToQuestion(
        index: Int,
        source: String,
    ) = navigationDelegate.goToQuestion(index, source)

    override val isFullAnswerMode: Boolean get() = navigationDelegate.isFullAnswerMode

    override fun canSkipToUnansweredSource(forward: Boolean): Boolean =
        navigationDelegate.canSkipToUnansweredSource(forward)

    override fun skipToUnansweredSource(forward: Boolean): SkipUnansweredSourceResult =
        navigationDelegate.skipToUnansweredSource(forward)

    fun canSkipToAdjacentSource(forward: Boolean): Boolean = canSkipToUnansweredSource(forward)

    fun skipToAdjacentSource(forward: Boolean) = skipToUnansweredSource(forward)

    override fun buildAnswerCardDisplayInfo(questions: List<Question>): Map<Int, AnswerCardDisplayInfo> =
        AnswerCardDisplayInfoPipeline.build(
            sessionQuestions = questions,
            sourceCatalog = progressLifecycle.sourceCatalog(),
            fullAnswerMode = isFullAnswerMode,
        )

    override fun answerCardEntryGrouped(questions: List<Question>): Boolean =
        AnswerCardDisplayInfoPipeline.useEntryGroupedLayout(
            buildAnswerCardDisplayInfo(questions),
            isFullAnswerMode,
        )

    override fun saveProgress() = progressLifecycle.saveProgress()

    override fun scheduleNavigationSave() = progressLifecycle.scheduleNavigationSave()

    override fun clearProgress() = progressLifecycle.clearProgress()

    override fun updateShowResult(
        index: Int,
        value: Boolean,
    ) = gradeDelegate.updateShowResult(index, value)

    override fun revealShowResult(index: Int) = gradeDelegate.revealShowResult(index)

    override suspend fun gradeSessionOnSubmit(): PracticeSessionGradeSnapshot = gradeDelegate.gradeSessionOnSubmit()

    override fun retryCurrentQuestion(index: Int) = gradeDelegate.retryCurrentQuestion(index)

    override fun retryWrongBlanks(index: Int) = gradeDelegate.retryWrongBlanks(index)

    override fun updateAnalysis(
        index: Int,
        text: String,
    ) {
        val questionId = _sessionState.value.questionsWithState.getOrNull(index)?.question?.id
        stateUpdater.updateAnalysis(index, text)
        if (questionId == null) {
            return
        }
        scope.launch {
            val existing = deps.facade.analysis.getDeepSeek(questionId).getOrNull()
            val richer = com.example.testapp.data.network.deepseek.DeepSeekAskLoadSeedPipeline
                .resolvePreferStructured(existing, text)
            if (richer.isNotBlank() && richer != existing) {
                deps.facade.analysis.saveDeepSeek(questionId, richer)
            }
        }
    }

    override fun updateSparkAnalysis(
        index: Int,
        text: String,
    ) {
        stateUpdater.updateSparkAnalysis(index, text)
    }

    override fun updateBaiduAnalysis(
        index: Int,
        text: String,
    ) {
        stateUpdater.updateBaiduAnalysis(index, text)
    }

    override fun addHistoryRecord(
        score: Int,
        total: Int,
        unanswered: Int,
    ) = recordPracticeHistory(scope, deps.facade, questionSourceId, score, total, unanswered, persistenceConfig().persistProgress)

    override fun saveNote(
        questionId: Int,
        index: Int,
        text: String,
    ) {
        scope.launch { deps.facade.notes.save(questionId, text) }
        noteCoordinator.saveNoteLocally(index, text)
    }

    override suspend fun saveNoteAndWait(
        questionId: Int,
        index: Int,
        text: String,
    ): Boolean = noteCoordinator.saveNoteAndWait(questionId, index, text)

    override fun appendNote(
        questionId: Int,
        index: Int,
        text: String,
    ) {
        scope.launch {
            try {
                noteCoordinator.appendNote(questionId, index, text)
            } catch (_: Exception) {
            }
        }
    }

    override suspend fun appendNoteSuspend(
        questionId: Int,
        index: Int,
        text: String,
    ): Unit = noteCoordinator.appendNote(questionId, index, text)

    suspend fun getNote(questionId: Int): String? = noteCoordinator.getNote(questionId)

    fun updateQuestionContent(
        index: Int,
        newContent: String,
    ) = questionContentDelegate.updateQuestionContent(index, newContent)

    override fun updateQuestionAllFields(
        index: Int,
        newContent: String,
        newOptions: List<String>,
        newAnswer: String,
        newExplanation: String,
    ) = questionContentDelegate.updateQuestionAllFields(
        index,
        newContent,
        newOptions,
        newAnswer,
        newExplanation,
    )

    override fun clearExplanation(
        index: Int,
        question: Question,
    ) = questionContentDelegate.clearExplanation(index, question)

    override fun loadWrongQuestions(fileName: String) {
        scope.launch { specialQuestionLoader.loadWrongQuestions(fileName) }
    }

    override fun loadFavoriteQuestions(fileName: String) {
        scope.launch { specialQuestionLoader.loadFavoriteQuestions(fileName) }
    }

    override val editableQuestion: StateFlow<Question?>
        get() = questionEditCoordinator.editableQuestion
    override val saveSuccess: StateFlow<Boolean>
        get() = questionEditCoordinator.saveSuccess

    override fun prepareEditableQuestion(questionId: Int) = questionEditCoordinator.prepareEditableQuestion(questionId)

    override fun clearEditableQuestion() = questionEditCoordinator.clearEditableQuestion()

    override suspend fun saveEditedQuestion(edited: Question): Boolean =
        questionEditCoordinator.saveEditedQuestion(edited)

    override fun indexOfQuestionBySourceId(sourceId: Int?): Int =
        questionEditCoordinator.indexOfQuestionBySourceId(sourceId)

    private fun rememberAnsweredHistorySnapshot(index: Int) {
        _sessionState.value.questionsWithState.getOrNull(index)?.let {
            navigationCoordinator.rememberAnsweredHistorySnapshot(it)
        }
    }

    private fun reopenQuestionForPendingRetry(index: Int) {
        PracticeQuestionReopenPipeline.reopenForPendingRetry(
            sessionState = _sessionState,
            index = index,
            onSnapshot = ::rememberAnsweredHistorySnapshot,
            onSaved = ::saveProgress,
        )
    }

    private fun reopenQuestionForFullAnswerRetry(index: Int) {
        PracticeQuestionReopenPipeline.reopenForFullAnswerRetry(
            sessionState = _sessionState,
            index = index,
            onSnapshot = ::rememberAnsweredHistorySnapshot,
            onSaved = ::saveProgress,
        )
    }
}
