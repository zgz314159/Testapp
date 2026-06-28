package com.example.testapp.presentation.screen.exam

import com.example.testapp.core.common.FontSettingsRepository
import com.example.testapp.core.common.buildExamProgressId
import com.example.testapp.domain.model.ExamProgress
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.usecase.*
import com.example.testapp.core.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExamLoadDelegate @Inject constructor(
    private val fontSettings: FontSettingsRepository,
    private val answerRules: ExamAnswerRules,
    private val fillTransform: ExamFillTransform,
    private val memoryModeEngine: ExamMemoryModeEngine,
    private val navigationHelper: ExamNavigationHelper,
    private val getQuestionsUseCase: GetQuestionsUseCase,
    private val getWrongBookUseCase: GetWrongBookUseCase,
    private val getFavoriteQuestionsUseCase: GetFavoriteQuestionsUseCase,
    private val saveExamProgressUseCase: SaveExamProgressUseCase,
    private val getExamProgressFlowUseCase: GetExamProgressFlowUseCase,
    private val clearExamProgressUseCase: ClearExamProgressUseCase,
    private val getQuestionAnalysisUseCase: GetQuestionAnalysisUseCase,
    private val getSparkAnalysisUseCase: GetSparkAnalysisUseCase,
    private val getBaiduAnalysisUseCase: GetBaiduAnalysisUseCase,
    private val getQuestionNoteUseCase: GetQuestionNoteUseCase
) {
    private var scope: CoroutineScope? = null
    private var progressIdRef: (() -> String)? = null
    private var setProgressIdFn: ((String) -> Unit)? = null
    private var progressSeedRef: (() -> Long)? = null
    private var setProgressSeedFn: ((Long) -> Unit)? = null
    private var setFullAnswerRequireCorrectFn: ((Boolean) -> Unit)? = null
    private var onFillConfigAppliedFn: ((ExamFillConfig) -> Unit)? = null
    private var memoryModeActiveRef: (() -> Boolean)? = null
    private var setMemoryModeActiveFn: ((Boolean) -> Unit)? = null
    private var memoryModeEnabledRef: (() -> Boolean)? = null
    private var memoryModeBatchSizeRef: (() -> Int)? = null
    private var memoryWrongModeRef: (() -> Int)? = null
    private var memoryPoolModeRef: (() -> Int)? = null
    private var allSourceQuestionsRef: (() -> List<Question>)? = null
    private var setAllSourceQuestionsFn: ((List<Question>) -> Unit)? = null
    private var setCurrentMemoryRoundQuestionIdsFn: ((Set<Int>) -> Unit)? = null
    private var persistentQuestionStateMap: MutableMap<Int, com.example.testapp.domain.model.UnifiedQuestionState>? = null

    private var onQuestionsFn: ((List<Question>) -> Unit)? = null
    private var onProgressLoadedFn: ((Boolean) -> Unit)? = null
    private var onPostArtifactsFn: ((ExamNavigationHelper.QuadLists) -> Unit)? = null
    private var onInitMemoryModeFn: (suspend (Long) -> Boolean)? = null
    private var onLoadProgressFn: (() -> Unit)? = null

    @Suppress("LongParameterList")
    fun init(
        vmScope: CoroutineScope,
        progressIdRef: () -> String, setProgressId: (String) -> Unit,
        progressSeedRef: () -> Long, setProgressSeed: (Long) -> Unit,
        setFullAnswerRequireCorrect: (Boolean) -> Unit,
        onFillConfigApplied: (ExamFillConfig) -> Unit,
        memoryModeActiveRef: () -> Boolean, setMemoryModeActive: (Boolean) -> Unit,
        memoryModeEnabledRef: () -> Boolean, memoryModeBatchSizeRef: () -> Int,
        memoryWrongModeRef: () -> Int, memoryPoolModeRef: () -> Int,
        allSourceQuestionsRef: () -> List<Question>, setAllSourceQuestions: (List<Question>) -> Unit,
        setCurrentMemoryRoundQuestionIds: (Set<Int>) -> Unit,
        persistentQuestionStateMap: MutableMap<Int, com.example.testapp.domain.model.UnifiedQuestionState>,
        onQuestions: (List<Question>) -> Unit,
        onProgressLoaded: (Boolean) -> Unit,
        onPostArtifacts: (ExamNavigationHelper.QuadLists) -> Unit,
        onInitMemoryMode: suspend (Long) -> Boolean,
        onLoadProgress: () -> Unit
    ) {
        this.scope = vmScope
        this.progressIdRef = progressIdRef; this.setProgressIdFn = setProgressId
        this.progressSeedRef = progressSeedRef; this.setProgressSeedFn = setProgressSeed
        this.setFullAnswerRequireCorrectFn = setFullAnswerRequireCorrect
        this.onFillConfigAppliedFn = onFillConfigApplied
        this.memoryModeActiveRef = memoryModeActiveRef; this.setMemoryModeActiveFn = setMemoryModeActive
        this.memoryModeEnabledRef = memoryModeEnabledRef; this.memoryModeBatchSizeRef = memoryModeBatchSizeRef
        this.memoryWrongModeRef = memoryWrongModeRef; this.memoryPoolModeRef = memoryPoolModeRef
        this.allSourceQuestionsRef = allSourceQuestionsRef; this.setAllSourceQuestionsFn = setAllSourceQuestions
        this.setCurrentMemoryRoundQuestionIdsFn = setCurrentMemoryRoundQuestionIds
        this.persistentQuestionStateMap = persistentQuestionStateMap
        this.onQuestionsFn = onQuestions; this.onProgressLoadedFn = onProgressLoaded
        this.onPostArtifactsFn = onPostArtifacts; this.onInitMemoryModeFn = onInitMemoryMode
        this.onLoadProgressFn = onLoadProgress
    }

    private fun setupLoadState(baseId: String, count: Int, random: Boolean, isMemory: Boolean) {
        setMemoryModeActiveFn!!(isMemory)
        setProgressIdFn!!(buildExamProgressId(baseId, count, random, isMemory,
            memoryModeBatchSizeRef!!(), memoryWrongModeRef!!(), memoryPoolModeRef!!()))
        if (!isMemory) {
            setAllSourceQuestionsFn!!(emptyList()); persistentQuestionStateMap!!.clear()
            setCurrentMemoryRoundQuestionIdsFn!!(emptySet())
        }
        onProgressLoadedFn!!(false)
    }

    private var lastLoadParams: Triple<List<Question>, Int, Boolean>? = null

    fun reloadForFillConfig() {
        val (src, count, random) = lastLoadParams ?: return
        if (src.isEmpty()) return
        scope!!.launch { loadCore(src, count, random) }
    }

    private suspend fun loadCore(
        originalQuestions: List<Question>,
        count: Int,
        random: Boolean,
        preserveFinishedProgress: Boolean = false
    ) {
        lastLoadParams = Triple(originalQuestions, count, random)
        val pid = progressIdRef!!()
        var progress: ExamProgress? = getExamProgressFlowUseCase(pid).firstOrNull()
        val priorFinished = progress?.finished == true
        val priorComplete = ExamRoundCompletePipeline.isComplete(progress)
        val savedSourceOrderEarly = progress?.fixedQuestionOrder
            ?.map(::extractSourceQuestionId)
            ?.distinct()
            .orEmpty()
        val savedSourcesDone = ExamSourceQuestionPipeline.savedSourcesFullyAnswered(
            savedSourceOrderEarly,
            progress?.questionStateMap.orEmpty()
        )
        val startNewRound = (priorComplete || savedSourcesDone) && !preserveFinishedProgress
        val seed = if (startNewRound) {
            System.currentTimeMillis()
        } else {
            progress?.timestamp ?: System.currentTimeMillis()
        }
        setProgressSeedFn!!(seed)
        if (!memoryModeActiveRef!!()) {
            persistentQuestionStateMap!!.clear()
            persistentQuestionStateMap!!.putAll(progress?.questionStateMap.orEmpty())
        }
        val fillSig = fillTransform.currentFillConfigSignature()
        val genMode = fontSettings.fillQuestionGenerationMode.firstOrNull()
            ?: FillQuestionGenerationMode.SCORE_RANGE_RANDOM

        val canReuseByFill = fillTransform.canReuseByFillSignature(progress?.sessionId, fillSig, fillTransform.isFillConfigSensitive(originalQuestions))
        val savedSig = fillTransform.extractFillConfigSignature(progress?.sessionId)
        if (progress != null && savedSig.isNullOrBlank() && canReuseByFill) {
            progress = progress.copy(sessionId = fillTransform.buildSessionIdWithFillSignature(pid, progress.timestamp, fillSig))
            saveExamProgressUseCase(progress)
        }

        val fullPoolSize = originalQuestions.size
        val expectedCount = ExamQuestionCountPolicy.expectedCount(count, fullPoolSize)
        val expectedSeq = if (count > 0) originalQuestions.take(expectedCount).map { it.id } else originalQuestions.map { it.id }
        val savedSourceOrder = progress?.fixedQuestionOrder?.let { order ->
            if (genMode == FillQuestionGenerationMode.FULL_ANSWER) order.map(::extractSourceQuestionId).distinct() else order
        }?.takeIf { canReuseByFill }.orEmpty()

        val canReuseSavedOrder = if (preserveFinishedProgress && priorFinished) {
            savedSourceOrder.isNotEmpty() && canReuseByFill
        } else {
            ExamRoundReusePipeline.canReuseSavedOrder(
                progress = progress,
                savedSourceOrder = savedSourceOrder,
                questionCount = count,
                fullPoolSize = fullPoolSize,
                expectedSeq = expectedSeq,
                random = random,
                canReuseByFill = canReuseByFill
            )
        }

        val sessionId = fillTransform.buildSessionIdWithFillSignature(pid, seed, fillSig)
        val answeredSourceIdsForLog = ExamSourceQuestionPipeline.answeredSourceIds(progress?.questionStateMap.orEmpty())
        val lastRoundSourceIdsForLog = ExamSourceQuestionPipeline.lastRoundSourceIds(progress?.fixedQuestionOrder.orEmpty())
        val finalQuestions = if (canReuseSavedOrder) {
            val map = originalQuestions.associateBy { it.id }
            savedSourceOrder.mapNotNull { map[it] }
        } else {
            val lastRoundSourceIds = if (startNewRound) {
                ExamSourceQuestionPipeline.lastRoundSourceIds(progress?.fixedQuestionOrder.orEmpty())
            } else {
                emptySet()
            }
            val answeredSourceIds = ExamQuestionOrderPipeline.answeredQuestionIds(progress?.questionStateMap.orEmpty())
            val ordered = ExamQuestionOrderPipeline.orderForNewRound(
                originalQuestions = originalQuestions,
                questionCount = count,
                random = random,
                answeredSourceIds = answeredSourceIds,
                seed = seed,
                lastRoundSourceIds = lastRoundSourceIds
            )
            val newProgress = ExamQuestionOrderPipeline.buildNewRoundProgress(
                prior = progress,
                progressId = pid,
                seed = seed,
                sessionId = sessionId,
                questions = ordered
            )
            saveExamProgressUseCase(newProgress)
            progress = newProgress
            ordered
        }

        ExamRoundLoadLog.loadCore(
            priorComplete = priorComplete,
            startNewRound = startNewRound,
            canReuseSavedOrder = canReuseSavedOrder,
            finished = progress?.finished == true,
            savedOrderSize = savedSourceOrder.size,
            questionCount = count,
            orderedIds = finalQuestions.map { it.id },
            answeredSourceIds = answeredSourceIdsForLog,
            lastRoundSourceIds = if (startNewRound) lastRoundSourceIdsForLog else emptySet(),
            savedSourcesDone = savedSourcesDone
        )

        val shuffled = navigationHelper.shuffleOptionsIfNeeded(finalQuestions, random, seed)

        if (memoryModeActiveRef!!()) {
            persistentQuestionStateMap!!.clear()
            persistentQuestionStateMap!!.putAll(progress?.questionStateMap.orEmpty())
            if (onInitMemoryModeFn!!(seed)) return
        }

        val configured = fillTransform.applyConfiguredFillQuestions(shuffled, seed, { config ->
            setFullAnswerRequireCorrectFn!!(config.fullAnswerRequireCorrect)
            onFillConfigAppliedFn!!(config)
        }) { }
        if (configured.isNotEmpty() && progress?.finished != true) {
            val variantOrder = configured.map { it.id }
            val needsVariantOrder = progress == null ||
                progress.fixedQuestionOrder != variantOrder ||
                !canReuseByFill
            if (needsVariantOrder) {
                saveExamProgressUseCase(
                    ExamProgress(
                        id = pid,
                        currentIndex = progress?.currentIndex ?: 0,
                        selectedOptions = progress?.selectedOptions.orEmpty(),
                        showResultList = progress?.showResultList.orEmpty(),
                        analysisList = progress?.analysisList.orEmpty(),
                        sparkAnalysisList = progress?.sparkAnalysisList.orEmpty(),
                        baiduAnalysisList = progress?.baiduAnalysisList.orEmpty(),
                        noteList = progress?.noteList.orEmpty(),
                        finished = false,
                        timestamp = seed,
                        sessionId = sessionId,
                        fixedQuestionOrder = variantOrder,
                        questionStateMap = if (canReuseByFill) progress?.questionStateMap.orEmpty() else emptyMap()
                    )
                )
                progress = getExamProgressFlowUseCase(pid).firstOrNull()
            }
        }
        onQuestionsFn!!(configured)
        val artifacts = navigationHelper.preloadStoredArtifacts(configured, progress,
            { getQuestionAnalysisUseCase(it).getOrNull().orEmpty() },
            { getSparkAnalysisUseCase(it).getOrNull().orEmpty() },
            { getBaiduAnalysisUseCase(it).getOrNull().orEmpty() },
            { getQuestionNoteUseCase(it).getOrNull().orEmpty() })
        onPostArtifactsFn!!(artifacts)
        onLoadProgressFn!!()
    }

    fun loadNormalExam(quizId: String, count: Int, random: Boolean) {
        val isMem = memoryModeEngine.shouldUseMemoryMode(memoryModeEnabledRef!!(), quizId)
        setupLoadState(quizId, count, random, isMem)
        scope!!.launch {
            val src = getQuestionsUseCase(quizId).firstOrNull().orEmpty()
            if (src.isEmpty()) { onQuestionsFn!!(emptyList()); onProgressLoadedFn!!(true); return@launch }
            setAllSourceQuestionsFn!!(src)
            loadCore(src, count, random)
        }
    }

    fun loadWrongExam(fileName: String, count: Int, random: Boolean) {
        setupLoadState(fileName, count, random, false)
        scope!!.launch {
            getWrongBookUseCase().collect { wrongList ->
                val src = wrongList.filter { it.question.fileName == fileName }.map { it.question }
                if (src.isEmpty()) { onQuestionsFn!!(emptyList()); onProgressLoadedFn!!(true); return@collect }
                setAllSourceQuestionsFn!!(src)
                loadCore(src, count, random)
            }
        }
    }

    fun loadFavoriteExam(fileName: String, count: Int, random: Boolean) {
        setupLoadState(fileName, count, random, false)
        scope!!.launch {
            getFavoriteQuestionsUseCase().collect { favList ->
                val src = favList.filter { it.question.fileName == fileName }.map { it.question }
                if (src.isEmpty()) { onQuestionsFn!!(emptyList()); onProgressLoadedFn!!(true); return@collect }
                setAllSourceQuestionsFn!!(src)
                loadCore(src, count, random)
            }
        }
    }

    fun loadReviewSession(
        targetProgressId: String,
        quizFile: String,
        count: Int,
        random: Boolean,
        wrongBook: Boolean = false,
        favorite: Boolean = false
    ) {
        scope!!.launch {
            setProgressIdFn!!(targetProgressId)
            val progress = getExamProgressFlowUseCase(targetProgressId).firstOrNull()
            if (progress == null) {
                onQuestionsFn!!(emptyList())
                onProgressLoadedFn!!(true)
                return@launch
            }
            setProgressSeedFn!!(progress.timestamp)
            val src = when {
                wrongBook -> getWrongBookUseCase().firstOrNull()
                    ?.filter { it.question.fileName == quizFile }
                    ?.map { it.question }
                    .orEmpty()
                favorite -> getFavoriteQuestionsUseCase().firstOrNull()
                    ?.filter { it.question.fileName == quizFile }
                    ?.map { it.question }
                    .orEmpty()
                else -> getQuestionsUseCase(quizFile).firstOrNull().orEmpty()
            }
            if (src.isEmpty()) {
                onQuestionsFn!!(emptyList())
                onProgressLoadedFn!!(true)
                return@launch
            }
            setAllSourceQuestionsFn!!(src)
            loadCore(src, count, random, preserveFinishedProgress = true)
        }
    }
}
