package com.example.testapp.presentation.screen.exam

import com.example.testapp.domain.model.ExamProgress
import com.example.testapp.core.common.LocalizedResult
import com.example.testapp.domain.model.UnifiedQuestionState
import com.example.testapp.domain.usecase.*
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExamProgressCoordinator @Inject constructor(
    private val state: ExamState,
    private val navHelper: ExamNavigationHelper,
    private val fillTransform: ExamFillTransform,
    private val saveExamProgressUseCase: SaveExamProgressUseCase,
    private val clearExamProgressUseCase: ClearExamProgressUseCase,
    private val getExamProgressFlowUseCase: GetExamProgressFlowUseCase,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun mergeCurrentStateToPersistentMap() {
        state.persistentQuestionStateMap.putAll(
            navHelper.buildCurrentStateMapByQuestionId(
                state._questions.value, state._selectedOptions.value, state._textAnswers.value,
                state._showResultList.value, state._analysisList.value, state._sparkAnalysisList.value,
                state._baiduAnalysisList.value, state._noteList.value
            )
        )
    }

    suspend fun saveProgressInternal() = withContext(Dispatchers.Default) {
        mergeCurrentStateToPersistentMap()
        val fs = fillTransform.currentFillConfigSignature()
        val qs = state._questions.value; val sel = state._selectedOptions.value; val txt = state._textAnswers.value
        val sr = state._showResultList.value; val an = state._analysisList.value
        val sp = state._sparkAnalysisList.value; val bd = state._baiduAnalysisList.value
        val nt = state._noteList.value
        val sm = mutableMapOf<Int, UnifiedQuestionState>(); val fo = mutableListOf<Int>()
        qs.forEachIndexed { i, q ->
            fo.add(q.id); sm[q.id] = UnifiedQuestionState(q.id,
                sel.getOrElse(i) { emptyList() }, txt.getOrElse(i) { "" },
                sr.getOrElse(i) { false }, an.getOrElse(i) { "" },
                sp.getOrElse(i) { "" }, bd.getOrElse(i) { "" }, nt.getOrElse(i) { "" }
            )
        }
        val fsm = if (state.memoryModeActive) state.persistentQuestionStateMap.toMap() else sm
        val ffo = if (state.memoryModeActive && state.allSourceQuestions.isNotEmpty())
            state.allSourceQuestions.map { it.id } else fo
        saveExamProgressUseCase(ExamProgress(
            id = state.progressId, currentIndex = state._currentIndex.value,
            selectedOptions = sel, showResultList = sr,
            analysisList = an, sparkAnalysisList = sp, baiduAnalysisList = bd, noteList = nt,
            finished = state._finished.value, timestamp = state.progressSeed,
            sessionId = fillTransform.buildSessionIdWithFillSignature(state.progressId, state.progressSeed, fs),
            fixedQuestionOrder = ffo, questionStateMap = fsm
        ))
        state._messageResult.value = LocalizedResult(com.example.testapp.domain.IOConstants.SAVE_SUCCESS)
    }

    fun saveProgress() { scope.launch { saveProgressInternal() } }

    fun loadProgress(
        currentFullAnswerCandidateIndices: (List<Int>) -> List<Int>,
        onStats: () -> Unit,
        onLoadAnalysis: () -> Unit,
        onLoadSparkAnalysis: () -> Unit,
        onLoadBaiduAnalysis: () -> Unit,
        onLoadNotes: () -> Unit
    ) {
        scope.launch {
            getExamProgressFlowUseCase(state.progressId).collect { progress ->
                if (progress != null && !state._progressLoaded.value) {
                    val size = state._questions.value.size; if (size == 0) return@collect
                    state.progressSeed = progress.timestamp
                    val nci = progress.currentIndex.coerceAtMost(size - 1)
                    val sci = if (state.randomExamEnabled && !progress.finished) {
                        val cand = currentFullAnswerCandidateIndices(state._questions.value.indices.filter { it != nci })
                        if (cand.isNotEmpty()) cand.random(kotlin.random.Random(state.progressSeed)) else nci
                    } else nci
                    state._currentIndex.value = sci
                    if (progress.questionStateMap.isNotEmpty()) {
                        val sel = MutableList(size) { emptyList<Int>() }; val txt = MutableList(size) { "" }
                        val sr = MutableList(size) { false }; val an = MutableList(size) { "" }
                        val sp = MutableList(size) { "" }; val bd = MutableList(size) { "" }
                        val nt = MutableList(size) { "" }
                        state._questions.value.forEachIndexed { i, q ->
                            progress.questionStateMap[q.id]?.let { s ->
                                sel[i] = s.selectedOptions; txt[i] = s.textAnswer
                                sr[i] = if (progress.finished) s.selectedOptions.isNotEmpty() || s.textAnswer.isNotBlank() else s.showResult
                                an[i] = s.analysis; sp[i] = s.sparkAnalysis; bd[i] = s.baiduAnalysis
                                nt[i] = s.note.takeIf { it.isNotBlank() } ?: state._noteList.value.getOrNull(i).orEmpty()
                            }
                        }
                        state._selectedOptions.value = sel; state._textAnswers.value = txt
                        state._showResultList.value = sr; state._analysisList.value = an
                        state._sparkAnalysisList.value = sp; state._baiduAnalysisList.value = bd
                        state._noteList.value = nt
                    } else {
                        var changed = false
                        val sel = if (progress.selectedOptions.size >= size) progress.selectedOptions.take(size)
                            else { changed = true; progress.selectedOptions + List(size - progress.selectedOptions.size) { emptyList() } }
                        state._selectedOptions.value = sel; state._textAnswers.value = List(size) { "" }
                        val shr = if (progress.showResultList.size >= size) progress.showResultList.take(size)
                            else { changed = true; progress.showResultList + List(size - progress.showResultList.size) { false } }
                        val fshr = shr.toMutableList()
                        if (progress.finished) sel.forEachIndexed { i, s -> if (s.isNotEmpty()) fshr[i] = true }
                        else sel.forEachIndexed { i, s -> if (s.isNotEmpty()) fshr[i] = shr.getOrElse(i) { false } else fshr[i] = false }
                        state._showResultList.value = fshr
                        state._analysisList.value = if (progress.analysisList.size >= size) progress.analysisList.take(size)
                            else { changed = true; progress.analysisList + List(size - progress.analysisList.size) { "" } }
                        state._sparkAnalysisList.value = if (progress.sparkAnalysisList.size >= size) progress.sparkAnalysisList.take(size)
                            else { changed = true; progress.sparkAnalysisList + List(size - progress.sparkAnalysisList.size) { "" } }
                        state._baiduAnalysisList.value = if (progress.baiduAnalysisList.size >= size) progress.baiduAnalysisList.take(size)
                            else { changed = true; progress.baiduAnalysisList + List(size - progress.baiduAnalysisList.size) { "" } }
                        state._noteList.value = (if (progress.noteList.size >= size) progress.noteList.take(size)
                            else { changed = true; progress.noteList + List(size - progress.noteList.size) { "" } })
                            .mapIndexed { i, n -> n.takeIf { it.isNotBlank() } ?: state._noteList.value.getOrNull(i).orEmpty() }
                        if (changed) saveProgressInternal()
                    }
                    state._finished.value = progress.finished
                } else if (progress == null && !state._progressLoaded.value) {
                    state._currentIndex.value = if (state.randomExamEnabled && state._questions.value.isNotEmpty())
                        (0 until state._questions.value.size).random(kotlin.random.Random(state.progressSeed)) else 0
                    saveProgress()
                }
                state._progressLoaded.value = true; onStats()
                if (!state.analysisLoaded) { onLoadAnalysis(); state.analysisLoaded = true }
                if (!state.sparkAnalysisLoaded) { onLoadSparkAnalysis(); state.sparkAnalysisLoaded = true }
                if (!state.baiduAnalysisLoaded) { onLoadBaiduAnalysis(); state.baiduAnalysisLoaded = true }
                if (!state.notesLoaded) { onLoadNotes(); state.notesLoaded = true }
            }
        }
    }

    fun clearProgressAndReload(onReload: () -> Unit) {
        scope.launch {
            clearExamProgressUseCase(state.progressId)
            val size = state._questions.value.size
            state._currentIndex.value = 0; state._selectedOptions.value = List(size) { emptyList() }
            state._textAnswers.value = List(size) { "" }; state._showResultList.value = List(size) { false }
            state._analysisList.value = List(size) { "" }; state._sparkAnalysisList.value = List(size) { "" }
            state._baiduAnalysisList.value = List(size) { "" }; state._noteList.value = List(size) { "" }
            state._finished.value = false; state._progressLoaded.value = false
            state.progressSeed = System.currentTimeMillis()
            state.analysisLoaded = false; state.sparkAnalysisLoaded = false
            state.baiduAnalysisLoaded = false; state.notesLoaded = false
            onReload()
        }
    }

    fun resetAllStates() {
        val size = state._questions.value.size
        state._currentIndex.value = 0; state._selectedOptions.value = List(size) { emptyList() }
        state._textAnswers.value = List(size) { "" }; state._showResultList.value = List(size) { false }
        state._analysisList.value = List(size) { "" }; state._sparkAnalysisList.value = List(size) { "" }
        state._baiduAnalysisList.value = List(size) { "" }; state._noteList.value = List(size) { "" }
        state._finished.value = false; state._progressLoaded.value = true
    }

    fun clearProgress() {
        scope.launch {
            clearExamProgressUseCase(state.progressId)
            state._currentIndex.value = 0
            state._selectedOptions.value = emptyList(); state._textAnswers.value = emptyList()
            state._showResultList.value = emptyList(); state._analysisList.value = emptyList()
            state._sparkAnalysisList.value = emptyList(); state._baiduAnalysisList.value = emptyList()
            state._noteList.value = emptyList()
            state._finished.value = false; state._progressLoaded.value = false
            state.progressSeed = System.currentTimeMillis()
            state.analysisLoaded = false; state.sparkAnalysisLoaded = false
            state.baiduAnalysisLoaded = false; state.notesLoaded = false
        }
    }
}

