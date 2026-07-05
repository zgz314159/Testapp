package com.example.testapp.presentation.screen.exam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.core.common.FontSettingsRepository
import com.example.testapp.core.common.LocalizedResult
import com.example.testapp.core.session.NavigationSaveScheduler
import com.example.testapp.core.session.SessionEngine
import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState
import com.example.testapp.domain.model.UnifiedQuestionState
import com.example.testapp.domain.model.updateAt
import com.example.testapp.core.session.policy.SessionStrategyFactory
import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.persistence.SessionPersistenceContext
import com.example.testapp.domain.usecase.ExamUseCaseFacade
import com.example.testapp.uicommon.component.AnswerCardDisplayInfoPipeline
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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

    private val _sessionState = MutableStateFlow(PracticeSessionState(finished = false))
    val sessionState: StateFlow<PracticeSessionState> = _sessionState.asStateFlow()

    private val sessionFlows = ExamViewModelSessionFlows.create(_sessionState, viewModelScope)
    val questions = sessionFlows.questions
    val currentIndex = sessionFlows.currentIndex
    val selectedOptions = sessionFlows.selectedOptions
    val textAnswers = sessionFlows.textAnswers
    val showResultList = sessionFlows.showResultList
    val answerTimeList = sessionFlows.answerTimeList
    val analysisList = sessionFlows.analysisList
    val sparkAnalysisList = sessionFlows.sparkAnalysisList
    val baiduAnalysisList = sessionFlows.baiduAnalysisList
    val noteList = sessionFlows.noteList
    val progressLoaded = sessionFlows.progressLoaded
    val finished = sessionFlows.finished

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
    val correctCount: Int get() = _sessionState.value.questionsWithState.count { it.isCorrect == true }
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

    private val navigationSaveScheduler = NavigationSaveScheduler(viewModelScope)

    private lateinit var progressCoordinator: ExamSessionProgressCoordinator
    private lateinit var reviewCoordinator: ExamReviewSessionCoordinator
    private lateinit var navigationCoordinator: ExamNavigationCoordinator
    private lateinit var answerCoordinator: ExamAnswerCoordinator
    private lateinit var memoryModeCoordinator: ExamMemoryModeCoordinator
    private lateinit var editCoordinator: ExamQuestionEditCoordinator
    private lateinit var statisticsCoordinator: ExamStatisticsCoordinator
    private lateinit var gradeCoordinator: ExamGradeCoordinator
    private lateinit var artifactCoordinator: ExamArtifactStateCoordinator
    private lateinit var progressResetCoordinator: ExamProgressResetCoordinator

    init {
        progressCoordinator = ExamSessionProgressCoordinator(
            sessionState = _sessionState,
            scope = viewModelScope,
            sessionEngine = sessionEngine,
            facade = facade,
            fillTransform = fillTransform,
            navHelper = navHelper,
            persistentQuestionStateMap = persistentQuestionStateMap,
            progressId = { progressId },
            progressSeedRef = { progressSeed },
            setProgressSeed = { progressSeed = it },
            randomExamEnabled = { randomExamEnabled },
            memoryModeActive = { memoryModeActive },
            allSourceQuestions = { allSourceQuestions },
            reviewModeActive = { _reviewModeActive.value },
            persistenceConfig = {
                SessionStrategyFactory.persistence(QuestionSessionKind.Exam(progressId))
                    .config(SessionPersistenceContext())
            },
            messageResult = _messageResult,
            currentFullAnswerCandidateIndices = { candidates ->
                navigationCoordinator.currentFullAnswerCandidateIndices(candidates)
            },
            onCalculateCumulativeStats = ::calculateCumulativeStats,
            onLoadAnalysis = { artifactCoordinator.loadAnalysisFromRepository() },
            onLoadSparkAnalysis = { artifactCoordinator.loadSparkAnalysisFromRepository() },
            onLoadBaiduAnalysis = { artifactCoordinator.loadBaiduAnalysisFromRepository() },
            onLoadNotes = { artifactCoordinator.loadNotesFromRepository() },
            isAnalysisLoaded = { analysisLoaded },
            isSparkAnalysisLoaded = { sparkAnalysisLoaded },
            isBaiduAnalysisLoaded = { baiduAnalysisLoaded },
            isNotesLoaded = { notesLoaded },
            markAnalysisLoaded = { analysisLoaded = true },
            markSparkAnalysisLoaded = { sparkAnalysisLoaded = true },
            markBaiduAnalysisLoaded = { baiduAnalysisLoaded = true },
            markNotesLoaded = { notesLoaded = true }
        )
        reviewCoordinator = ExamReviewSessionCoordinator(
            sessionState = _sessionState,
            reviewModeActive = _reviewModeActive,
            scope = viewModelScope,
            progressId = { progressId },
            setProgressId = { progressId = it },
            resetArtifactLoadedFlags = ::resetArtifactLoadedFlags,
            loadReviewSession = { id, quiz, count, random, wrong, fav ->
                loadDelegate.loadReviewSession(id, quiz, count, random, wrong, fav)
            },
            scheduleNavigationSave = ::scheduleNavigationSave
        )
        navigationCoordinator = ExamNavigationCoordinator(
            sessionState = _sessionState,
            scope = viewModelScope,
            navHelper = navHelper,
            answerRules = answerRules,
            fullAnswerModeActive = ::fullAnswerModeActiveNow,
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
        answerCoordinator = ExamAnswerCoordinator(
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
        memoryModeCoordinator = ExamMemoryModeCoordinator(
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
            mergeCurrentStateToPersistentMap = progressCoordinator::mergeCurrentStateToPersistentMap,
            saveProgressInternal = ::saveProgressInternal
        )
        editCoordinator = ExamQuestionEditCoordinator(
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
        statisticsCoordinator = ExamStatisticsCoordinator(
            sessionState = _sessionState,
            cumulativeCorrect = _cumulativeCorrect,
            cumulativeAnswered = _cumulativeAnswered,
            cumulativeExamCount = _cumulativeExamCount,
            scope = viewModelScope,
            fontSettingsRepository = fontSettingsRepository
        )
        gradeCoordinator = ExamGradeCoordinator(
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
        artifactCoordinator = ExamArtifactStateCoordinator(
            scope = viewModelScope,
            facade = facade,
            sessionEngine = sessionEngine,
            sessionState = _sessionState,
            messageResult = _messageResult,
            saveProgress = ::saveProgress
        )
        progressResetCoordinator = ExamProgressResetCoordinator(
            scope = viewModelScope,
            sessionEngine = sessionEngine,
            sessionState = _sessionState,
            progressId = { progressId },
            setProgressSeed = { progressSeed = it },
            resetArtifactLoadedFlags = ::resetArtifactLoadedFlags,
            loadProgress = ::loadProgress
        )
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _cumulativeExamCount.value = fontSettingsRepository.cumulativeExamCount.firstOrNull() ?: 0
            } catch (_: Exception) {
                _cumulativeExamCount.value = 0
            }
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
            onQuestions = { qs ->
                _sessionState.update { it.copy(questionsWithState = qs.map { q -> QuestionWithState(question = q) }) }
            },
            onProgressLoaded = { loaded -> _sessionState.update { it.copy(progressLoaded = loaded) } },
            onPostArtifacts = { arts ->
                _sessionState.update { s ->
                    val upd = s.questionsWithState.mapIndexed { i, qws ->
                        qws.copy(
                            analysis = arts.analysis.getOrElse(i) { qws.analysis },
                            sparkAnalysis = arts.sparkAnalysis.getOrElse(i) { qws.sparkAnalysis },
                            baiduAnalysis = arts.baiduAnalysis.getOrElse(i) { qws.baiduAnalysis },
                            note = arts.notes.getOrElse(i) { qws.note }
                        )
                    }
                    s.copy(questionsWithState = upd)
                }
            },
            onInitMemoryMode = { seed -> initializeMemoryModeIfNeeded(seed) },
            onLoadProgress = { loadProgress() }
        )
    }

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
            qs, progressSeed,
            { config ->
                activeFillConfig = config
                fullAnswerRequireCorrect = config.fullAnswerRequireCorrect
            }
        ) { _emptyQuestionResult.value = it }

    fun reloadForFillConfig() {
        _sessionState.update { it.copy(progressLoaded = false) }
        loadDelegate.reloadForFillConfig()
    }

    fun setRandomExam(enabled: Boolean) { randomExamEnabled = enabled }
    fun resetLoadState() { _sessionState.update { it.copy(progressLoaded = false) } }
    fun setMemoryModeConfig(enabled: Boolean, batchSize: Int, wrongMode: Int, poolMode: Int) {
        memoryModeEnabled = enabled
        if (!enabled) memoryModeActive = false
        memoryModeBatchSize = batchSize.coerceIn(1, 100)
        memoryWrongMode = wrongMode
        memoryPoolMode = poolMode
    }

    private suspend fun initializeMemoryModeIfNeeded(seed: Long): Boolean =
        memoryModeCoordinator.initializeMemoryModeIfNeeded(seed)

    suspend fun refreshMemoryRoundPoolIfNeeded(answeredIndex: Int): Boolean =
        memoryModeCoordinator.refreshMemoryRoundPoolIfNeeded(answeredIndex)

    suspend fun advanceMemoryRoundIfNeeded(): Boolean =
        memoryModeCoordinator.advanceMemoryRoundIfNeeded()

    private fun effectiveCurrentMemoryRoundQuestionIdsB() =
        memoryModeCoordinator.effectiveCurrentMemoryRoundQuestionIds()

    private fun currentFullAnswerCandidateIndicesB(candidates: List<Int>): List<Int> =
        navigationCoordinator.currentFullAnswerCandidateIndices(candidates)

    private suspend fun navigateToRandomUnansweredOrAdvanceRound() =
        navigationCoordinator.navigateToRandomUnansweredOrAdvanceRound()

    fun nextQuestion() {
        if (reviewCoordinator.tryNavigateReviewBrowse(1)) return
        navigationCoordinator.nextQuestion()
    }

    fun prevQuestion() {
        if (reviewCoordinator.tryNavigateReviewBrowse(-1)) return
        navigationCoordinator.prevQuestion()
    }

    fun prevQuestionViaIcon() {
        if (reviewCoordinator.tryNavigateReviewBrowse(-1)) return
        navigationCoordinator.prevQuestionViaIcon()
    }

    fun nextQuestionViaIcon() {
        if (reviewCoordinator.tryNavigateReviewBrowse(1)) return
        navigationCoordinator.nextQuestionViaIcon()
    }

    fun prevQuestionViaIconDoubleClick(): Boolean = navigationCoordinator.prevQuestionViaIconDoubleClick()
    fun nextQuestionViaIconDoubleClick(): Boolean = navigationCoordinator.nextQuestionViaIconDoubleClick()

    fun retryCurrentQuestion(index: Int) {
        _sessionState.update { s ->
            if (index !in s.questionsWithState.indices) return@update s
            val updated = s.questionsWithState.mapIndexed { idx, qws ->
                if (idx == index) ExamQuestionRetryPipeline.reopenCurrent(qws) else qws
            }
            s.copy(questionsWithState = updated, currentIndex = index, finished = false)
        }
        viewModelScope.launch { saveProgressInternal() }
    }

    fun retryWrongFillBlanks(index: Int) {
        _sessionState.update { s ->
            if (index !in s.questionsWithState.indices) return@update s
            val updated = s.questionsWithState.mapIndexed { idx, qws ->
                if (idx == index) ExamQuestionRetryPipeline.reopenWrongBlanks(qws) else qws
            }
            s.copy(questionsWithState = updated, currentIndex = index, finished = false)
        }
        viewModelScope.launch { saveProgressInternal() }
    }

    private fun reopenQuestionForFullAnswerRetry(index: Int) = retryWrongFillBlanks(index)

    fun hasPendingQuestions(): Boolean = navigationCoordinator.hasPendingQuestions()
    private fun fullAnswerModeActiveNow(): Boolean =
        ExamFullAnswerModeActivePipeline.isActive(activeFillConfig, _sessionState.value.questions)
    val isFullAnswerMode: Boolean get() = fullAnswerModeActiveNow()

    fun canNavigateToNextUnanswered(): Boolean =
        navigationCoordinator.canNavigateToNextUnanswered() ||
            (isFullAnswerMode && canSkipToAdjacentSource(forward = true))

    fun canNavigateToPrevUnanswered(): Boolean =
        navigationCoordinator.canNavigateToPrevUnanswered() ||
            (isFullAnswerMode && canSkipToAdjacentSource(forward = false))

    fun canSkipToAdjacentSource(forward: Boolean): Boolean =
        navigationCoordinator.canSkipToAdjacentSource(forward)

    fun skipToAdjacentSource(forward: Boolean) = navigationCoordinator.skipToAdjacentSource(forward)

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

    fun selectOption(option: Int, skipAfterChanged: Boolean = false) =
        answerCoordinator.selectOption(option, skipAfterChanged)

    fun updateTextAnswer(answer: String) = answerCoordinator.updateTextAnswer(answer)

    fun prepareEditableQuestion(index: Int) = editCoordinator.prepareEditableQuestion(index)
    fun clearEditableQuestion() = editCoordinator.clearEditableQuestion()
    fun normalizeEditedSelectedOptions(sel: List<Int>, q: Question) =
        editCoordinator.normalizeEditedSelectedOptions(sel, q)
    fun saveEditedQuestion(index: Int, newContent: String, newAnswer: String, newOptions: List<String>) =
        editCoordinator.saveEditedQuestion(index, newContent, newAnswer, newOptions)

    private suspend fun saveProgressInternal() = progressCoordinator.saveProgressInternal()
    private fun saveProgress() {
        navigationSaveScheduler.flushAndSave { saveProgressInternal() }
    }

    private fun scheduleNavigationSave() {
        navigationSaveScheduler.schedule { saveProgressInternal() }
    }

    private fun loadProgress() = progressCoordinator.loadProgress(onSaveProgress = ::saveProgress)

    fun prevQuestionSequential() = navigationCoordinator.prevQuestionSequential()
    fun nextQuestionSequential() = navigationCoordinator.nextQuestionSequential()
    fun canGoPrevSequential(): Boolean = navigationCoordinator.canGoPrevSequential()
    fun canGoNextSequential(): Boolean = navigationCoordinator.canGoNextSequential()
    fun goToQuestion(index: Int) = navigationCoordinator.goToQuestion(index)

    fun updateShowResult(index: Int, value: Boolean) {
        _sessionState.update { s ->
            s.updateAt(index) { qws ->
                if (value && qws.sessionAnswerTime == 0L) {
                    qws.copy(showResult = true, sessionAnswerTime = System.currentTimeMillis())
                } else {
                    qws.copy(showResult = value)
                }
            }
        }
        saveProgress()
    }

    fun updateAnalysis(index: Int, text: String) = artifactCoordinator.updateAnalysis(index, text)
    fun updateSparkAnalysis(index: Int, text: String) = artifactCoordinator.updateSparkAnalysis(index, text)
    fun updateBaiduAnalysis(index: Int, text: String) = artifactCoordinator.updateBaiduAnalysis(index, text)

    suspend fun saveNoteAndWait(questionId: Int, index: Int, text: String): Boolean =
        artifactCoordinator.saveNoteAndWait(questionId, index, text)

    fun saveNote(questionId: Int, index: Int, text: String) =
        artifactCoordinator.saveNote(questionId, index, text)

    fun appendNote(questionId: Int, index: Int, text: String) =
        artifactCoordinator.appendNote(questionId, index, text)

    suspend fun appendNoteSuspend(questionId: Int, index: Int, text: String): Boolean =
        artifactCoordinator.appendNoteSuspend(questionId, index, text)

    suspend fun getNote(questionId: Int): String? = artifactCoordinator.getNote(questionId)

    fun scheduleGradeExamAfterDispose() = gradeCoordinator.scheduleGradeExamAfterDispose()
    suspend fun gradeExam(): Int = gradeCoordinator.gradeExam()

    fun enterReviewSession(
        targetProgressId: String,
        quizFile: String,
        questionCount: Int,
        random: Boolean,
        wrongBook: Boolean = false,
        favorite: Boolean = false
    ) = reviewCoordinator.enterReviewSession(
        targetProgressId, quizFile, questionCount, random, wrongBook, favorite
    )

    fun canReviewBrowseBack(): Boolean = reviewCoordinator.canReviewBrowseBack()
    fun canReviewBrowseForward(): Boolean = reviewCoordinator.canReviewBrowseForward()
    fun browseReviewAnsweredOlder(): ExamReviewSwipeOutcome = reviewCoordinator.browseReviewAnsweredOlder()
    fun browseReviewAnsweredNewer(): ExamReviewSwipeOutcome = reviewCoordinator.browseReviewAnsweredNewer()

    fun clearProgressAndReload() = progressResetCoordinator.clearProgressAndReload()
    fun resetAllStates() = progressResetCoordinator.resetAllStates()
    fun clearProgress() = progressResetCoordinator.clearProgress()

    private fun resetArtifactLoadedFlags() {
        analysisLoaded = false
        sparkAnalysisLoaded = false
        baiduAnalysisLoaded = false
        notesLoaded = false
    }

    private fun calculateCumulativeStats() = statisticsCoordinator.calculateCumulativeStats()
    private fun incrementExamCount() = statisticsCoordinator.incrementExamCount()
}
