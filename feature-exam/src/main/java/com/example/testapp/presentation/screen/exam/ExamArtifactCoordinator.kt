package com.example.testapp.presentation.screen.exam

import com.example.testapp.core.common.LocalizedResult
import com.example.testapp.domain.usecase.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExamArtifactCoordinator @Inject constructor(
    private val state: ExamState,
    private val saveQuestionNoteUseCase: SaveQuestionNoteUseCase,
    private val getQuestionNoteUseCase: GetQuestionNoteUseCase,
    private val saveQuestionAnalysisUseCase: SaveQuestionAnalysisUseCase,
    private val getQuestionAnalysisUseCase: GetQuestionAnalysisUseCase,
    private val saveSparkAnalysisUseCase: SaveSparkAnalysisUseCase,
    private val getSparkAnalysisUseCase: GetSparkAnalysisUseCase,
    private val saveBaiduAnalysisUseCase: SaveBaiduAnalysisUseCase,
    private val getBaiduAnalysisUseCase: GetBaiduAnalysisUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    suspend fun saveNoteAndWait(questionId: Int, index: Int, text: String, onSaveProgress: () -> Unit): Boolean {
        val res = saveQuestionNoteUseCase(questionId, text)
        if (res.isFailure) {
            val ex = res.exceptionOrNull()
            state._messageResult.value = if (ex is com.example.testapp.domain.LocalizedException)
                LocalizedResult(com.example.testapp.domain.IOConstants.SAVE_FAILED_PREFIX, ex.args)
            else LocalizedResult(com.example.testapp.domain.IOConstants.SAVE_FAILED_PREFIX, listOf(ex?.message ?: ""))
            return false
        }
        val pt = getQuestionNoteUseCase(questionId).getOrNull() ?: text
        val list = state._noteList.value.toMutableList()
        while (list.size <= index) list.add("")
        list[index] = pt; state._noteList.value = list; onSaveProgress(); return true
    }

    fun saveNote(questionId: Int, index: Int, text: String, onSaveProgress: () -> Unit) {
        scope.launch { saveNoteAndWait(questionId, index, text, onSaveProgress) }
    }

    fun appendNote(questionId: Int, index: Int, text: String, onSaveProgress: () -> Unit) {
        scope.launch {
            state.appendNoteMutex.withLock {
                val cr = getQuestionNoteUseCase(questionId)
                if (cr.isFailure) {
                    val ex = cr.exceptionOrNull()
                    state._messageResult.value = if (ex is com.example.testapp.domain.LocalizedException)
                        LocalizedResult(com.example.testapp.domain.IOConstants.SAVE_FAILED_PREFIX, ex.args)
                    else LocalizedResult(com.example.testapp.domain.IOConstants.SAVE_FAILED_PREFIX, listOf(ex?.message ?: ""))
                    return@withLock
                }
                val cur = cr.getOrNull() ?: ""
                val ts = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                    .format(java.util.Date())
                val nt = if (cur.isBlank()) "[$ts]\n$text" else "$cur\n\n[$ts]\n$text"
                val sr = saveQuestionNoteUseCase(questionId, nt)
                if (sr.isFailure) {
                    val ex = sr.exceptionOrNull()
                    state._messageResult.value = if (ex is com.example.testapp.domain.LocalizedException)
                        LocalizedResult(com.example.testapp.domain.IOConstants.SAVE_FAILED_PREFIX, ex.args)
                    else LocalizedResult(com.example.testapp.domain.IOConstants.SAVE_FAILED_PREFIX, listOf(ex?.message ?: ""))
                    return@withLock
                }
                val list = state._noteList.value.toMutableList()
                while (list.size <= index) list.add("")
                list[index] = nt; state._noteList.value = list; onSaveProgress()
            }
        }
    }

    suspend fun getNote(questionId: Int): String? = getQuestionNoteUseCase(questionId).getOrNull()

    fun updateAnalysis(index: Int, text: String, onSaveProgress: () -> Unit) {
        state._questions.value.getOrNull(index)?.id?.let { id ->
            scope.launch { saveQuestionAnalysisUseCase(id, text) }
        }
        val list = state._analysisList.value.toMutableList()
        while (list.size <= index) list.add("")
        list[index] = text; state._analysisList.value = list; onSaveProgress()
    }

    fun updateSparkAnalysis(index: Int, text: String, onSaveProgress: () -> Unit) {
        state._questions.value.getOrNull(index)?.id?.let { id ->
            scope.launch { saveSparkAnalysisUseCase(id, text) }
        }
        val list = state._sparkAnalysisList.value.toMutableList()
        while (list.size <= index) list.add("")
        list[index] = text; state._sparkAnalysisList.value = list; onSaveProgress()
    }

    fun updateBaiduAnalysis(index: Int, text: String, onSaveProgress: () -> Unit) {
        state._questions.value.getOrNull(index)?.id?.let { id ->
            scope.launch { saveBaiduAnalysisUseCase(id, text) }
        }
        val list = state._baiduAnalysisList.value.toMutableList()
        while (list.size <= index) list.add("")
        list[index] = text; state._baiduAnalysisList.value = list; onSaveProgress()
    }

    suspend fun loadNotesFromRepository(onSaveProgress: () -> Unit) {
        val qs = state._questions.value; val list = state._noteList.value.toMutableList(); var changed = false
        qs.forEachIndexed { i, q ->
            if (i >= list.size) list.add("")
            val res = getQuestionNoteUseCase(q.id); val text = res.getOrNull()
            if (res.isFailure) {
                val ex = res.exceptionOrNull()
                state._messageResult.value = if (ex is com.example.testapp.domain.LocalizedException)
                    LocalizedResult(com.example.testapp.domain.IOConstants.SAVE_FAILED_PREFIX, ex.args)
                else LocalizedResult(com.example.testapp.domain.IOConstants.SAVE_FAILED_PREFIX, listOf(ex?.message ?: ""))
            }
            if (text != null && text != list[i]) { list[i] = text; changed = true }
        }
        if (changed) { state._noteList.value = list; onSaveProgress() }
    }

    suspend fun loadAnalysisFromRepository() {
        val qs = state._questions.value; val list = state._analysisList.value.toMutableList(); var changed = false
        qs.forEachIndexed { i, q ->
            if (i >= list.size) list.add("")
            if (list[i].isBlank()) {
                val res = getQuestionAnalysisUseCase(q.id); val text = res.getOrNull()
                if (res.isFailure) {
                    val ex = res.exceptionOrNull()
                    state._messageResult.value = if (ex is com.example.testapp.domain.LocalizedException)
                        LocalizedResult(com.example.testapp.domain.ParsingConstants.PARSE_FAILED_PREFIX_COLON_KEY, ex.args)
                    else LocalizedResult(com.example.testapp.domain.ParsingConstants.PARSE_FAILED_PREFIX_COLON_KEY, listOf(ex?.message ?: ""))
                }
                if (!text.isNullOrBlank()) { list[i] = text; changed = true }
            }
        }
        if (changed) state._analysisList.value = list
    }

    suspend fun loadSparkAnalysisFromRepository() {
        val qs = state._questions.value; val list = state._sparkAnalysisList.value.toMutableList(); var changed = false
        qs.forEachIndexed { i, q ->
            if (i >= list.size) list.add("")
            if (list[i].isBlank()) {
                val res = getSparkAnalysisUseCase(q.id); val text = res.getOrNull()
                if (res.isFailure) {
                    val ex = res.exceptionOrNull()
                    state._messageResult.value = if (ex is com.example.testapp.domain.LocalizedException)
                        LocalizedResult(com.example.testapp.domain.ParsingConstants.PARSE_FAILED_PREFIX_COLON_KEY, ex.args)
                    else LocalizedResult(com.example.testapp.domain.ParsingConstants.PARSE_FAILED_PREFIX_COLON_KEY, listOf(ex?.message ?: ""))
                }
                if (!text.isNullOrBlank()) { list[i] = text; changed = true }
            }
        }
        if (changed) state._sparkAnalysisList.value = list
    }

    suspend fun loadBaiduAnalysisFromRepository() {
        val qs = state._questions.value; val list = state._baiduAnalysisList.value.toMutableList(); var changed = false
        qs.forEachIndexed { i, q ->
            if (i >= list.size) list.add("")
            if (list[i].isBlank()) {
                val res = getBaiduAnalysisUseCase(q.id); val text = res.getOrNull()
                if (res.isFailure) {
                    val ex = res.exceptionOrNull()
                    state._messageResult.value = if (ex is com.example.testapp.domain.LocalizedException)
                        LocalizedResult(com.example.testapp.domain.ParsingConstants.PARSE_FAILED_PREFIX_COLON_KEY, ex.args)
                    else LocalizedResult(com.example.testapp.domain.ParsingConstants.PARSE_FAILED_PREFIX_COLON_KEY, listOf(ex?.message ?: ""))
                }
                if (!text.isNullOrBlank()) { list[i] = text; changed = true }
            }
        }
        if (changed) state._baiduAnalysisList.value = list
    }
}

