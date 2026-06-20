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

    private suspend fun loadCore(originalQuestions: List<Question>, count: Int, random: Boolean) {
        val pid = progressIdRef!!()
        var progress: ExamProgress? = getExamProgressFlowUseCase(pid).firstOrNull()
        val seed = progress?.timestamp ?: System.currentTimeMillis()
        setProgressSeedFn!!(seed)
        val fillSig = fillTransform.currentFillConfigSignature()
        val genMode = fontSettings.fillQuestionGenerationMode.firstOrNull()
            ?: FillQuestionGenerationMode.SCORE_RANGE_RANDOM

        if (progress?.finished == true) { /* 保留已批改进度，重入时恢复 */ }

        val canReuse = fillTransform.canReuseByFillSignature(progress?.sessionId, fillSig, fillTransform.isFillConfigSensitive(originalQuestions))
        val savedSig = fillTransform.extractFillConfigSignature(progress?.sessionId)
        if (progress != null && savedSig.isNullOrBlank() && canReuse) {
            progress = progress.copy(sessionId = fillTransform.buildSessionIdWithFillSignature(pid, progress.timestamp, fillSig))
            saveExamProgressUseCase(progress)
        }

        val expectedCount = if (count > 0) count.coerceAtMost(originalQuestions.size) else originalQuestions.size
        val expectedSeq = if (count > 0) originalQuestions.take(expectedCount).map { it.id } else originalQuestions.map { it.id }
        val savedOrder = progress?.fixedQuestionOrder?.let { order ->
            if (genMode == FillQuestionGenerationMode.FULL_ANSWER) order.map(::extractSourceQuestionId).distinct() else order
        }?.takeIf { canReuse }.orEmpty()

        val finalQuestions = if (navigationHelper.shouldReuseSavedSourceOrder(savedOrder, expectedCount, expectedSeq, random)) {
            val map = originalQuestions.associateBy { it.id }; savedOrder.mapNotNull { map[it] }
        } else {
            val ordered = if (random && progress != null) {
                val answeredIds = progress.questionStateMap.keys.toSet()
                val un = originalQuestions.filter { it.id !in answeredIds }
                val an = originalQuestions.filter { it.id in answeredIds }
                if (un.isNotEmpty()) un.shuffled(java.util.Random(seed)) + an.shuffled(java.util.Random(seed + 1000))
                else originalQuestions.shuffled(java.util.Random(seed))
            } else if (random) originalQuestions.shuffled(java.util.Random(seed))
            else originalQuestions
            val limited = if (count > 0) ordered.take(count.coerceAtMost(ordered.size)) else ordered
            saveExamProgressUseCase(ExamProgress(id = pid, currentIndex = 0, selectedOptions = emptyList(),
                showResultList = emptyList(), analysisList = emptyList(), sparkAnalysisList = emptyList(),
                baiduAnalysisList = emptyList(), noteList = emptyList(), finished = false, timestamp = seed,
                sessionId = fillTransform.buildSessionIdWithFillSignature(pid, seed, fillSig),
                fixedQuestionOrder = limited.map { it.id },
                questionStateMap = if (progress?.finished == true) progress.questionStateMap else emptyMap()))
            limited
        }

        val shuffled = navigationHelper.shuffleOptionsIfNeeded(finalQuestions, random, seed)

        if (memoryModeActiveRef!!()) {
            persistentQuestionStateMap!!.clear()
            persistentQuestionStateMap!!.putAll(progress?.questionStateMap.orEmpty())
            if (onInitMemoryModeFn!!(seed)) return
        }

        val configured = fillTransform.applyConfiguredFillQuestions(shuffled, seed, setFullAnswerRequireCorrectFn!!) { }
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
}
