package com.example.testapp.presentation.screen.practice

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.core.common.FontSettingsRepository
import com.example.testapp.domain.review.ReviewBrowseSession
import com.example.testapp.domain.review.ReviewAnsweredSwipePipeline
import com.example.testapp.presentation.screen.practice.navigation.AnsweredHistoryBackwardResult
import com.example.testapp.presentation.screen.practice.navigation.AnsweredHistoryForwardResult
import com.example.testapp.presentation.screen.practice.UnansweredNavResult
import com.example.testapp.core.common.parsePracticeReviewTarget
import com.example.testapp.domain.model.*
import com.example.testapp.domain.usecase.PracticeUseCaseFacade
import com.example.testapp.domain.usecase.QuestionFlowCache
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.core.session.SessionEngine
import com.example.testapp.core.util.FillQuestionGenerationMode
import com.example.testapp.core.util.retainCorrectFillAnswerParts
import com.example.testapp.core.util.resolveFillCorrectAnswer
import com.example.testapp.uicommon.component.AnswerCardDisplayInfo
import com.example.testapp.uicommon.component.AnswerCardDisplayInfoPipeline
import com.example.testapp.uicommon.model.QuestionUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    val questions: StateFlow<List<Question>> = _sessionState.map { it.questions }.stateIn(
        viewModelScope, SharingStarted.Lazily, emptyList()
    )

    val uiQuestions: StateFlow<List<QuestionUiModel>> = _sessionState.map { state ->
        state.questionsWithState.map { questionWithState ->
            QuestionUiModel(
                question = questionWithState.question,
                status = when {
                    !questionWithState.isAnswered -> AnswerStatus.UNANSWERED
                    !questionWithState.showResult -> AnswerStatus.UNANSWERED
                    questionWithState.isCorrect == true -> AnswerStatus.CORRECT
                    else -> AnswerStatus.INCORRECT
                },
                selectedOptions = questionWithState.selectedOptions
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val currentIndex: StateFlow<Int> = _sessionState.map { it.currentIndex }.distinctUntilChanged().stateIn(
        viewModelScope, SharingStarted.Lazily, 0
    )

    val sessionAnsweredCountFlow: StateFlow<Int> = _sessionState.map { it.sessionAnsweredCount }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val sessionCorrectCountFlow: StateFlow<Int> = _sessionState.map { it.sessionCorrectCount }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val currentQuestionUi: StateFlow<PracticeCurrentQuestionUi?> = _sessionState
        .map { PracticeCurrentQuestionUiPipeline.snapshot(it) }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val answeredList: StateFlow<List<Int>> = _sessionState.map { it.answeredIndices }.stateIn(
        viewModelScope, SharingStarted.Lazily, emptyList()
    )

    val selectedOptions: StateFlow<List<List<Int>>> = _sessionState.map { state ->
        state.questionsWithState.map { it.selectedOptions }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val progressLoaded: StateFlow<Boolean> = _sessionState.map { it.progressLoaded }.stateIn(
        viewModelScope, SharingStarted.Lazily, false
    )

    val showResultList: StateFlow<List<Boolean>> = _sessionState.map { state ->
        state.questionsWithState.map { it.showResult }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val analysisList: StateFlow<List<String>> = _sessionState.map { state ->
        state.questionsWithState.map { it.analysis }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val sparkAnalysisList: StateFlow<List<String>> = _sessionState.map { state ->
        state.questionsWithState.map { it.sparkAnalysis }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val baiduAnalysisList: StateFlow<List<String>> = _sessionState.map { state ->
        state.questionsWithState.map { it.baiduAnalysis }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val noteList: StateFlow<List<String>> = _sessionState.map { state ->
        state.questionsWithState.map { it.note }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val textAnswers: StateFlow<List<String>> = _sessionState.map { state ->
        state.questionsWithState.map { it.textAnswer }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

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
    private var reviewBrowseSession: ReviewBrowseSession? = null
    private var reviewAnsweredSwipeOrder: List<Int> = emptyList()

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
            memoryModeActive = { false }
        )
        viewModelScope.launch {
            facade.progress.clear("practice_default")
        }
    }

    fun reloadForFillConfig(questionCount: Int? = null) {
        progressLifecycle.reloadForFillConfig(
            questionCount ?: progressLifecycle.lastAppliedQuestionCount()
        )
    }

    fun enterReviewSession(targetProgressId: String) {
        viewModelScope.launch {
            _reviewModeActive.value = true
            _reviewReady.value = false
            if (PracticeReviewReusePipeline.canReuse(progressId, targetProgressId, _sessionState.value)) {
                applyReviewPresentation()
                return@launch
            }
            val target = parsePracticeReviewTarget(targetProgressId)
            progressLifecycle.loadReviewSession(
                targetProgressId = target.progressId,
                sourceId = target.quizFileName,
                questionCount = target.questionCount,
                wrongBook = target.isWrongBookMode,
                favorite = target.isFavoriteMode
            ) {
                applyReviewPresentation()
            }
        }
    }

    private suspend fun applyReviewPresentation() {
        val state = _sessionState.value
        val prepared = withContext(Dispatchers.Default) {
            PracticeReviewPresentationPipeline.prepare(state)
        }
        reviewBrowseSession = prepared.reviewBrowseSession
        reviewAnsweredSwipeOrder = prepared.reviewAnsweredSwipeOrder
        _sessionState.value = state.copy(
            questionsWithState = prepared.questionsWithState,
            currentIndex = prepared.currentIndex,
            progressLoaded = true
        )
        _reviewReady.value = true
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
        if (tryNavigateReviewBrowse(1)) return
        Log.d("JUMP_DEBUG", "[nextQuestion] called, stack: ${Exception().stackTraceToString().take(500)}")
        navigationCoordinator.nextQuestion()
    }

    fun prevQuestionViaIcon(): UnansweredNavResult {
        if (tryNavigateReviewBrowse(-1)) return UnansweredNavResult.Navigated
        return navigationCoordinator.prevQuestionViaIcon()
    }

    fun nextQuestionViaIcon(): UnansweredNavResult {
        if (tryNavigateReviewBrowse(1)) return UnansweredNavResult.Navigated
        return navigationCoordinator.nextQuestionViaIcon()
    }

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
        if (tryNavigateReviewBrowse(-1)) return
    }

    fun isInAnsweredHistory(): Boolean = navigationCoordinator.isInAnsweredHistory

    fun browseAnsweredHistoryOlder(): AnsweredHistoryBackwardResult {
        if (reviewBrowseSession != null) {
            val ordered = reviewAnsweredSwipeOrder
            if (ordered.isEmpty()) return AnsweredHistoryBackwardResult.NoMoreHistory
            val currentIndex = _sessionState.value.currentIndex
            val target = ReviewAnsweredSwipePipeline.resolveOlderIndex(ordered, currentIndex)
            if (target == null) {
                return if (ReviewAnsweredSwipePipeline.isAtOldest(ordered, currentIndex)) {
                    AnsweredHistoryBackwardResult.AtOldestAnswered
                } else {
                    AnsweredHistoryBackwardResult.NoMoreHistory
                }
            }
            _sessionState.update { it.copy(currentIndex = target) }
            scheduleNavigationSave()
            return AnsweredHistoryBackwardResult.Navigated
        }
        val result = navigationCoordinator.browseAnsweredHistoryOlder()
        android.util.Log.d(
            "PracticeHistorySwipe",
            "VM.browseOlder | idx=${_sessionState.value.currentIndex} | fullAnswer=$isFullAnswerMode | result=$result"
        )
        return result
    }

    fun browseAnsweredHistoryNewer(): AnsweredHistoryForwardResult {
        if (reviewBrowseSession != null) {
            val ordered = reviewAnsweredSwipeOrder
            if (ordered.isEmpty()) return AnsweredHistoryForwardResult.AtLatestAnswered
            val currentIndex = _sessionState.value.currentIndex
            val target = ReviewAnsweredSwipePipeline.resolveNewerIndex(ordered, currentIndex)
            if (target == null) {
                return AnsweredHistoryForwardResult.AtLatestAnswered
            }
            _sessionState.update { it.copy(currentIndex = target) }
            scheduleNavigationSave()
            return AnsweredHistoryForwardResult.Navigated
        }
        val result = navigationCoordinator.browseAnsweredHistoryNewer()
        android.util.Log.d(
            "PracticeHistorySwipe",
            "VM.browseNewer | idx=${_sessionState.value.currentIndex} | fullAnswer=$isFullAnswerMode | inHistory=${navigationCoordinator.isInAnsweredHistory} | result=$result"
        )
        return result
    }

    fun goToQuestion(index: Int) {
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
        val currentState = _sessionState.value
        if (index !in currentState.questionsWithState.indices) return
        navigationCoordinator.rememberAnsweredHistorySnapshot(currentState.questionsWithState[index])
        _sessionState.value = currentState.copy(
            currentIndex = index,
            questionsWithState = currentState.questionsWithState.mapIndexed { idx, questionWithState ->
                if (idx == index) {
                    questionWithState.copy(showResult = false, sessionAnswerTime = 0L)
                } else {
                    questionWithState
                }
            }
        )
        saveProgress()
    }

    private fun reopenQuestionForFullAnswerRetry(index: Int) {
        val currentState = _sessionState.value
        if (index !in currentState.questionsWithState.indices) return
        navigationCoordinator.rememberAnsweredHistorySnapshot(currentState.questionsWithState[index])
        val targetQuestion = currentState.questionsWithState[index]
        _sessionState.value = currentState.copy(
            currentIndex = index,
            questionsWithState = currentState.questionsWithState.mapIndexed { idx, questionWithState ->
                if (idx != index) {
                    questionWithState
                } else if (QuestionTypes.isFill(questionWithState.question.type)) {
                    val retainedAnswer = retainCorrectFillAnswerParts(
                        userAnswer = targetQuestion.textAnswer,
                        correctAnswer = resolveFillCorrectAnswer(targetQuestion.question)
                    )
                    questionWithState.copy(
                        textAnswer = retainedAnswer,
                        selectedOptions = if (retainedAnswer.isNotBlank()) listOf(-1) else emptyList(),
                        showResult = false,
                        sessionAnswerTime = 0L
                    )
                } else {
                    questionWithState.copy(
                        selectedOptions = emptyList(),
                        textAnswer = "",
                        showResult = false,
                        sessionAnswerTime = 0L
                    )
                }
            }
        )
        saveProgress()
    }
}
