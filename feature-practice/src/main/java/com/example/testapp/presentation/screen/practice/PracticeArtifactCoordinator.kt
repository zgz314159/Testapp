package com.example.testapp.presentation.screen.practice

import com.example.testapp.core.common.LocalizedResult
import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.usecase.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PracticeArtifactCoordinator(
    private val _sessionState: MutableStateFlow<PracticeSessionState>,
    private val _messageResult: MutableStateFlow<LocalizedResult?>,
    private val scope: CoroutineScope,
    private val onSaveProgress: () -> Unit,
    private val appendNoteMutex: Mutex,
    private val noteTraceTag: String,
    // repos
    private val saveQuestionAnalysisUseCase: SaveQuestionAnalysisUseCase,
    private val getQuestionAnalysisUseCase: GetQuestionAnalysisUseCase,
    private val saveSparkAnalysisUseCase: SaveSparkAnalysisUseCase,
    private val getSparkAnalysisUseCase: GetSparkAnalysisUseCase,
    private val saveBaiduAnalysisUseCase: SaveBaiduAnalysisUseCase,
    private val getBaiduAnalysisUseCase: GetBaiduAnalysisUseCase,
    private val saveQuestionNoteUseCase: SaveQuestionNoteUseCase,
    private val getQuestionNoteUseCase: GetQuestionNoteUseCase
) {

    // ---- Analysis update ----

    fun updateAnalysis(index: Int, text: String) {
        val questionId = _sessionState.value.questionsWithState.getOrNull(index)?.question?.id
        if (questionId != null) {
            scope.launch {
                val res = saveQuestionAnalysisUseCase(questionId, text)
                if (res.isFailure) {
                    val ex = res.exceptionOrNull()
                    _messageResult.value = if (ex is com.example.testapp.domain.LocalizedException)
                        LocalizedResult(com.example.testapp.domain.ParsingConstants.PARSE_FAILED_PREFIX_COLON_KEY, ex.args)
                    else LocalizedResult(com.example.testapp.domain.ParsingConstants.PARSE_FAILED_PREFIX_COLON_KEY, listOf(ex?.message ?: ""))
                }
            }
        }
        val currentState = _sessionState.value
        val updated = currentState.questionsWithState.mapIndexed { idx, qws ->
            if (idx == index) qws.copy(analysis = text) else qws
        }
        _sessionState.value = currentState.copy(questionsWithState = updated)
        onSaveProgress()
    }

    fun updateSparkAnalysis(index: Int, text: String) {
        val questionId = _sessionState.value.questionsWithState.getOrNull(index)?.question?.id
        if (questionId != null) {
            scope.launch {
                val res = saveSparkAnalysisUseCase(questionId, text)
                if (res.isFailure) {
                    val ex = res.exceptionOrNull()
                    _messageResult.value = if (ex is com.example.testapp.domain.LocalizedException)
                        LocalizedResult(com.example.testapp.domain.ParsingConstants.PARSE_FAILED_PREFIX_COLON_KEY, ex.args)
                    else LocalizedResult(com.example.testapp.domain.ParsingConstants.PARSE_FAILED_PREFIX_COLON_KEY, listOf(ex?.message ?: ""))
                }
            }
        }
        val currentState = _sessionState.value
        val updated = currentState.questionsWithState.mapIndexed { idx, qws ->
            if (idx == index) qws.copy(sparkAnalysis = text) else qws
        }
        _sessionState.value = currentState.copy(questionsWithState = updated)
        onSaveProgress()
    }

    fun updateBaiduAnalysis(index: Int, text: String) {
        val questionId = _sessionState.value.questionsWithState.getOrNull(index)?.question?.id
        if (questionId != null) {
            scope.launch {
                val res = saveBaiduAnalysisUseCase(questionId, text)
                if (res.isFailure) {
                    val ex = res.exceptionOrNull()
                    _messageResult.value = if (ex is com.example.testapp.domain.LocalizedException)
                        LocalizedResult(com.example.testapp.domain.ParsingConstants.PARSE_FAILED_PREFIX_COLON_KEY, ex.args)
                    else LocalizedResult(com.example.testapp.domain.ParsingConstants.PARSE_FAILED_PREFIX_COLON_KEY, listOf(ex?.message ?: ""))
                }
            }
        }
        val currentState = _sessionState.value
        val updated = currentState.questionsWithState.mapIndexed { idx, qws ->
            if (idx == index) qws.copy(baiduAnalysis = text) else qws
        }
        _sessionState.value = currentState.copy(questionsWithState = updated)
        onSaveProgress()
    }

    // ---- Note CRUD ----

    suspend fun saveNoteAndWait(questionId: Int, index: Int, text: String): Boolean {
        val res = saveQuestionNoteUseCase(questionId, text)
        if (res.isFailure) {
            val ex = res.exceptionOrNull()
            _messageResult.value = if (ex is com.example.testapp.domain.LocalizedException)
                LocalizedResult(com.example.testapp.domain.IOConstants.SAVE_FAILED_PREFIX, ex.args)
            else LocalizedResult(com.example.testapp.domain.IOConstants.SAVE_FAILED_PREFIX, listOf(ex?.message ?: ""))
            return false
        }
        val readBack = getQuestionNoteUseCase(questionId).getOrNull() ?: text
        val currentState = _sessionState.value
        val updated = currentState.questionsWithState.mapIndexed { idx, qws ->
            if (idx == index || qws.question.id == questionId) qws.copy(note = readBack) else qws
        }
        _sessionState.value = currentState.copy(questionsWithState = updated)
        onSaveProgress()
        return true
    }

    fun saveNote(questionId: Int, index: Int, text: String) {
        scope.launch { saveNoteAndWait(questionId, index, text) }
    }

    suspend fun appendNoteSuspend(questionId: Int, index: Int, text: String): Boolean {
        try {
            appendNoteMutex.withLock {
                val currentRes = getQuestionNoteUseCase(questionId)
                if (currentRes.isFailure) {
                    val ex = currentRes.exceptionOrNull()
                    _messageResult.value = if (ex is com.example.testapp.domain.LocalizedException)
                        LocalizedResult(com.example.testapp.domain.IOConstants.SAVE_FAILED_PREFIX, ex.args)
                    else LocalizedResult(com.example.testapp.domain.IOConstants.SAVE_FAILED_PREFIX, listOf(ex?.message ?: ""))
                    return false
                }
                val current = currentRes.getOrNull() ?: ""
                val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                val timestampedText = "[$timestamp]\n$text"
                val newText = if (current.isBlank()) timestampedText else "$current\n\n$timestampedText"
                val saveRes = saveQuestionNoteUseCase(questionId, newText)
                if (saveRes.isFailure) {
                    val ex = saveRes.exceptionOrNull()
                    _messageResult.value = if (ex is com.example.testapp.domain.LocalizedException)
                        LocalizedResult(com.example.testapp.domain.IOConstants.SAVE_FAILED_PREFIX, ex.args)
                    else LocalizedResult(com.example.testapp.domain.IOConstants.SAVE_FAILED_PREFIX, listOf(ex?.message ?: ""))
                    return false
                }
                val currentState = _sessionState.value
                val updated = currentState.questionsWithState.mapIndexed { idx, qws ->
                    if (idx == index) qws.copy(note = newText) else qws
                }
                _sessionState.value = currentState.copy(questionsWithState = updated)
                return true
            }
        } catch (e: Exception) {
            _messageResult.value = LocalizedResult(com.example.testapp.domain.IOConstants.SAVE_FAILED_PREFIX, listOf(e.message ?: ""))
            return false
        }
    }

    fun appendNote(questionId: Int, index: Int, text: String) {
        scope.launch {
            try {
                appendNoteMutex.withLock {
                    val currentRes = getQuestionNoteUseCase(questionId)
                    if (currentRes.isFailure) {
                        val ex = currentRes.exceptionOrNull()
                        _messageResult.value = if (ex is com.example.testapp.domain.LocalizedException)
                            LocalizedResult(com.example.testapp.domain.IOConstants.SAVE_FAILED_PREFIX, ex.args)
                        else LocalizedResult(com.example.testapp.domain.IOConstants.SAVE_FAILED_PREFIX, listOf(ex?.message ?: ""))
                        return@withLock
                    }
                    val current = currentRes.getOrNull() ?: ""
                    val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                    val timestampedText = "[$timestamp]\n$text"
                    val newText = if (current.isBlank()) timestampedText else "$current\n\n$timestampedText"
                    val saveRes = saveQuestionNoteUseCase(questionId, newText)
                    if (saveRes.isFailure) {
                        val ex = saveRes.exceptionOrNull()
                        _messageResult.value = if (ex is com.example.testapp.domain.LocalizedException)
                            LocalizedResult(com.example.testapp.domain.IOConstants.SAVE_FAILED_PREFIX, ex.args)
                        else LocalizedResult(com.example.testapp.domain.IOConstants.SAVE_FAILED_PREFIX, listOf(ex?.message ?: ""))
                        return@withLock
                    }
                    val currentState = _sessionState.value
                    val updated = currentState.questionsWithState.mapIndexed { idx, qws ->
                        if (idx == index) qws.copy(note = newText) else qws
                    }
                    _sessionState.value = currentState.copy(questionsWithState = updated)
                }
            } catch (e: Exception) {
                _messageResult.value = LocalizedResult(com.example.testapp.domain.IOConstants.SAVE_FAILED_PREFIX, listOf(e.message ?: ""))
            }
        }
    }

    suspend fun getNote(questionId: Int): String? = getQuestionNoteUseCase(questionId).getOrNull()

    // ---- Stored analyses refresh ----

    suspend fun refreshStoredAnalyses(index: Int) {
        val currentState = _sessionState.value
        val qws = currentState.questionsWithState.getOrNull(index) ?: return
        val deepSeek = getQuestionAnalysisUseCase(qws.question.id).getOrNull().orEmpty()
        val spark = getSparkAnalysisUseCase(qws.question.id).getOrNull().orEmpty()
        val baidu = getBaiduAnalysisUseCase(qws.question.id).getOrNull().orEmpty()
        val savedNote = getQuestionNoteUseCase(qws.question.id).getOrNull()
        if (deepSeek.isBlank() && spark.isBlank() && baidu.isBlank() && savedNote == null) return
        val updated = currentState.questionsWithState.mapIndexed { idx, item ->
            if (idx == index) item.copy(
                analysis = deepSeek.ifBlank { item.analysis },
                sparkAnalysis = spark.ifBlank { item.sparkAnalysis },
                baiduAnalysis = baidu.ifBlank { item.baiduAnalysis },
                note = savedNote ?: item.note
            ) else item
        }
        _sessionState.value = currentState.copy(questionsWithState = updated)
        onSaveProgress()
    }
}

