package com.example.testapp.presentation.screen.practice.session

import com.example.testapp.core.common.LocalizedResult
import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.usecase.GetBaiduAnalysisUseCase
import com.example.testapp.domain.usecase.GetQuestionAnalysisUseCase
import com.example.testapp.domain.usecase.GetQuestionNoteUseCase
import com.example.testapp.domain.usecase.GetSparkAnalysisUseCase
import com.example.testapp.presentation.screen.practice.PracticeSessionAnalysisMergePipeline
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Analysis Manager — 4种 AI 分析/笔记加载器
 * Extracted from PracticeSessionCoordinator's private load* methods.
 * Each loader is a self-contained suspend function ~40 lines.
 */
class AnalysisManager(
    private val _sessionState: MutableStateFlow<PracticeSessionState>,
    private val _messageResult: MutableStateFlow<LocalizedResult?>,
    private val scope: CoroutineScope,
    private val getQuestionAnalysisUseCase: GetQuestionAnalysisUseCase,
    private val getSparkAnalysisUseCase: GetSparkAnalysisUseCase,
    private val getBaiduAnalysisUseCase: GetBaiduAnalysisUseCase,
    private val getQuestionNoteUseCase: GetQuestionNoteUseCase,
    private val saveProgress: suspend () -> Unit
) {
    private var supplementaryLoadJob: Job? = null

    fun enqueueSupplementaryRepositoryLoads() {
        supplementaryLoadJob?.cancel()
        supplementaryLoadJob = scope.launch(Dispatchers.IO) {
            loadAnalysisFromRepository()
            loadSparkAnalysisFromRepository()
            loadBaiduAnalysisFromRepository()
            loadNotesFromRepository()
        }
    }

    fun cancelSupplementaryJobs() {
        supplementaryLoadJob?.cancel()
        supplementaryLoadJob = null
    }

    private suspend fun loadAnalysisFromRepository() {
        val currentState = _sessionState.value
        var changed = false
        val updated = currentState.questionsWithState.map { qws ->
            if (qws.analysis.isBlank()) {
                val res = getQuestionAnalysisUseCase(qws.question.id)
                val text = res.getOrNull()
                if (res.isFailure) postParseError(res.exceptionOrNull())
                if (!text.isNullOrBlank()) { changed = true; qws.copy(analysis = text) } else qws
            } else qws
        }
        if (changed) {
            val latest = _sessionState.value
            _sessionState.value = latest.copy(
                questionsWithState = PracticeSessionAnalysisMergePipeline.mergeSupplementaryLoad(
                    latest.questionsWithState,
                    updated
                )
            )
            saveProgress()
        }
    }

    private suspend fun loadSparkAnalysisFromRepository() {
        val currentState = _sessionState.value
        var changed = false
        val updated = currentState.questionsWithState.map { qws ->
            if (qws.sparkAnalysis.isBlank()) {
                val res = getSparkAnalysisUseCase(qws.question.id)
                val text = res.getOrNull()
                if (res.isFailure) postParseError(res.exceptionOrNull())
                if (!text.isNullOrBlank()) { changed = true; qws.copy(sparkAnalysis = text) } else qws
            } else qws
        }
        if (changed) {
            val latest = _sessionState.value
            _sessionState.value = latest.copy(
                questionsWithState = PracticeSessionAnalysisMergePipeline.mergeSupplementaryLoad(
                    latest.questionsWithState,
                    updated
                )
            )
            saveProgress()
        }
    }

    private suspend fun loadBaiduAnalysisFromRepository() {
        val currentState = _sessionState.value
        var changed = false
        val updated = currentState.questionsWithState.map { qws ->
            if (qws.baiduAnalysis.isBlank()) {
                val res = getBaiduAnalysisUseCase(qws.question.id)
                val text = res.getOrNull()
                if (res.isFailure) postParseError(res.exceptionOrNull())
                if (!text.isNullOrBlank()) { changed = true; qws.copy(baiduAnalysis = text) } else qws
            } else qws
        }
        if (changed) {
            val latest = _sessionState.value
            _sessionState.value = latest.copy(
                questionsWithState = PracticeSessionAnalysisMergePipeline.mergeSupplementaryLoad(
                    latest.questionsWithState,
                    updated
                )
            )
            saveProgress()
        }
    }

    private suspend fun loadNotesFromRepository() {
        val currentState = _sessionState.value
        var changed = false
        val updated = currentState.questionsWithState.map { qws ->
            val res = getQuestionNoteUseCase(qws.question.id)
            val text = res.getOrNull()
            if (res.isFailure) postNoteError(res.exceptionOrNull())
            if (text != null && text != qws.note) { changed = true; qws.copy(note = text) } else qws
        }
        if (changed) {
            val latest = _sessionState.value
            _sessionState.value = latest.copy(
                questionsWithState = PracticeSessionAnalysisMergePipeline.mergeSupplementaryLoad(
                    latest.questionsWithState,
                    updated
                )
            )
            saveProgress()
        }
    }

    private fun postParseError(ex: Throwable?) {
        _messageResult.value = when (ex) {
            is com.example.testapp.domain.LocalizedException ->
                LocalizedResult(com.example.testapp.domain.ParsingConstants.PARSE_FAILED_PREFIX_COLON_KEY, ex.args)
            else ->
                LocalizedResult(com.example.testapp.domain.ParsingConstants.PARSE_FAILED_PREFIX_COLON_KEY, listOf(ex?.message ?: ""))
        }
    }

    private fun postNoteError(ex: Throwable?) {
        _messageResult.value = when (ex) {
            is com.example.testapp.domain.LocalizedException ->
                LocalizedResult(com.example.testapp.domain.IOConstants.SAVE_FAILED_PREFIX, ex.args)
            else ->
                LocalizedResult(com.example.testapp.domain.IOConstants.SAVE_FAILED_PREFIX, listOf(ex?.message ?: ""))
        }
    }
}
