package com.example.testapp.presentation.screen.practice
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.core.common.FontSettingsRepository
import com.example.testapp.presentation.screen.practice.navigation.AnsweredHistoryBackwardResult
import com.example.testapp.presentation.screen.practice.navigation.AnsweredHistoryForwardResult
import com.example.testapp.presentation.screen.practice.UnansweredNavResult
import com.example.testapp.domain.model.*
import com.example.testapp.domain.usecase.PracticeUseCaseFacade
import com.example.testapp.domain.usecase.QuestionFlowCache
import com.example.testapp.core.session.SessionEngine
import com.example.testapp.core.util.FillQuestionGenerationMode
import com.example.testapp.uicommon.component.AnswerCardDisplayInfo
import com.example.testapp.uicommon.component.AnswerCardDisplayInfoPipeline
import com.example.testapp.uicommon.model.QuestionUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class PracticeViewModel @Inject constructor(
    private val sessionEngine: SessionEngine,
    private val facade: PracticeUseCaseFacade,
    private val questionFlowCache: QuestionFlowCache,
    private val fontSettings: FontSettingsRepository
) : ViewModel() {
    val fontSettingsRepository: FontSettingsRepository get() = fontSettings
    private val _sessionState = MutableStateFlow(PracticeSessionState())
    val sessionState: StateFlow<PracticeSessionState> = _sessionState.asStateFlow()
    private val stateUpdater = PracticeStateUpdater(_sessionState, ::saveProgress)
    private val repositoryContentLoader = PracticeRepositoryContentLoader(
        sessionEngine = sessionEngine,
        sessionState = _sessionState,
        saveProgress = ::saveProgress
    )
    private val answerHandler = PracticeAnswerHandler()
    private val navigationCoordinator = PracticeNavigationCoordinator()
    private val questionEditCoordinator = PracticeQuestionEditCoordinator(facade, _sessionState)
    private val noteCoordinator = PracticeNoteCoordinator(facade, _sessionState)
    private val sessionFlows = PracticeViewModelSessionFlows.create(_sessionState, viewModelScope)
    val questions = sessionFlows.questions
    val uiQuestions: StateFlow<List<QuestionUiModel>> = sessionFlows.uiQuestions
    val currentIndex = sessionFlows.currentIndex
    val sessionAnsweredCountFlow = sessionFlows.sessionAnsweredCountFlow
    val sessionCorrectCountFlow = sessionFlows.sessionCorrectCountFlow
    val hasAnyInputInSessionFlow = sessionFlows.hasAnyInputInSessionFlow
    val sessionInputCountFlow = sessionFlows.sessionInputCountFlow
    val currentQuestionUi = sessionFlows.currentQuestionUi
    val answeredList = sessionFlows.answeredList
    val selectedOptions = sessionFlows.selectedOptions
    val progressLoaded = sessionFlows.progressLoaded
    val showResultList = sessionFlows.showResultList
    val analysisList = sessionFlows.analysisList
    val sparkAnalysisList = sessionFlows.sparkAnalysisList
    val baiduAnalysisList = sessionFlows.baiduAnalysisList
    val noteList = sessionFlows.noteList
    val textAnswers = sessionFlows.textAnswers
    val totalCount: Int get() = _sessionState.value.totalCount
    val answeredCount: Int get() = _sessionState.value.answeredCount
    val correctCount: Int get() = _sessionState.value.correctCount
    val wrongCount: Int get() = _sessionState.value.wrongCount
    val unansweredCount: Int get() = _sessionState.value.unansweredCount
    private var progressId: String = ""
    val currentProgressId: String get() = progressId
    private var questionSourceId: String = ""
    private var randomPracticeEnabled: Boolean = false
    private var activeFillConfig: PracticeFillConfig = PracticeFillConfig.default
    private val _reviewModeActive = MutableStateFlow(false)
    val reviewModeActive: StateFlow<Boolean> = _reviewModeActive.asStateFlow()
    private val _reviewReady = MutableStateFlow(false)
    val reviewReady: StateFlow<Boolean> = _reviewReady.asStateFlow()
    private val progressLifecycle = PracticeProgressLifecycleCoordinator(
        scope = viewModelScope,
        sessionEngine = sessionEngine,
        facade = facade,
        questionFlowCache = questionFlowCache,
        fontSettings = fontSettings,
        sessionState = _sessionState,
        repositoryContentLoader = repositoryContentLoader,
        progressId = { progressId },
        setProgressIdValue = { progressId = it },
        questionSourceId = { questionSourceId },
        setQuestionSourceId = { questionSourceId = it },
        randomPracticeEnabled = { randomPracticeEnabled },
        setRandomPracticeEnabled = { randomPracticeEnabled = it; navigationCoordinator.randomPracticeEnabled = it },
        onFillConfigApplied = { activeFillConfig = it },
        onProgressRestored = { questionsWithState, currentIndex ->
            navigationCoordinator.seedAnsweredHistoryFromRestoredProgress(
                questionsWithState,
                answerHandler::isQuestionAnswered
            )
            navigationCoordinator.seedRandomNavigationHistory(
                questionsWithState,
                currentIndex,
                answerHandler::isQuestionAnswered
            )
        }
    )
    private val reviewCoordinator = PracticeReviewSessionCoordinator(
        sessionState = _sessionState,
        reviewModeActive = _reviewModeActive,
        reviewReady = _reviewReady,
        scope = viewModelScope,
        progressId = { progressId },
        loadReviewSession = progressLifecycle::loadReviewSession,
        scheduleNavigationSave = ::scheduleNavigationSave
    )
    private val specialQuestionLoader = PracticeSpecialQuestionLoader(
        facade = facade,
        fontSettings = fontSettings,
        sessionState = _sessionState,
        progressId = { progressId },
        randomPracticeEnabled = { randomPracticeEnabled },
        onFillConfigApplied = { activeFillConfig = it },
        loadProgress = progressLifecycle::loadProgress
    )
    init {
        navigationCoordinator.randomPracticeEnabled = randomPracticeEnabled
        navigationCoordinator.initPhase4(
            _sessionState = _sessionState,
            scope = viewModelScope,
            isQuestionPendingForCurrentMode = { qws ->
                answerHandler.isQuestionPendingForCurrentMode(
                    qws,
                    fullAnswerModeActive = activeFillConfig.generationMode == FillQuestionGenerationMode.FULL_ANSWER,
                    fullAnswerRequireCorrect = activeFillConfig.fullAnswerRequireCorrect
                )
            },
            isQuestionAnswered = answerHandler::isQuestionAnswered,
            shouldReopenUnansweredReveal = answerHandler::shouldReopenUnansweredReveal,
            currentSourcePendingIndices = { questionsWithState, currentIndex, eligibleIndices ->
                answerHandler.currentSourcePendingIndices(
                    questionsWithState,
                    currentIndex,
                    eligibleIndices,
                    fullAnswerModeActive = activeFillConfig.generationMode == FillQuestionGenerationMode.FULL_ANSWER,
                    fullAnswerRequireCorrect = activeFillConfig.fullAnswerRequireCorrect
                )
            },
            isCurrentSourceComplete = { state ->
                answerHandler.isCurrentSourceComplete(
                    state,
                    fullAnswerModeActive = activeFillConfig.generationMode == FillQuestionGenerationMode.FULL_ANSWER,
                    fullAnswerRequireCorrect = activeFillConfig.fullAnswerRequireCorrect
                )
            },
            findNextSourceEntryIndices = { state ->
                answerHandler.findNextSourceEntryIndices(
                    state,
                    fullAnswerModeActive = activeFillConfig.generationMode == FillQuestionGenerationMode.FULL_ANSWER,
                    fullAnswerRequireCorrect = activeFillConfig.fullAnswerRequireCorrect
                )
            },
            findAdjacentDerivedQuestionIndex = { _, _ -> null },
            effectiveCurrentMemoryRoundQuestionIds = { emptySet() },
            nextFullAnswerCandidateIndices = { questionsWithState, currentIndex, eligibleIndices ->
                answerHandler.nextFullAnswerCandidateIndices(
                    questionsWithState,
                    currentIndex,
                    eligibleIndices,
                    fullAnswerModeActive = activeFillConfig.generationMode == FillQuestionGenerationMode.FULL_ANSWER,
                    fullAnswerRequireCorrect = activeFillConfig.fullAnswerRequireCorrect
                )
            },
            reopenQuestionForPendingRetry = ::reopenQuestionForPendingRetry,
            reopenQuestionForFullAnswerRetry = ::reopenQuestionForFullAnswerRetry,
            scheduleNavigationSave = ::scheduleNavigationSave,
            fullAnswerModeActive = {
                activeFillConfig.generationMode == FillQuestionGenerationMode.FULL_ANSWER
            },
            fullAnswerRequireCorrect = { activeFillConfig.fullAnswerRequireCorrect },
            fullAnswerRandomOrder = { activeFillConfig.fullAnswerRandomOrder },
            memoryModeActive = { false }
        )
        viewModelScope.launch {
            facade.progress.clear("practice_default")
        }
        viewModelScope.launch {
            var prevIndex = _sessionState.value.currentIndex
            _sessionState.collect { state ->
                val cur = state.currentIndex
                if (cur != prevIndex) {
                    PracticeJumpDebugLog.sessionIndexMutation(
                        prevIndex,
                        cur,
                        Exception().stackTraceToString().take(1200)
                    )
                    prevIndex = cur
                }
            }
        }
    }
    fun reloadForFillConfig(questionCount: Int? = null, initKey: String? = null) {
        progressLifecycle.reloadForFillConfig(
            questionCount ?: progressLifecycle.lastAppliedQuestionCount(),
            initKey
        )
    }
    fun shouldReloadForQuizInit(initKey: String): Boolean =
        progressLifecycle.shouldReloadForQuizInit(initKey)
    fun enterReviewSession(targetProgressId: String) =
        reviewCoordinator.enterReviewSession(targetProgressId)
    fun canReviewBrowseBack(): Boolean = reviewCoordinator.canReviewBrowseBack()
    fun canReviewBrowseForward(): Boolean = reviewCoordinator.canReviewBrowseForward()
    fun setRandomPractice(enabled: Boolean) {
        randomPracticeEnabled = enabled
        navigationCoordinator.randomPracticeEnabled = enabled
    }
    fun setProgressId(
        id: String,
        questionsId: String = id,
        loadQuestions: Boolean = true,
        questionCount: Int = 0,
        random: Boolean = randomPracticeEnabled
    ) = progressLifecycle.setProgressId(id, questionsId, loadQuestions, questionCount, random)
    fun answerQuestion(option: Int) {
        stateUpdater.answerQuestion(option)
    }
    fun toggleOption(option: Int) {
        stateUpdater.toggleOption(option)
    }
    fun updateTextAnswer(answer: String) {
        stateUpdater.updateTextAnswer(answer)
    }
    fun nextQuestion() {
        if (reviewCoordinator.tryNavigateReviewBrowse(1)) return
        PracticeJumpDebugLog.vmNextQuestion(_sessionState.value.currentIndex)
        navigationCoordinator.nextQuestion()
    }
    fun prevQuestionViaIcon(): UnansweredNavResult {
        if (reviewCoordinator.tryNavigateReviewBrowse(-1)) return UnansweredNavResult.Navigated
        val idx = _sessionState.value.currentIndex
        val qws = _sessionState.value.questionsWithState.getOrNull(idx)
        PracticeFullAnswerIconNavDebugLog.tapEntry(
            forward = false,
            source = "VM.prevQuestionViaIcon",
            detail = "idx=$idx fullAnswer=$isFullAnswerMode textLen=${qws?.textAnswer?.length ?: 0} " +
                "showResult=${qws?.showResult} id=${_sessionState.value.questions.getOrNull(idx)?.id}"
        )
        return navigationCoordinator.prevQuestionViaIcon()
    }
    fun nextQuestionViaIcon(): UnansweredNavResult {
        if (reviewCoordinator.tryNavigateReviewBrowse(1)) return UnansweredNavResult.Navigated
        val idx = _sessionState.value.currentIndex
        val qws = _sessionState.value.questionsWithState.getOrNull(idx)
        PracticeFullAnswerIconNavDebugLog.tapEntry(
            forward = true,
            source = "VM.nextQuestionViaIcon",
            detail = "idx=$idx fullAnswer=$isFullAnswerMode textLen=${qws?.textAnswer?.length ?: 0} " +
                "showResult=${qws?.showResult} id=${_sessionState.value.questions.getOrNull(idx)?.id}"
        )
        return navigationCoordinator.nextQuestionViaIcon()
    }
    fun prevQuestionViaIconDoubleClick(): Boolean =
        navigationCoordinator.prevQuestionViaIconDoubleClick()
    fun nextQuestionViaIconDoubleClick(): Boolean =
        navigationCoordinator.nextQuestionViaIconDoubleClick()
    fun canNavigateToPrevUnanswered(): Boolean =
        navigationCoordinator.canNavigateToPrevUnanswered() ||
            (isFullAnswerMode && navigationCoordinator.canSkipToUnansweredSource(forward = false))
    fun canNavigateToNextUnanswered(): Boolean =
        navigationCoordinator.canNavigateToNextUnanswered() ||
            (isFullAnswerMode && navigationCoordinator.canSkipToUnansweredSource(forward = true))
    fun hasPendingQuestions(): Boolean = answerHandler.hasPendingQuestions(
        _sessionState.value.questionsWithState,
        fullAnswerModeActive = activeFillConfig.generationMode == FillQuestionGenerationMode.FULL_ANSWER,
        fullAnswerRequireCorrect = activeFillConfig.fullAnswerRequireCorrect
    )
    fun prevQuestion() {
        if (reviewCoordinator.tryNavigateReviewBrowse(-1)) return
    }
    fun isInAnsweredHistory(): Boolean = navigationCoordinator.isInAnsweredHistory
    fun browseAnsweredHistoryOlder(): AnsweredHistoryBackwardResult {
        reviewCoordinator.browseAnsweredHistoryOlder()?.let { return it }
        val result = navigationCoordinator.browseAnsweredHistoryOlder()
        android.util.Log.d(
            "PracticeHistorySwipe",
            "VM.browseOlder | idx=${_sessionState.value.currentIndex} | fullAnswer=$isFullAnswerMode | result=$result"
        )
        return result
    }
    fun browseAnsweredHistoryNewer(): AnsweredHistoryForwardResult {
        reviewCoordinator.browseAnsweredHistoryNewer()?.let { return it }
        val result = navigationCoordinator.browseAnsweredHistoryNewer()
        android.util.Log.d(
            "PracticeHistorySwipe",
            "VM.browseNewer | idx=${_sessionState.value.currentIndex} | fullAnswer=$isFullAnswerMode | inHistory=${navigationCoordinator.isInAnsweredHistory} | result=$result"
        )
        return result
    }
    fun goToQuestion(index: Int, source: String = "goToQuestion") {
        val from = _sessionState.value.currentIndex
        if (from != index) {
            PracticeJumpDebugLog.vmGoToQuestion(from, index, source)
        }
        navigationCoordinator.goToQuestion(index)
    }
    val isFullAnswerMode: Boolean
        get() = activeFillConfig.generationMode == FillQuestionGenerationMode.FULL_ANSWER
    fun canSkipToUnansweredSource(forward: Boolean): Boolean =
        navigationCoordinator.canSkipToUnansweredSource(forward)
    fun skipToUnansweredSource(forward: Boolean): SkipUnansweredSourceResult =
        navigationCoordinator.skipToUnansweredSource(forward)
    fun canSkipToAdjacentSource(forward: Boolean): Boolean = canSkipToUnansweredSource(forward)
    fun skipToAdjacentSource(forward: Boolean) = skipToUnansweredSource(forward)
    fun buildAnswerCardDisplayInfo(questions: List<Question>): Map<Int, AnswerCardDisplayInfo> =
        AnswerCardDisplayInfoPipeline.build(
            sessionQuestions = questions,
            sourceCatalog = progressLifecycle.sourceCatalog(),
            fullAnswerMode = isFullAnswerMode
        )
    fun answerCardEntryGrouped(questions: List<Question>): Boolean =
        AnswerCardDisplayInfoPipeline.useEntryGroupedLayout(
            buildAnswerCardDisplayInfo(questions),
            isFullAnswerMode
        )
    fun saveProgress() = progressLifecycle.saveProgress()
    fun scheduleNavigationSave() = progressLifecycle.scheduleNavigationSave()
    fun clearProgress() = progressLifecycle.clearProgress()
    fun updateShowResult(index: Int, value: Boolean) {
        stateUpdater.updateShowResult(index, value)
        if (value) rememberAnsweredHistorySnapshot(index)
    }
    fun revealShowResult(index: Int) {
        stateUpdater.revealShowResult(index)
        viewModelScope.launch {
            rememberAnsweredHistorySnapshot(index)
            saveProgress()
        }
    }
    /** 浜ゅ嵎纭锛氭壒閲?reveal 鏈夎緭鍏ユ湭鎵规敼鐨勯锛屽苟鍐欏叆鍘嗗彶蹇収銆?*/
    suspend fun gradeSessionOnSubmit(): PracticeSessionGradeSnapshot {
        val revealed = stateUpdater.revealAllInputAnswers()
        revealed.forEach { index -> rememberAnsweredHistorySnapshot(index) }
        val state = _sessionState.value
        return PracticeSessionGradeSnapshot(
            sessionCorrectCount = state.sessionCorrectCount,
            sessionAnsweredCount = state.sessionAnsweredCount,
            answeredCount = state.answeredCount
        )
    }
    fun retryCurrentQuestion(index: Int) {
        val state = _sessionState.value
        if (index !in state.questionsWithState.indices) return
        rememberAnsweredHistorySnapshot(index)
        _sessionState.value = state.copy(
            currentIndex = index,
            questionsWithState = state.questionsWithState.mapIndexed { idx, qws ->
                if (idx == index) PracticeQuestionRetryPipeline.reopenCurrent(qws) else qws
            }
        )
        saveProgress()
    }
    fun retryWrongBlanks(index: Int) {
        val state = _sessionState.value
        if (index !in state.questionsWithState.indices) return
        rememberAnsweredHistorySnapshot(index)
        _sessionState.value = state.copy(
            currentIndex = index,
            questionsWithState = state.questionsWithState.mapIndexed { idx, qws ->
                if (idx == index) PracticeQuestionRetryPipeline.reopenWrongBlanks(qws) else qws
            }
        )
        saveProgress()
    }
    fun updateAnalysis(index: Int, text: String) {
        stateUpdater.updateAnalysis(index, text)
    }
    fun updateSparkAnalysis(index: Int, text: String) {
        stateUpdater.updateSparkAnalysis(index, text)
    }
    fun updateBaiduAnalysis(index: Int, text: String) {
        stateUpdater.updateBaiduAnalysis(index, text)
    }
    fun addHistoryRecord(score: Int, total: Int, unanswered: Int) {
        viewModelScope.launch {
            val id = "practice_${questionSourceId}"
            val actualAnswered = total - unanswered
            if (actualAnswered > 0) facade.history.add(HistoryRecord(score, total, unanswered, id))
        }
    }
    fun saveNote(questionId: Int, index: Int, text: String) {
        viewModelScope.launch { facade.notes.save(questionId, text) }
        noteCoordinator.saveNoteLocally(index, text)
    }
    suspend fun saveNoteAndWait(questionId: Int, index: Int, text: String): Boolean =
        noteCoordinator.saveNoteAndWait(questionId, index, text)
    fun appendNote(questionId: Int, index: Int, text: String) {
        viewModelScope.launch {
            try {
                noteCoordinator.appendNote(questionId, index, text)
            } catch (_: Exception) { }
        }
    }
    suspend fun appendNoteSuspend(questionId: Int, index: Int, text: String): Unit =
        noteCoordinator.appendNote(questionId, index, text)
    suspend fun getNote(questionId: Int): String? = noteCoordinator.getNote(questionId)
    fun updateQuestionContent(index: Int, newContent: String) {
        val currentState = _sessionState.value
        if (index in currentState.questionsWithState.indices) {
            val updated = currentState.updateAt(index) { qws -> qws.copy(question = qws.question.copy(content = newContent)) }
            _sessionState.value = updated
            val updatedQuestion = updated.questionsWithState[index].question
            viewModelScope.launch {
                val fileName = updatedQuestion.fileName ?: "default.json"
                val questionsToSave = updated.questionsWithState.map { it.question }.filter { it.fileName == fileName }
                val existingQuestions = facade.questions.get(fileName).firstOrNull() ?: emptyList()
                if (existingQuestions.isNotEmpty()) facade.questions.save(fileName, questionsToSave)
            }
        }
    }
    fun updateQuestionAllFields(index: Int, newContent: String, newOptions: List<String>, newAnswer: String, newExplanation: String) {
        val currentState = _sessionState.value
        if (index in currentState.questionsWithState.indices) {
            val updated = currentState.updateAt(index) { qws -> qws.copy(question = qws.question.copy(content = newContent, options = newOptions, answer = newAnswer, explanation = newExplanation)) }
            _sessionState.value = updated
            val updatedQuestion = updated.questionsWithState[index].question
            viewModelScope.launch {
                val fileName = updatedQuestion.fileName ?: "default.json"
                val existingQuestions = facade.questions.get(fileName).firstOrNull() ?: emptyList()
                val merged = existingQuestions.map { q -> if (q.id == updatedQuestion.id) updatedQuestion else q }
                if (existingQuestions.isNotEmpty()) {
                    facade.questions.save(fileName, merged)
                    _sessionState.value = _sessionState.value.updateAt(index) { qws -> qws.copy(question = updatedQuestion) }
                    saveProgress()
                }
            }
        }
    }
    fun clearExplanation(index: Int, question: Question) {
        val currentState = _sessionState.value
        if (index !in currentState.questionsWithState.indices) return
        val updated = currentState.updateAt(index) { qws ->
            qws.copy(question = qws.question.copy(explanation = ""))
        }
        _sessionState.value = updated
        viewModelScope.launch {
            val fileName = question.fileName ?: "default.json"
            val questionsToSave = updated.questionsWithState.map { it.question }.filter { it.fileName == fileName }
            val existingQuestions = facade.questions.get(fileName).firstOrNull() ?: emptyList()
            if (existingQuestions.isNotEmpty()) { facade.questions.save(fileName, questionsToSave) }
        }
    }
    fun loadWrongQuestions(fileName: String) {
        viewModelScope.launch { specialQuestionLoader.loadWrongQuestions(fileName) }
    }
    fun loadFavoriteQuestions(fileName: String) {
        viewModelScope.launch { specialQuestionLoader.loadFavoriteQuestions(fileName) }
    }
    val editableQuestion: StateFlow<Question?> = questionEditCoordinator.editableQuestion
    val saveSuccess: StateFlow<Boolean> = questionEditCoordinator.saveSuccess
    fun prepareEditableQuestion(questionId: Int) =
        questionEditCoordinator.prepareEditableQuestion(questionId)
    fun clearEditableQuestion() =
        questionEditCoordinator.clearEditableQuestion()
    suspend fun saveEditedQuestion(edited: Question): Boolean =
        questionEditCoordinator.saveEditedQuestion(edited)
    fun indexOfQuestionBySourceId(sourceId: Int?): Int =
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
            onSaved = ::saveProgress
        )
    }
    private fun reopenQuestionForFullAnswerRetry(index: Int) {
        PracticeQuestionReopenPipeline.reopenForFullAnswerRetry(
            sessionState = _sessionState,
            index = index,
            onSnapshot = ::rememberAnsweredHistorySnapshot,
            onSaved = ::saveProgress
        )
    }
}
