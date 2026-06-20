package com.example.testapp.presentation.screen.practice

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.core.common.FontSettingsRepository
import com.example.testapp.domain.model.*
import com.example.testapp.domain.usecase.PracticeUseCaseFacade
import com.example.testapp.domain.usecase.QuestionFlowCache
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.core.session.SessionEngine
import com.example.testapp.core.util.retainCorrectFillAnswerParts
import com.example.testapp.core.util.resolveFillCorrectAnswer
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
        setRandomPracticeEnabled = { randomPracticeEnabled = it; navigationCoordinator.randomPracticeEnabled = it }
    )

    private val specialQuestionLoader = PracticeSpecialQuestionLoader(
        facade = facade,
        sessionState = _sessionState,
        progressId = { progressId },
        randomPracticeEnabled = { randomPracticeEnabled },
        loadProgress = progressLifecycle::loadProgress
    )
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

    val currentIndex: StateFlow<Int> = _sessionState.map { it.currentIndex }.stateIn(
        viewModelScope, SharingStarted.Lazily, 0
    )

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

    init {
        navigationCoordinator.randomPracticeEnabled = randomPracticeEnabled
        navigationCoordinator.initPhase4(
            _sessionState = _sessionState,
            scope = viewModelScope,
            isQuestionPendingForCurrentMode = { qws ->
                answerHandler.isQuestionPendingForCurrentMode(
                    qws,
                    fullAnswerModeActive = false,
                    fullAnswerRequireCorrect = false
                )
            },
            shouldReopenUnansweredReveal = answerHandler::shouldReopenUnansweredReveal,
            currentSourcePendingIndices = { questionsWithState, currentIndex, eligibleIndices ->
                answerHandler.currentSourcePendingIndices(
                    questionsWithState,
                    currentIndex,
                    eligibleIndices,
                    fullAnswerModeActive = false,
                    fullAnswerRequireCorrect = false
                )
            },
            isCurrentSourceComplete = { state ->
                answerHandler.isCurrentSourceComplete(
                    state,
                    fullAnswerModeActive = false,
                    fullAnswerRequireCorrect = false
                )
            },
            findNextSourceEntryIndices = { emptyList() },
            findAdjacentDerivedQuestionIndex = { _, _ -> null },
            effectiveCurrentMemoryRoundQuestionIds = { emptySet() },
            nextFullAnswerCandidateIndices = { questionsWithState, currentIndex, eligibleIndices ->
                answerHandler.nextFullAnswerCandidateIndices(
                    questionsWithState,
                    currentIndex,
                    eligibleIndices,
                    fullAnswerModeActive = false,
                    fullAnswerRequireCorrect = false
                )
            },
            reopenQuestionForPendingRetry = ::reopenQuestionForPendingRetry,
            reopenQuestionForFullAnswerRetry = ::reopenQuestionForFullAnswerRetry,
            saveProgress = ::saveProgress,
            fullAnswerModeActive = { false },
            fullAnswerRequireCorrect = { false },
            memoryModeActive = { false }
        )
        viewModelScope.launch {
            facade.progress.clear("practice_default")
        }
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
        Log.d("JUMP_DEBUG", "[nextQuestion] called, stack: ${Exception().stackTraceToString().take(500)}")
        navigationCoordinator.nextQuestion()
    }

    fun prevQuestion() {
        Log.d("JUMP_DEBUG", "[prevQuestion] called, stack: ${Exception().stackTraceToString().take(500)}")
        navigationCoordinator.prevQuestion()
    }

    fun goToQuestion(index: Int) {
        navigationCoordinator.goToQuestion(index)
    }

    fun saveProgress() = progressLifecycle.saveProgress()

    fun clearProgress() = progressLifecycle.clearProgress()

    fun updateShowResult(index: Int, value: Boolean) {
        stateUpdater.updateShowResult(index, value)
        if (value) rememberAnsweredHistorySnapshot(index)
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
