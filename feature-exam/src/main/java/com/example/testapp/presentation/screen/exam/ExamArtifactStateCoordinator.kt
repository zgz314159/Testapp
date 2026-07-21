package com.example.testapp.presentation.screen.exam

import com.example.testapp.core.common.LocalizedResult
import com.example.testapp.core.session.SessionEngine
import com.example.testapp.domain.IOConstants
import com.example.testapp.domain.LocalizedException
import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.updateAt
import com.example.testapp.domain.usecase.ExamUseCaseFacade
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal class ExamArtifactStateCoordinator(
    private val scope: CoroutineScope,
    private val facade: ExamUseCaseFacade,
    private val sessionEngine: SessionEngine,
    private val sessionState: MutableStateFlow<PracticeSessionState>,
    private val messageResult: MutableStateFlow<LocalizedResult?>,
    private val saveProgress: () -> Unit
) {
    private val appendNoteMutex = Mutex()

    fun updateAnalysis(index: Int, text: String) {
        // 空串是显式删除：state 直接清空并落库，不能走 preferStructured（旧文更"富"会吞掉删除）
        sessionState.value.questions.getOrNull(index)?.id?.let { id ->
            scope.launch {
                if (text.isBlank()) {
                    facade.analysis.saveDeepSeek(id, "")
                    return@launch
                }
                val existing = facade.analysis.getDeepSeek(id).getOrNull()
                val richer = com.example.testapp.presentation.screen.shared.SessionDeepSeekAnalysisTextPipeline
                    .preferStructured(existing, text)
                if (richer.isNotBlank() && richer != existing) {
                    facade.analysis.saveDeepSeek(id, richer)
                }
            }
        }
        sessionState.update { s ->
            val current = s.questionsWithState.getOrNull(index)?.analysis
            val next = if (text.isBlank()) {
                ""
            } else {
                com.example.testapp.presentation.screen.shared.SessionDeepSeekAnalysisTextPipeline
                    .preferStructured(current, text)
            }
            if (current == next) s
            else s.updateAt(index) { it.copy(analysis = next) }
        }
        saveProgress()
    }

    fun updateSparkAnalysis(index: Int, text: String) {
        sessionState.value.questions.getOrNull(index)?.id?.let { id ->
            scope.launch { facade.analysis.saveSpark(id, text) }
        }
        sessionState.update { s -> s.updateAt(index) { it.copy(sparkAnalysis = text) } }
        saveProgress()
    }

    fun updateBaiduAnalysis(index: Int, text: String) {
        sessionState.value.questions.getOrNull(index)?.id?.let { id ->
            scope.launch { facade.analysis.saveBaidu(id, text) }
        }
        sessionState.update { s -> s.updateAt(index) { it.copy(baiduAnalysis = text) } }
        saveProgress()
    }

    suspend fun saveNoteAndWait(questionId: Int, index: Int, text: String): Boolean {
        val res = facade.notes.save(questionId, text)
        if (res.isFailure) {
            emitSaveFailure(res.exceptionOrNull())
            return false
        }
        val persistedText = facade.notes.get(questionId).getOrNull() ?: text
        sessionState.update { s -> s.updateAt(index) { it.copy(note = persistedText) } }
        saveProgress()
        return true
    }

    fun saveNote(questionId: Int, index: Int, text: String) {
        // Optimistic state update — shows immediately even before async persist completes
        sessionState.update { s -> s.updateAt(index) { it.copy(note = text) } }
        saveProgress()
        scope.launch {
            facade.notes.save(questionId, text)
        }
    }

    suspend fun appendNoteSuspend(questionId: Int, index: Int, text: String): Boolean {
        var success = false
        appendNoteMutex.withLock {
            val currentResult = facade.notes.get(questionId)
            if (currentResult.isFailure) {
                emitSaveFailure(currentResult.exceptionOrNull())
                return@withLock
            }
            val current = currentResult.getOrNull().orEmpty()
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val next = if (current.isBlank()) "[$timestamp]\n$text" else "$current\n\n[$timestamp]\n$text"
            val saveResult = facade.notes.save(questionId, next)
            if (saveResult.isFailure) {
                emitSaveFailure(saveResult.exceptionOrNull())
                return@withLock
            }
            sessionState.update { s -> s.updateAt(index) { it.copy(note = next) } }
            success = true
        }
        if (success) saveProgress()
        return success
    }

    fun appendNote(questionId: Int, index: Int, text: String) {
        scope.launch { appendNoteSuspend(questionId, index, text) }
    }

    suspend fun getNote(questionId: Int): String? = facade.notes.get(questionId).getOrNull()

    suspend fun loadNotesFromRepository() {
        val list = sessionState.value.questionsWithState
        val changed = sessionEngine.analysisLoader.loadNotes(list)
        if (changed != list) {
            sessionState.update { it.copy(questionsWithState = changed) }
            saveProgress()
        }
    }

    suspend fun loadAnalysisFromRepository() {
        val list = sessionState.value.questionsWithState
        val changed = sessionEngine.analysisLoader.loadAnalysis(list)
        if (changed != list) sessionState.update { it.copy(questionsWithState = changed) }
    }

    suspend fun loadSparkAnalysisFromRepository() {
        val list = sessionState.value.questionsWithState
        val changed = sessionEngine.analysisLoader.loadSparkAnalysis(list)
        if (changed != list) sessionState.update { it.copy(questionsWithState = changed) }
    }

    suspend fun loadBaiduAnalysisFromRepository() {
        val list = sessionState.value.questionsWithState
        val changed = sessionEngine.analysisLoader.loadBaiduAnalysis(list)
        if (changed != list) sessionState.update { it.copy(questionsWithState = changed) }
    }

    private fun emitSaveFailure(ex: Throwable?) {
        messageResult.value = if (ex is LocalizedException) {
            LocalizedResult(IOConstants.SAVE_FAILED_PREFIX, ex.args)
        } else {
            LocalizedResult(IOConstants.SAVE_FAILED_PREFIX, listOf(ex?.message ?: ""))
        }
    }
}
