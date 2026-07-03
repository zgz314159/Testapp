package com.example.testapp.presentation.screen.practice

import com.example.testapp.core.common.FontSettingsRepository
import com.example.testapp.core.session.NavigationSaveScheduler
import com.example.testapp.core.session.SessionEngine
import com.example.testapp.core.util.extractSourceQuestionId
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
    private val setRandomPracticeEnabled: (Boolean) -> Unit,
    private val onFillConfigApplied: (PracticeFillConfig) -> Unit = {},
    private val onProgressRestored: (List<QuestionWithState>, Int) -> Unit = { _, _ -> }
) {
    private val progressCoordinator = PracticeProgressCoordinator()
    private val navigationSaveScheduler = NavigationSaveScheduler(scope)
    private var loadQuestionsJob: Job? = null
    private var loadProgressJob: Job? = null
    private var lastQuestionCount: Int = 0
    private var lastAppliedInitKey: String? = null
    private var sourceCatalogQuestions: List<Question> = emptyList()
    private val cumulativeQuestionStateMap = mutableMapOf<Int, com.example.testapp.domain.model.UnifiedQuestionState>()

    fun sourceCatalog(): List<Question> = sourceCatalogQuestions

    fun lastAppliedQuestionCount(): Int = lastQuestionCount

    fun shouldReloadForQuizInit(initKey: String): Boolean {
        val state = sessionState.value
        val sessionActive = state.progressLoaded && state.questionsWithState.isNotEmpty()
        return PracticeQuizInitReloadPipeline.shouldReloadFillConfig(
            sessionActive = sessionActive,
            appliedInitKey = lastAppliedInitKey,
            currentInitKey = initKey
        )
    }

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
        lastQuestionCount = questionCount
        lastAppliedInitKey = null
        android.util.Log.d("PracticeViewModel", "setProgressId: randomPracticeEnabled=${randomPracticeEnabled()}, randomParam=$random")
        val newSessionStartTime = System.currentTimeMillis()

        sessionState.value = sessionState.value.copy(
            progressLoaded = false,
            sessionStartTime = newSessionStartTime
        )
        repositoryContentLoader.reset()

        if (!loadQuestions) return

        loadQuestionsJob?.cancel()
        loadQuestionsJob = scope.launch {
            loadQuestionsForCurrentSource(questionCount, newSessionStartTime)
        }
    }

    fun reloadForFillConfig(questionCount: Int = lastQuestionCount, initKey: String? = null) {
        if (questionSourceId().isBlank()) return
        lastQuestionCount = questionCount
        val preserveIndex = sessionState.value.takeIf {
            it.progressLoaded && it.questionsWithState.isNotEmpty()
        }?.currentIndex
        loadQuestionsJob?.cancel()
        loadQuestionsJob = scope.launch {
            val newSessionStartTime = System.currentTimeMillis()
            sessionState.value = sessionState.value.copy(
                progressLoaded = false,
                sessionStartTime = newSessionStartTime
            )
            loadQuestionsForCurrentSource(
                questionCount = questionCount,
                newSessionStartTime = newSessionStartTime,
                preserveCurrentIndex = preserveIndex
            )
            initKey?.let { lastAppliedInitKey = it }
        }
    }

    private suspend fun loadQuestionsForCurrentSource(
        questionCount: Int,
        newSessionStartTime: Long,
        sourceQuestions: List<Question>? = null,
        preserveCurrentIndex: Int? = null
    ) {
        val sourceId = questionSourceId()
        val originalQuestions = sourceQuestions ?: loadOriginalQuestions(sourceId)
        android.util.Log.d(
            "PracticeViewModel",
            "loadQuestionsForCurrentSource: originalQuestions.size=${originalQuestions.size}, ids=${originalQuestions.map { it.id }}"
        )
        if (originalQuestions.isEmpty()) {
            sourceCatalogQuestions = emptyList()
            sessionState.value = PracticeSessionState(
                progressLoaded = true,
                progressId = progressId(),
                sessionStartTime = newSessionStartTime
            )
            return
        }

        sourceCatalogQuestions = originalQuestions.distinctBy { it.id }

        var existingProgress = facade.progress.getFlow(progressId()).firstOrNull()
        val fillConfig = PracticeFillConfigPipeline.read(fontSettings)
        onFillConfigApplied(fillConfig)
        val curFillSignature = fillConfig.signature()
        val fillConfigSensitive = PracticeFillConfigPipeline.isFillConfigSensitive(originalQuestions, sourceId)
        val canReuseByFill = progressCoordinator.canReuseByFillSignature(
            existingProgress?.sessionId,
            curFillSignature,
            fillConfigSensitive
        )
        val savedFillSignature = progressCoordinator.extractFillConfigSignature(existingProgress?.sessionId)
        if (existingProgress != null && savedFillSignature.isNullOrBlank() && canReuseByFill) {
            val upgradedProgress = existingProgress.copy(
                sessionId = progressCoordinator.buildSessionIdWithFillSignature(
                    progressId(),
                    progressCoordinator.practiceProgressSeed(existingProgress, newSessionStartTime),
                    curFillSignature
                )
            )
            facade.progress.save(upgradedProgress)
            existingProgress = upgradedProgress
        }

        val savedSourceOrder = existingProgress?.fixedQuestionOrder
            ?.map(::extractSourceQuestionId)
            ?.distinct()
            ?.takeIf { canReuseByFill && it.isNotEmpty() }
            .orEmpty()
        val fullPoolSize = originalQuestions.distinctBy { it.id }.size
        val priorComplete = PracticeRoundCompletePipeline.isComplete(existingProgress)
        val savedSourcesDone = PracticeSourceQuestionPipeline.savedSourcesFullyAnswered(
            savedSourceOrder,
            existingProgress?.questionStateMap.orEmpty()
        )
        val startNewRound = priorComplete || savedSourcesDone
        cumulativeQuestionStateMap.clear()
        cumulativeQuestionStateMap.putAll(existingProgress?.questionStateMap.orEmpty())
        val canReuseSavedOrder = PracticeRoundReusePipeline.canReuseSavedOrder(
            progress = existingProgress,
            savedSourceOrder = savedSourceOrder,
            questionCount = questionCount,
            fullPoolSize = fullPoolSize,
            canReuseByFill = canReuseByFill
        )
        val orderedSourceQuestions = if (canReuseSavedOrder) {
            val questionsMap = originalQuestions.associateBy { it.id }
            savedSourceOrder.mapNotNull { questionsMap[it] }
        } else {
            withContext(Dispatchers.Default) {
                val lastRoundSourceIds = if (startNewRound) {
                    PracticeSourceQuestionPipeline.lastRoundSourceIds(existingProgress?.fixedQuestionOrder.orEmpty())
                } else {
                    emptySet()
                }
                val answeredSourceIds = PracticeQuestionOrderPipeline.answeredQuestionIds(
                    existingProgress?.questionStateMap.orEmpty()
                )
                PracticeQuestionOrderPipeline.orderForNewRound(
                    originalQuestions = originalQuestions,
                    questionCount = questionCount,
                    random = randomPracticeEnabled(),
                    answeredSourceIds = answeredSourceIds,
                    seed = newSessionStartTime,
                    lastRoundSourceIds = lastRoundSourceIds
                )
            }
        }
        val fillSeed = progressCoordinator.practiceProgressSeed(
            if (canReuseByFill) existingProgress else null,
            newSessionStartTime
        )
        val fillTransformed = withContext(Dispatchers.Default) {
            PracticeFillConfigPipeline.applyTransform(orderedSourceQuestions, fillConfig, fillSeed)
        }
        val questionsWithFixedOrder = PracticeQuestionCountPolicy.limitQuestions(
            fillTransformed,
            questionCount
        )
        PracticeRoundLoadLog.loadQuestions(
            priorComplete = priorComplete,
            startNewRound = startNewRound,
            canReuse = canReuseSavedOrder,
            savedSize = savedSourceOrder.size,
            questionCount = questionCount,
            orderedIds = questionsWithFixedOrder.map { it.id },
            answeredSourceIds = PracticeSourceQuestionPipeline.answeredSourceIds(
                existingProgress?.questionStateMap.orEmpty()
            ),
            lastRoundSourceIds = if (startNewRound) {
                PracticeSourceQuestionPipeline.lastRoundSourceIds(existingProgress?.fixedQuestionOrder.orEmpty())
            } else {
                emptySet()
            },
            savedSourcesDone = savedSourcesDone
        )
        if (startNewRound || existingProgress?.fixedQuestionOrder.isNullOrEmpty() || !canReuseByFill || !canReuseSavedOrder) {
            val newProgress = PracticeNewRoundProgressPipeline.build(
                prior = existingProgress,
                progressId = progressId(),
                seed = newSessionStartTime,
                sessionId = progressCoordinator.buildSessionIdWithFillSignature(
                    progressId(),
                    fillSeed,
                    curFillSignature
                ),
                questions = questionsWithFixedOrder
            )
            facade.progress.save(newProgress)
            existingProgress = newProgress
        }

        val restoreFromMap = PracticeSessionRestorePipeline.shouldRestoreAnswersFromMap(existingProgress)
        PracticeRoundLoadLog.restore(restoreFromMap, existingProgress?.questionStateMap?.size ?: 0)
        val baseQuestionsWithState = questionsWithFixedOrder.map { QuestionWithState(question = it) }
        val questionsWithState = PracticeSessionRestorePipeline.resolveSessionQuestions(
            sessionQuestions = baseQuestionsWithState,
            progress = existingProgress,
            restoreFromMap = restoreFromMap,
            sessionStartTime = newSessionStartTime
        )
        val startIndex = PracticeSessionStartIndexPipeline.resolve(
            questionCount = questionsWithState.size,
            restoreFromMap = restoreFromMap,
            savedCurrentIndex = existingProgress?.currentIndex,
            randomPracticeEnabled = randomPracticeEnabled(),
            sessionStartTime = newSessionStartTime,
            preserveCurrentIndex = preserveCurrentIndex
        )
        val prevIndex = sessionState.value.currentIndex
        sessionState.value = sessionState.value.copy(
            questionsWithState = questionsWithState,
            sessionStartTime = newSessionStartTime,
            questionCount = questionCount,
            currentIndex = startIndex
        )
        if (prevIndex != startIndex) {
            PracticeJumpDebugLog.sessionIndexMutation(
                prevIndex,
                startIndex,
                "loadQuestionsForCurrentSource restoreFromMap=$restoreFromMap preserve=$preserveCurrentIndex"
            )
        }
        if (restoreFromMap) {
            onProgressRestored(questionsWithState, startIndex)
        }
        markAppliedInitKey(questionCount, curFillSignature)
        scope.launch { repositoryContentLoader.loadOnce() }
        loadProgress()
    }

    private fun markAppliedInitKey(questionCount: Int, fillConfigVersion: String) {
        lastAppliedInitKey = PracticeQuizInitReloadPipeline.buildInitKey(
            fillConfigVersion = fillConfigVersion,
            practiceCount = questionCount,
            randomPractice = randomPracticeEnabled()
        )
    }

    private suspend fun loadOriginalQuestions(sourceId: String): List<Question> =
        questionFlowCache.preload(sourceId)

    fun loadProgress() {
        loadProgressJob?.cancel()
        loadProgressJob = scope.launch {
            if (!sessionState.value.progressLoaded) {
                sessionState.value = sessionState.value.copy(progressLoaded = true)
            }
        }
    }

    fun saveProgress() {
        navigationSaveScheduler.flushAndSave { performSaveProgress() }
    }

    fun scheduleNavigationSave() {
        navigationSaveScheduler.schedule { performSaveProgress() }
    }

    private suspend fun performSaveProgress() {
        val state = sessionState.value
        val mergedMap = withContext(Dispatchers.Default) {
            PracticeProgressMapPipeline.mergeInto(cumulativeQuestionStateMap, state.questionsWithState)
        }
        cumulativeQuestionStateMap.clear()
        cumulativeQuestionStateMap.putAll(mergedMap)
        val fillSignature = PracticeFillConfigPipeline.read(fontSettings).signature()
        val fixedOrder = state.questionsWithState.map { it.question.id }
        PracticeRoundLoadLog.save(cumulativeQuestionStateMap.size, fixedOrder.size)
        withContext(Dispatchers.IO) {
            sessionEngine.progressManager.saveProgress(
                progressId = progressId(),
                state = state.toUnifiedSessionState(),
                memoryActive = false,
                allSourceQuestions = emptyList(),
                fillSignature = fillSignature,
                extras = mapOf(
                    "questionStateMap" to cumulativeQuestionStateMap.toMap(),
                    "fixedQuestionOrder" to fixedOrder
                )
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

    fun loadReviewSession(
        targetProgressId: String,
        sourceId: String,
        questionCount: Int,
        wrongBook: Boolean = false,
        favorite: Boolean = false,
        onComplete: suspend () -> Unit
    ) {
        setProgressIdValue(ensurePrefix(targetProgressId))
        setQuestionSourceId(sourceId)
        lastQuestionCount = questionCount
        loadQuestionsJob?.cancel()
        loadQuestionsJob = scope.launch {
            val progress = facade.progress.getFlow(progressId()).firstOrNull()
            if (progress == null) {
                sessionState.value = sessionState.value.copy(progressLoaded = true)
                onComplete()
                return@launch
            }
            val sourceQuestions = resolveSourceQuestions(sourceId, wrongBook, favorite)
            loadQuestionsForCurrentSource(questionCount, progress.timestamp, sourceQuestions)
            onComplete()
        }
    }

    private suspend fun resolveSourceQuestions(
        sourceId: String,
        wrongBook: Boolean,
        favorite: Boolean
    ): List<Question>? {
        if (!wrongBook && !favorite) return null
        return when {
            wrongBook -> facade.wrongFavorite.getWrongBook().firstOrNull()
                ?.filter { it.question.fileName == sourceId }
                ?.map { it.question }
                .orEmpty()
            favorite -> facade.wrongFavorite.getFavorites().firstOrNull()
                ?.filter { it.question.fileName == sourceId }
                ?.map { it.question }
                .orEmpty()
            else -> null
        }
    }

    private fun ensurePrefix(id: String): String = if (id.startsWith("practice_")) id else "practice_$id"

    private fun orderedForPractice(
        originalQuestions: List<Question>,
        existingProgress: PracticeProgress?,
        newSessionStartTime: Long
    ): List<Question> {
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
