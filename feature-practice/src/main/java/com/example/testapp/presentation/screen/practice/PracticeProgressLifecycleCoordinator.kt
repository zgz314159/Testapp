package com.example.testapp.presentation.screen.practice

import com.example.testapp.core.common.FontSettingsRepository
import com.example.testapp.core.session.NavigationSaveScheduler
import com.example.testapp.core.session.SessionEngine
import com.example.testapp.core.session.policy.SessionStrategyFactory
import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState
import com.example.testapp.domain.model.toUnifiedSessionState
import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.persistence.SessionPersistenceConfig
import com.example.testapp.domain.session.persistence.SessionPersistenceContext
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
    private var persistenceConfig: SessionPersistenceConfig? = null
    private var pendingPinnedQuestionId: Int? = null

    fun applyPersistenceConfig(config: SessionPersistenceConfig) {
        persistenceConfig = config
    }

    private fun activePersistenceConfig(): SessionPersistenceConfig =
        persistenceConfig ?: SessionStrategyFactory.persistence(
            QuestionSessionKind.Practice(progressId())
        ).config(SessionPersistenceContext())

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
        random: Boolean = randomPracticeEnabled(),
        pinnedQuestionId: Int? = null,
    ) {
        setProgressIdValue(PracticeProgressIdPipeline.ensurePrefix(id))
        setQuestionSourceId(questionsId)
        setRandomPracticeEnabled(random)
        lastQuestionCount = questionCount
        lastAppliedInitKey = null
        pendingPinnedQuestionId = pinnedQuestionId
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

        val fillConfig = PracticeFillConfigPipeline.read(fontSettings)
        onFillConfigApplied(fillConfig)
        var existingProgress = facade.progress.getFlow(progressId()).firstOrNull()
        val pinnedId = pendingPinnedQuestionId
        pendingPinnedQuestionId = null

        when (
            val outcome = PracticeProgressLoadQuestionsPipeline.prepare(
                originalQuestions = originalQuestions,
                existingProgress = existingProgress,
                questionCount = questionCount,
                newSessionStartTime = newSessionStartTime,
                fillConfig = fillConfig,
                sourceId = sourceId,
                randomPracticeEnabled = randomPracticeEnabled(),
                pinnedQuestionId = pinnedId,
                preserveCurrentIndex = preserveCurrentIndex,
                progressCoordinator = progressCoordinator,
                persistenceConfig = activePersistenceConfig(),
                progressId = progressId(),
            )
        ) {
            is PracticeProgressLoadQuestionsPipeline.Outcome.Empty -> {
                sourceCatalogQuestions = emptyList()
                sessionState.value = PracticeSessionState(
                    progressLoaded = true,
                    progressId = outcome.catalog.progressId,
                    sessionStartTime = outcome.catalog.sessionStartTime,
                )
                return
            }
            is PracticeProgressLoadQuestionsPipeline.Outcome.Ready -> {
                val loaded = outcome.loaded
                loaded.fillSignatureUpgrade?.let { facade.progress.save(it) }
                loaded.newRoundProgress?.let { facade.progress.save(it) }
                val existingProgress = loaded.finalProgress
                val applied = PracticeProgressApplyLoadedPipeline.apply(
                    loaded = loaded,
                    existingProgress = existingProgress,
                )
                sourceCatalogQuestions = applied.sourceCatalogQuestions
                cumulativeQuestionStateMap.clear()
                cumulativeQuestionStateMap.putAll(applied.cumulativeQuestionStateMap)
                val log = applied.logContext
                PracticeRoundLoadLog.loadQuestions(
                    priorComplete = log.priorComplete,
                    startNewRound = log.startNewRound,
                    canReuse = log.canReuse,
                    savedSize = log.savedSize,
                    questionCount = log.questionCount,
                    orderedIds = log.orderedIds,
                    answeredSourceIds = log.answeredSourceIds,
                    lastRoundSourceIds = log.lastRoundSourceIds,
                    savedSourcesDone = log.savedSourcesDone,
                )
                PracticeRoundLoadLog.restore(log.restoreFromMap, log.restoredMapSize)
                val prevIndex = sessionState.value.currentIndex
                val nextState = PracticeProgressApplyLoadedPipeline.patchSessionState(
                    sessionState.value,
                    applied,
                )
                sessionState.value = nextState
                if (prevIndex != applied.startIndex) {
                    PracticeJumpDebugLog.sessionIndexMutation(
                        prevIndex,
                        applied.startIndex,
                        "loadQuestionsForCurrentSource restoreFromMap=${applied.restoreFromMap} preserve=$preserveCurrentIndex",
                    )
                }
                if (applied.restoreFromMap) {
                    onProgressRestored(applied.questionsWithState, applied.startIndex)
                }
                lastAppliedInitKey = applied.initKey
                scope.launch { repositoryContentLoader.loadOnce() }
                loadProgress()
            }
        }
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
        if (!PracticeProgressPersistencePipeline.shouldSaveOnNavigation(activePersistenceConfig())) return
        navigationSaveScheduler.schedule { performSaveProgress() }
    }

    private suspend fun performSaveProgress() {
        if (!PracticeProgressPersistencePipeline.shouldPersist(activePersistenceConfig())) return
        val state = sessionState.value
        val mergedMap = withContext(Dispatchers.Default) {
            PracticeProgressSaveRequestPipeline.mergeMap(cumulativeQuestionStateMap, state.questionsWithState)
        }
        cumulativeQuestionStateMap.clear()
        cumulativeQuestionStateMap.putAll(mergedMap)
        val fillSignature = PracticeFillConfigPipeline.read(fontSettings).signature()
        val request = PracticeProgressSaveRequestPipeline.build(
            state = state,
            cumulativeQuestionStateMap = cumulativeQuestionStateMap.toMap(),
            fillSignature = fillSignature,
            unifiedState = state.toUnifiedSessionState(),
        )
        PracticeRoundLoadLog.save(request.logMapSize, request.logFixedOrderSize)
        withContext(Dispatchers.IO) {
            sessionEngine.progressManager.saveProgress(
                progressId = progressId(),
                state = request.unifiedState,
                memoryActive = false,
                allSourceQuestions = emptyList(),
                fillSignature = request.fillSignature,
                extras = request.extras,
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
        setProgressIdValue(PracticeProgressIdPipeline.ensurePrefix(targetProgressId))
        setQuestionSourceId(sourceId)
        lastQuestionCount = questionCount
        loadQuestionsJob?.cancel()
        loadQuestionsJob = scope.launch {
            val progress = facade.progress.getFlow(progressId()).firstOrNull()
            val sourceQuestions = resolveSourceQuestions(sourceId, wrongBook, favorite)
            when (
                val outcome = PracticeReviewSessionLoadPipeline.resolve(progress, sourceQuestions)
            ) {
                PracticeReviewSessionLoadPipeline.Outcome.MarkLoadedOnly -> {
                    sessionState.value = sessionState.value.copy(progressLoaded = true)
                    onComplete()
                }
                is PracticeReviewSessionLoadPipeline.Outcome.LoadQuestions -> {
                    loadQuestionsForCurrentSource(
                        questionCount,
                        outcome.sessionStartTime,
                        outcome.sourceQuestions,
                    )
                    onComplete()
                }
            }
        }
    }

    private suspend fun resolveSourceQuestions(
        sourceId: String,
        wrongBook: Boolean,
        favorite: Boolean
    ): List<Question>? {
        if (!wrongBook && !favorite) return null
        val wrongQuestions = facade.wrongFavorite.getWrongBook().firstOrNull()
            ?.map { it.question }
            .orEmpty()
        val favoriteQuestions = facade.wrongFavorite.getFavorites().firstOrNull()
            ?.map { it.question }
            .orEmpty()
        return PracticeReviewSourceQuestionsPipeline.filterBySource(
            sourceId = sourceId,
            wrongBook = wrongBook,
            favorite = favorite,
            wrongBookQuestions = wrongQuestions,
            favoriteQuestions = favoriteQuestions,
        )
    }

    private fun resetLocalState() {
        sessionState.value = PracticeProgressLocalResetPipeline.resetQuestions(sessionState.value)
    }
}
