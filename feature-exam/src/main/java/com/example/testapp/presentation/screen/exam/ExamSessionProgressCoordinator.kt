package com.example.testapp.presentation.screen.exam

import com.example.testapp.core.common.LocalizedResult
import com.example.testapp.core.session.SessionEngine
import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.SessionMode
import com.example.testapp.domain.model.UnifiedQuestionState
import com.example.testapp.domain.model.UnifiedSessionState
import com.example.testapp.domain.usecase.ExamUseCaseFacade
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Unified-session progress save/load for [ExamViewModel]. */
internal class ExamSessionProgressCoordinator(
    private val sessionState: MutableStateFlow<PracticeSessionState>,
    private val scope: CoroutineScope,
    private val sessionEngine: SessionEngine,
    private val facade: ExamUseCaseFacade,
    private val fillTransform: ExamFillTransform,
    private val navHelper: ExamNavigationHelper,
    private val persistentQuestionStateMap: MutableMap<Int, UnifiedQuestionState>,
    private val progressId: () -> String,
    private val progressSeedRef: () -> Long,
    private val setProgressSeed: (Long) -> Unit,
    private val randomExamEnabled: () -> Boolean,
    private val memoryModeActive: () -> Boolean,
    private val allSourceQuestions: () -> List<com.example.testapp.domain.model.Question>,
    private val reviewModeActive: () -> Boolean,
    private val messageResult: MutableStateFlow<LocalizedResult?>,
    private val currentFullAnswerCandidateIndices: (List<Int>) -> List<Int>,
    private val onCalculateCumulativeStats: () -> Unit,
    private val onLoadAnalysis: suspend () -> Unit,
    private val onLoadSparkAnalysis: suspend () -> Unit,
    private val onLoadBaiduAnalysis: suspend () -> Unit,
    private val onLoadNotes: suspend () -> Unit,
    private val isAnalysisLoaded: () -> Boolean,
    private val isSparkAnalysisLoaded: () -> Boolean,
    private val isBaiduAnalysisLoaded: () -> Boolean,
    private val isNotesLoaded: () -> Boolean,
    private val markAnalysisLoaded: () -> Unit,
    private val markSparkAnalysisLoaded: () -> Unit,
    private val markBaiduAnalysisLoaded: () -> Unit,
    private val markNotesLoaded: () -> Unit
) {
    fun mergeCurrentStateToPersistentMap() {
        val s = sessionState.value
        persistentQuestionStateMap.putAll(
            navHelper.buildCurrentStateMapByQuestionId(
                s.questions,
                s.questionsWithState.map { it.selectedOptions },
                s.questionsWithState.map { it.textAnswer },
                s.questionsWithState.map { it.showResult },
                s.questionsWithState.map { it.analysis },
                s.questionsWithState.map { it.sparkAnalysis },
                s.questionsWithState.map { it.baiduAnalysis },
                s.questionsWithState.map { it.note }
            )
        )
    }

    suspend fun saveProgressInternal() = withContext(Dispatchers.Default) {
        mergeCurrentStateToPersistentMap()
        val fs = fillTransform.currentFillConfigSignature()
        val s = sessionState.value
        val fo = s.questionsWithState.map { it.question.id }
        val ffo = if (memoryModeActive() && allSourceQuestions().isNotEmpty()) {
            allSourceQuestions().map { it.id }
        } else {
            fo
        }
        val fsm = persistentQuestionStateMap.toMap()
        val state = UnifiedSessionState(
            questionsWithState = s.questionsWithState,
            currentIndex = s.currentIndex,
            sessionStartTime = s.sessionStartTime,
            mode = SessionMode.EXAM,
            progressId = progressId(),
            questionsSource = "",
            isRandomMode = randomExamEnabled(),
            progressLoaded = s.progressLoaded,
            finished = s.finished,
            sessionId = fillTransform.buildSessionIdWithFillSignature(progressId(), progressSeedRef(), fs)
        )
        ExamPipelineLog.save(s.questionsWithState.map { it.sessionAnswerTime })
        sessionEngine.progressManager.saveProgress(
            progressId = progressId(),
            state = state,
            memoryActive = memoryModeActive(),
            allSourceQuestions = allSourceQuestions(),
            fillSignature = fs,
            extras = mapOf("questionStateMap" to fsm, "fixedQuestionOrder" to ffo)
        )
        ExamRoundLoadLog.save(mapSize = fsm.size, finished = s.finished, fixedOrderSize = ffo.size)
        messageResult.value = LocalizedResult(com.example.testapp.domain.IOConstants.SAVE_SUCCESS)
    }

    fun loadProgress(onSaveProgress: () -> Unit) {
        scope.launch {
            facade.progress.getFlow(progressId()).collect { progress ->
                val s = sessionState.value
                if (progress != null && !s.progressLoaded) {
                    val size = s.questionsWithState.size
                    if (size == 0) return@collect
                    setProgressSeed(progress.timestamp)
                    val seed = progress.timestamp
                    val nci = progress.currentIndex.coerceAtMost(size - 1)
                    val sci = if (randomExamEnabled() && !progress.finished) {
                        val cand = currentFullAnswerCandidateIndices(
                            s.questionsWithState.indices.filter { it != nci }
                        )
                        if (cand.isNotEmpty()) cand.random(kotlin.random.Random(seed)) else nci
                    } else {
                        nci
                    }
                    val restoreFromMap = ExamSessionRestorePipeline.shouldRestoreAnswersFromStateMap(
                        progress = progress,
                        reviewMode = reviewModeActive()
                    )
                    ExamRoundLoadLog.restore(
                        restoreFromMap = restoreFromMap,
                        reviewMode = reviewModeActive(),
                        progressFinished = progress.finished,
                        mapSize = progress.questionStateMap.size
                    )
                    persistentQuestionStateMap.clear()
                    persistentQuestionStateMap.putAll(progress.questionStateMap)
                    val qws = ExamSessionRestorePipeline.resolveSessionQuestions(
                        sessionQuestions = s.questionsWithState,
                        progress = progress,
                        restoreFromMap = restoreFromMap
                    )
                    ExamPipelineLog.load(qws.map { it.sessionAnswerTime })
                    val resumeIndex = if (randomExamEnabled() && s.questionsWithState.isNotEmpty() && !restoreFromMap) {
                        (0 until s.questionsWithState.size).random(kotlin.random.Random(seed))
                    } else if (restoreFromMap) {
                        sci
                    } else {
                        0
                    }
                    sessionState.value = s.copy(
                        questionsWithState = qws,
                        currentIndex = resumeIndex,
                        finished = false
                    )
                    if (progress.selectedOptions.size < size) saveProgressInternal()
                } else if (progress == null && !s.progressLoaded) {
                    val nci = if (randomExamEnabled() && s.questionsWithState.isNotEmpty()) {
                        (0 until s.questionsWithState.size).random(kotlin.random.Random(progressSeedRef()))
                    } else {
                        0
                    }
                    sessionState.update { it.copy(currentIndex = nci) }
                    onSaveProgress()
                }
                sessionState.update { it.copy(progressLoaded = true) }
                onCalculateCumulativeStats()
                if (!isAnalysisLoaded()) {
                    onLoadAnalysis()
                    markAnalysisLoaded()
                }
                if (!isSparkAnalysisLoaded()) {
                    onLoadSparkAnalysis()
                    markSparkAnalysisLoaded()
                }
                if (!isBaiduAnalysisLoaded()) {
                    onLoadBaiduAnalysis()
                    markBaiduAnalysisLoaded()
                }
                if (!isNotesLoaded()) {
                    onLoadNotes()
                    markNotesLoaded()
                }
            }
        }
    }
}
