package com.example.testapp.presentation.screen.practice

import com.example.testapp.core.common.FontSettingsRepository
import com.example.testapp.core.session.SessionEngine
import com.example.testapp.core.util.FillQuestionGenerationMode
import com.example.testapp.core.util.transformQuestionVariantsForFillSettings
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.PracticeProgress
import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.QuestionWithState
import com.example.testapp.domain.model.toUnifiedSessionState
import com.example.testapp.domain.usecase.PracticeUseCaseFacade
import com.example.testapp.domain.usecase.QuestionFlowCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class PracticeProgressLifecycleCoordinator(
    private val scope: CoroutineScope,
    private val sessionEngine: SessionEngine,
    private val facade: PracticeUseCaseFacade,
    private val questionFlowCache: QuestionFlowCache,
    private val fontSettings: FontSettingsRepository,
    private val sessionState: MutableStateFlow<PracticeSessionState>,
    private val repositoryContentLoader: PracticeRepositoryContentLoader,
    private val progressId: () -> String,
    private val setProgressIdValue: (String) -> Unit,
    private val questionSourceId: () -> String,
    private val setQuestionSourceId: (String) -> Unit,
    private val randomPracticeEnabled: () -> Boolean,
    private val setRandomPracticeEnabled: (Boolean) -> Unit
) {
    private var loadQuestionsJob: Job? = null
    private var loadProgressJob: Job? = null

    fun setProgressId(
        id: String,
        questionsId: String = id,
        loadQuestions: Boolean = true,
        questionCount: Int = 0,
        random: Boolean = randomPracticeEnabled()
    ) {
        setProgressIdValue(ensurePrefix(id))
        setQuestionSourceId(questionsId)
        setRandomPracticeEnabled(random)
        android.util.Log.d("PracticeViewModel", "setProgressId: randomPracticeEnabled=${randomPracticeEnabled()}, randomParam=$random")
        val sessionId = "${progressId()}_${System.currentTimeMillis()}"
        val newSessionStartTime = System.currentTimeMillis()

        sessionState.value = sessionState.value.copy(
            progressLoaded = false,
            sessionStartTime = newSessionStartTime
        )
        repositoryContentLoader.reset()

        if (!loadQuestions) return

        loadQuestionsJob?.cancel()
        loadQuestionsJob = scope.launch {
            val sourceId = questionSourceId()
            val originalQuestions = loadOriginalQuestions(sourceId)
            android.util.Log.d("PracticeViewModel", "setProgressId: originalQuestions.size=${originalQuestions.size}, ids=${originalQuestions.map { it.id }}")
            if (originalQuestions.isEmpty()) {
                sessionState.value = PracticeSessionState(
                    progressLoaded = true,
                    progressId = progressId(),
                    sessionStartTime = newSessionStartTime
                )
                return@launch
            }

            val existingProgress = facade.progress.getFlow(progressId()).firstOrNull()
            val fillSettings = readFillSettings()
            val questionsWithFixedOrder = if (existingProgress?.fixedQuestionOrder?.isNotEmpty() == true) {
                val questionsMap = originalQuestions.associateBy { it.id }
                existingProgress.fixedQuestionOrder.mapNotNull { questionsMap[it] }
            } else {
                val smartOrderedQuestions = withContext(Dispatchers.Default) {
                    orderedForPractice(originalQuestions, existingProgress, newSessionStartTime)
                }
                val fillTransformed = withContext(Dispatchers.Default) {
                    applyFillTransform(smartOrderedQuestions, fillSettings, newSessionStartTime)
                }
                val finalQuestions = if (questionCount > 0) {
                    fillTransformed.take(questionCount.coerceAtMost(fillTransformed.size))
                } else {
                    fillTransformed
                }
                android.util.Log.d("PracticeViewModel", "setProgressId: finalQuestions.size=${finalQuestions.size}, ids=${finalQuestions.map { it.id }}")
                facade.progress.save(
                    PracticeProgress(
                        id = progressId(),
                        currentIndex = 0,
                        answeredList = emptyList(),
                        selectedOptions = emptyList(),
                        showResultList = emptyList(),
                        analysisList = emptyList(),
                        sparkAnalysisList = emptyList(),
                        baiduAnalysisList = emptyList(),
                        noteList = emptyList(),
                        timestamp = newSessionStartTime,
                        sessionId = sessionId,
                        fixedQuestionOrder = finalQuestions.map { it.id },
                        questionStateMap = emptyMap()
                    )
                )
                finalQuestions
            }

            android.util.Log.d("PracticeViewModel", "setProgressId: randomPracticeEnabled=${randomPracticeEnabled()}, id=$id, questionsId=$questionsId, questionCount=$questionCount, loadQuestions=$loadQuestions")
            val questionsWithState = questionsWithFixedOrder.map { question ->
                val questionState = existingProgress?.questionStateMap?.get(question.id)
                QuestionWithState(
                    question = question,
                    selectedOptions = questionState?.selectedOptions ?: emptyList(),
                    showResult = questionState?.showResult ?: false,
                    analysis = questionState?.analysis ?: "",
                    sparkAnalysis = questionState?.sparkAnalysis ?: "",
                    baiduAnalysis = questionState?.baiduAnalysis ?: "",
                    note = questionState?.note ?: "",
                    sessionAnswerTime = questionState?.answerTime ?: 0L
                )
            }
            sessionState.value = sessionState.value.copy(
                questionsWithState = questionsWithState,
                sessionStartTime = newSessionStartTime,
                currentIndex = existingProgress?.currentIndex ?: 0
            )
            scope.launch { repositoryContentLoader.loadOnce() }
            loadProgress()
        }
    }

    private suspend fun loadOriginalQuestions(sourceId: String): List<Question> =
        questionFlowCache.preload(sourceId)

    fun loadProgress() {
        loadProgressJob?.cancel()
        loadProgressJob = scope.launch {
            val progress = facade.progress.getFlow(progressId()).firstOrNull()
            val currentState = sessionState.value
            if (!currentState.progressLoaded) {
                if (progress != null) {
                    val result = sessionEngine.progressManager.restoreFromRawProgress(
                        currentState.questions,
                        progress,
                        currentState.sessionStartTime
                    )
                    if (result != null) {
                        val updatedQuestionsWithState = result.questionsWithState.map { qws ->
                            val savedState = (progress as? PracticeProgress)?.questionStateMap?.get(qws.question.id)
                            if (savedState != null && savedState.showResult && savedState.selectedOptions.isNotEmpty()) {
                                qws
                            } else if (savedState != null) {
                                val showResult = if (savedState.selectedOptions.isNotEmpty()) {
                                    savedState.showResult || (savedState.answerTime > 0L && savedState.answerTime < currentState.sessionStartTime)
                                } else {
                                    savedState.showResult
                                }
                                qws.copy(showResult = showResult)
                            } else {
                                qws
                            }
                        }
                        val smartCurrentIndex = if (randomPracticeEnabled()) {
                            val unansweredIndices = updatedQuestionsWithState.mapIndexedNotNull { index, qws ->
                                if (qws.selectedOptions.isEmpty()) index else null
                            }
                            if (unansweredIndices.isNotEmpty()) {
                                unansweredIndices.random(kotlin.random.Random(currentState.sessionStartTime))
                            } else {
                                result.savedCurrentIndex.coerceAtMost(currentState.questionsWithState.size - 1)
                            }
                        } else {
                            result.savedCurrentIndex.coerceAtMost(currentState.questionsWithState.size - 1)
                        }
                        sessionState.value = currentState.copy(
                            currentIndex = smartCurrentIndex,
                            questionsWithState = updatedQuestionsWithState,
                            progressLoaded = true
                        )
                    } else {
                        sessionState.value = currentState.copy(progressLoaded = true)
                    }
                } else {
                    val smartStartIndex = if (randomPracticeEnabled() && currentState.questionsWithState.isNotEmpty()) {
                        (0 until currentState.questionsWithState.size).random(kotlin.random.Random(currentState.sessionStartTime))
                    } else {
                        0
                    }
                    sessionState.value = currentState.copy(currentIndex = smartStartIndex, progressLoaded = true)
                    saveProgress()
                }
            }
        }
    }

    fun saveProgress() {
        scope.launch {
            sessionEngine.progressManager.saveProgress(
                progressId = progressId(),
                state = sessionState.value.toUnifiedSessionState(),
                memoryActive = false,
                allSourceQuestions = emptyList(),
                fillSignature = ""
            )
        }
    }

    fun clearProgress() {
        scope.launch {
            sessionEngine.progressManager.clearProgress(progressId())
            resetLocalState()
            repositoryContentLoader.reset()
        }
    }

    private fun ensurePrefix(id: String): String = if (id.startsWith("practice_")) id else "practice_$id"

    private data class FillSettings(
        val maxBlanks: Int,
        val genMode: FillQuestionGenerationMode,
        val far: Boolean,
        val minS: Int,
        val maxS: Int,
        val tag: String
    )

    private suspend fun readFillSettings(): FillSettings = FillSettings(
        maxBlanks = fontSettings.fillBlankCount.firstOrNull() ?: 0,
        genMode = fontSettings.fillQuestionGenerationMode.firstOrNull()
            ?: FillQuestionGenerationMode.SCORE_RANGE_RANDOM,
        far = fontSettings.fillFullAnswerRandomOrder.firstOrNull() ?: true,
        minS = fontSettings.fillAnswerScoreMin.firstOrNull() ?: 1,
        maxS = fontSettings.fillAnswerScoreMax.firstOrNull() ?: 10,
        tag = fontSettings.fillAnswerTagFilter.firstOrNull().orEmpty()
    )

    private fun applyFillTransform(
        questions: List<Question>,
        settings: FillSettings,
        sessionStartTime: Long
    ): List<Question> = questions.flatMapIndexed { i, q ->
        if (QuestionTypes.isInlineBlank(q.type)) {
            transformQuestionVariantsForFillSettings(
                q,
                settings.maxBlanks,
                settings.genMode,
                settings.far,
                settings.minS,
                settings.maxS,
                settings.tag,
                sessionStartTime + q.id.toLong() + i
            )
        } else {
            listOf(q)
        }
    }

    private fun orderedForPractice(
        originalQuestions: List<com.example.testapp.domain.model.Question>,
        existingProgress: PracticeProgress?,
        newSessionStartTime: Long
    ): List<com.example.testapp.domain.model.Question> {
        if (!randomPracticeEnabled()) return originalQuestions
        if (existingProgress == null) return originalQuestions.shuffled(java.util.Random(newSessionStartTime))

        val answeredQuestionIds = existingProgress.questionStateMap
            .filterValues { it.selectedOptions.isNotEmpty() && it.showResult }
            .keys
        val unansweredQuestions = originalQuestions.filter { it.id !in answeredQuestionIds }
        val answeredQuestions = originalQuestions.filter { it.id in answeredQuestionIds }
        return if (unansweredQuestions.isNotEmpty()) {
            unansweredQuestions.shuffled(java.util.Random(newSessionStartTime)) +
                answeredQuestions.shuffled(java.util.Random(newSessionStartTime + 1000))
        } else {
            originalQuestions.shuffled(java.util.Random(newSessionStartTime))
        }
    }

    private fun resetLocalState() {
        val currentState = sessionState.value
        sessionState.value = currentState.copy(
            currentIndex = 0,
            questionsWithState = currentState.questionsWithState.map {
                it.copy(
                    selectedOptions = emptyList(),
                    showResult = false,
                    analysis = "",
                    sparkAnalysis = "",
                    baiduAnalysis = "",
                    note = ""
                )
            },
            progressLoaded = false
        )
    }
}
