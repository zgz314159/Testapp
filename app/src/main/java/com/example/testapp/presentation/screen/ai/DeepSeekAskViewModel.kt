package com.example.testapp.presentation.screen.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.data.network.deepseek.DeepSeekApiService
import com.example.testapp.data.network.deepseek.DeepSeekAskDisplayPipeline
import com.example.testapp.data.network.deepseek.DeepSeekAskFollowUpPipeline
import com.example.testapp.data.network.deepseek.DeepSeekAskPersistFormatPipeline
import com.example.testapp.data.network.deepseek.DeepSeekAskPersistPipeline
import com.example.testapp.data.network.deepseek.DeepSeekAskSavePipeline
import com.example.testapp.data.network.deepseek.DeepSeekChatTurn
import com.example.testapp.data.network.deepseek.DeepSeekExamAnchor
import com.example.testapp.data.network.deepseek.DeepSeekAskSessionRestorePipeline
import com.example.testapp.data.network.deepseek.DeepSeekMultiTurnMessagesPipeline
import com.example.testapp.domain.usecase.GetDeepSeekAskResultUseCase
import com.example.testapp.domain.usecase.GetQuestionAnalysisUseCase
import com.example.testapp.domain.usecase.SaveDeepSeekAskResultUseCase
import com.example.testapp.domain.usecase.SaveQuestionAnalysisUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class DeepSeekAskViewModel @Inject constructor(
    private val api: DeepSeekApiService,
    private val getAnalysisUseCase: GetQuestionAnalysisUseCase,
    private val saveAnalysisUseCase: SaveQuestionAnalysisUseCase,
    private val getAskLegacyUseCase: GetDeepSeekAskResultUseCase,
    private val saveAskLegacyUseCase: SaveDeepSeekAskResultUseCase
) : ViewModel() {
    companion object {
        const val PARSING = "解析中..."
        const val PARSE_FAILED_PREFIX = "解析失败"
    }

    private val _displayText = MutableStateFlow("")
    val displayText: StateFlow<String> = _displayText.asStateFlow()

    private val _chatTurns = MutableStateFlow<List<DeepSeekChatTurn>>(emptyList())
    val chatTurns: StateFlow<List<DeepSeekChatTurn>> = _chatTurns.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isParsing = MutableStateFlow(false)
    val isParsing: StateFlow<Boolean> = _isParsing.asStateFlow()

    private val turns = mutableListOf<DeepSeekChatTurn>()
    private var firstQuestion: String = ""
    private var examAnchor: DeepSeekExamAnchor? = null

    fun setExamAnchor(anchor: DeepSeekExamAnchor?) {
        examAnchor = anchor
    }

    fun reset() {
        _displayText.value = ""
        _chatTurns.value = emptyList()
        _errorMessage.value = null
        _isParsing.value = false
        turns.clear()
        firstQuestion = ""
        examAnchor = null
    }

    fun restoreSession(questionText: String, savedText: String) {
        firstQuestion = DeepSeekAskSessionRestorePipeline.firstQuestionText(questionText, examAnchor)
        turns.clear()
        turns.addAll(DeepSeekAskPersistFormatPipeline.decode(firstQuestion, savedText))
        publishTurns()
    }

    suspend fun loadSaved(questionId: Int, questionText: String, note: String? = null): String? {
        val raw = loadRawPersisted(questionId, note) ?: return null
        restoreSession(questionText, raw)
        return _displayText.value.takeIf { it.isNotBlank() }
    }

    private suspend fun loadRawPersisted(questionId: Int, note: String?): String? {
        val analysis = getAnalysisUseCase(questionId).getOrNull()
        val askLegacy = getAskLegacyUseCase(questionId)
        return DeepSeekAskPersistPipeline.resolveLoadText(
            analysis = analysis,
            askLegacy = askLegacy ?: DeepSeekAskPersistPipeline.extractFromAskNote(note)
        )
    }

    suspend fun getSavedAnswer(questionId: Int, note: String? = null): String? =
        loadRawPersisted(questionId, note)?.let { raw ->
            DeepSeekAskDisplayPipeline.fromTurns(
                DeepSeekAskPersistFormatPipeline.decode("", raw)
            ).ifBlank { raw }
        }

    suspend fun saveAndWait(questionId: Int, displayText: String): String? {
        val normalized = displayText.trim()
        if (normalized.isBlank()) return null
        val persisted = DeepSeekAskSavePipeline.resolvePersistText(turns, normalized)
        if (persisted.isBlank()) return null
        if (!saveAnalysisUseCase(questionId, persisted).isSuccess) return null
        saveAskLegacyUseCase(questionId, persisted)
        return DeepSeekAskDisplayPipeline.fromTurns(turns).ifBlank { normalized }
    }

    fun ask(questionText: String) {
        viewModelScope.launch {
            _isParsing.value = true
            _errorMessage.value = null
            val isFollowUp = turns.isNotEmpty()
            if (!isFollowUp) {
                firstQuestion = DeepSeekAskSessionRestorePipeline.firstQuestionText(questionText, examAnchor)
            }
            val nextUser = DeepSeekAskFollowUpPipeline.resolveNextUserContent(
                firstQuestion = firstQuestion,
                currentQuestionInput = questionText,
                isFollowUp = isFollowUp,
                examAnchor = examAnchor
            ) ?: run {
                _isParsing.value = false
                return@launch
            }
            val messages = DeepSeekMultiTurnMessagesPipeline.build(
                priorTurns = turns.toList(),
                nextUserContent = nextUser,
                examAnchor = examAnchor
            )
            runCatching { api.chat(messages) }
                .onSuccess { response ->
                    turns.add(DeepSeekChatTurn(user = nextUser, assistant = response))
                    publishTurns()
                }
                .onFailure {
                    _errorMessage.value = "${PARSE_FAILED_PREFIX}: ${it.message ?: "未知错误"}"
                }
            _isParsing.value = false
        }
    }

    fun parsingSuffix(): String = PARSING

    private fun publishTurns() {
        _chatTurns.value = turns.toList()
        _displayText.value = DeepSeekAskDisplayPipeline.fromTurns(turns)
    }
}
