package com.example.testapp.presentation.screen.practice.session

import com.example.testapp.domain.model.PracticeProgress
import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState
import com.example.testapp.domain.model.UnifiedQuestionState
import com.example.testapp.domain.usecase.ClearPracticeProgressUseCase
import com.example.testapp.domain.usecase.SavePracticeProgressUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Progress Persistence — saveProgress + clearProgress + buildStoredQuestionState
 * Extracted from PracticeSessionCoordinator to keep each file <200 lines.
 */
class ProgressPersistence(
    private val _sessionState: MutableStateFlow<PracticeSessionState>,
    private val scope: CoroutineScope,
    private val saveProgressMutex: Mutex,
    private val savePracticeProgressUseCase: SavePracticeProgressUseCase,
    private val clearPracticeProgressUseCase: ClearPracticeProgressUseCase,
    // lambda callbacks
    private val buildSessionIdWithFillSignature: (String, Long, String) -> String,
    private val currentFillConfigSignature: suspend () -> String,
    private val resetLocalStateFn: () -> Unit,
    private val noteTraceTag: String,
    // mutable VM fields
    private val progressId: () -> String,
    private val memoryModeActive: () -> Boolean,
    private val allSourceQuestions: () -> List<Question>,
    private val persistentQuestionStateMap: MutableMap<Int, UnifiedQuestionState>
) {
    suspend fun buildStoredQuestionState(
        question: Question,
        savedState: UnifiedQuestionState?,
        restoreConfiguredQuestionSnapshot: (Question, UnifiedQuestionState?) -> Question,
        getAnalysis: suspend (Int) -> String,
        getSpark: suspend (Int) -> String,
        getBaidu: suspend (Int) -> String,
        getNote: suspend (Int) -> String
    ): QuestionWithState {
        val restoredQuestion = restoreConfiguredQuestionSnapshot(question, savedState)
        val deepSeekAnalysis = savedState?.analysis?.takeIf { it.isNotBlank() }
            ?: getAnalysis(restoredQuestion.id)
        val sparkAnalysis = savedState?.sparkAnalysis?.takeIf { it.isNotBlank() }
            ?: getSpark(restoredQuestion.id)
        val baiduAnalysis = savedState?.baiduAnalysis?.takeIf { it.isNotBlank() }
            ?: getBaidu(restoredQuestion.id)
        val note = savedState?.note?.takeIf { it.isNotBlank() }
            ?: getNote(restoredQuestion.id)

        if (note.isNotBlank() || savedState?.note?.isNullOrBlank() == false) {
        }

        return QuestionWithState(
            question = restoredQuestion,
            selectedOptions = savedState?.selectedOptions ?: emptyList(),
            textAnswer = savedState?.textAnswer ?: "",
            showResult = savedState?.showResult ?: false,
            analysis = deepSeekAnalysis,
            sparkAnalysis = sparkAnalysis,
            baiduAnalysis = baiduAnalysis,
            note = note,
            sessionAnswerTime = savedState?.answerTime ?: 0L
        )
    }

    fun saveProgress() {
        scope.launch(Dispatchers.Default) {
            saveProgressMutex.withLock {
                val currentState = _sessionState.value
                val questionStateMap = mutableMapOf<Int, UnifiedQuestionState>()
                val fixedQuestionOrder = mutableListOf<Int>()
                val ma = memoryModeActive()
                val asq = allSourceQuestions()

                currentState.questionsWithState.forEach { qws ->
                    val qId = qws.question.id
                    fixedQuestionOrder.add(qId)
                    questionStateMap[qId] = UnifiedQuestionState(
                        questionId = qId,
                        selectedOptions = qws.selectedOptions,
                        textAnswer = qws.textAnswer,
                        showResult = qws.showResult,
                        analysis = qws.analysis,
                        sparkAnalysis = qws.sparkAnalysis,
                        baiduAnalysis = qws.baiduAnalysis,
                        note = qws.note,
                        answerTime = qws.sessionAnswerTime,
                        displayedQuestionContent = qws.question.content,
                        displayedQuestionAnswer = qws.question.answer
                    )
                }

                if (ma) persistentQuestionStateMap.putAll(questionStateMap)
                val finalQuestionStateMap = if (ma) persistentQuestionStateMap.toMap() else questionStateMap
                val finalFixedOrder = if (ma && asq.isNotEmpty()) asq.map { it.id } else fixedQuestionOrder

                val fillSignature = currentFillConfigSignature()
                val progress = PracticeProgress(
                    id = progressId(),
                    currentIndex = currentState.currentIndex,
                    answeredList = currentState.answeredIndices,
                    selectedOptions = currentState.questionsWithState.map { it.selectedOptions },
                    showResultList = currentState.questionsWithState.map { it.showResult },
                    analysisList = currentState.questionsWithState.map { it.analysis },
                    sparkAnalysisList = currentState.questionsWithState.map { it.sparkAnalysis },
                    baiduAnalysisList = currentState.questionsWithState.map { it.baiduAnalysis },
                    noteList = currentState.questionsWithState.map { it.note },
                    timestamp = System.currentTimeMillis(),
                    sessionId = buildSessionIdWithFillSignature(progressId(), currentState.sessionStartTime, fillSignature),
                    fixedQuestionOrder = finalFixedOrder,
                    questionStateMap = finalQuestionStateMap
                )

                savePracticeProgressUseCase(progress)
            }
        }
    }

    fun clearProgress() {
        scope.launch {
            clearPracticeProgressUseCase(progressId())
            resetLocalStateFn()
        }
    }
}
