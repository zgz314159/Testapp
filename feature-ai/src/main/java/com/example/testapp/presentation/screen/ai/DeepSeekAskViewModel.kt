package com.example.testapp.presentation.screen.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.data.network.deepseek.DeepSeekApiService
import com.example.testapp.data.network.deepseek.DeepSeekAskDisplayPipeline
import com.example.testapp.data.network.deepseek.DeepSeekAskFollowUpPipeline
import com.example.testapp.data.network.deepseek.DeepSeekAskLoadSeedPipeline
import com.example.testapp.data.network.deepseek.DeepSeekAskPersistDebugLog
import com.example.testapp.data.network.deepseek.DeepSeekAskPersistFormatPipeline
import com.example.testapp.data.network.deepseek.DeepSeekAskPersistPipeline
import com.example.testapp.data.network.deepseek.DeepSeekAskSavePipeline
import com.example.testapp.data.network.deepseek.DeepSeekAskSessionRestorePipeline
import com.example.testapp.data.network.deepseek.DeepSeekChatTurn
import com.example.testapp.data.network.deepseek.DeepSeekExamAnchor
import com.example.testapp.data.network.deepseek.DeepSeekMultiTurnMessagesPipeline
import com.example.testapp.domain.usecase.GetDeepSeekAskResultUseCase
import com.example.testapp.domain.usecase.GetQuestionAnalysisUseCase
import com.example.testapp.domain.usecase.SaveDeepSeekAskResultUseCase
import com.example.testapp.domain.usecase.SaveQuestionAnalysisUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    private val _webSearchEnabled = MutableStateFlow(false)
    val webSearchEnabled: StateFlow<Boolean> = _webSearchEnabled.asStateFlow()

    private val turns = mutableListOf<DeepSeekChatTurn>()
    private var firstQuestion: String = ""
    private var examAnchor: DeepSeekExamAnchor? = null

    fun setExamAnchor(anchor: DeepSeekExamAnchor?) {
        examAnchor = anchor
    }

    fun reset() {
        DeepSeekAskPersistDebugLog.d("VM.reset", "clear turns=${turns.size} firstQ=${DeepSeekAskPersistDebugLog.preview(firstQuestion)}")
        _displayText.value = ""
        _chatTurns.value = emptyList()
        _errorMessage.value = null
        _isParsing.value = false
        _webSearchEnabled.value = false
        turns.clear()
        firstQuestion = ""
        examAnchor = null
    }

    fun restoreSession(questionText: String, savedText: String) {
        firstQuestion = DeepSeekAskSessionRestorePipeline.firstQuestionText(questionText, examAnchor)
        turns.clear()
        turns.addAll(DeepSeekAskPersistFormatPipeline.decode(firstQuestion, savedText))
        DeepSeekAskPersistDebugLog.d(
            "VM.restoreSession",
            "qIdText=${DeepSeekAskPersistDebugLog.preview(questionText, 48)} " +
                "saved.${DeepSeekAskPersistDebugLog.meta(savedText)} " +
                "decoded=${DeepSeekAskPersistDebugLog.turnsSummary(turns)}",
        )
        publishTurns()
    }

    suspend fun loadSaved(
        questionId: Int,
        questionText: String,
        note: String? = null,
        seedDisplay: String? = null,
    ): String? {
        val raw = loadRawPersisted(questionId, note)
        val currentEncoded = if (turns.isNotEmpty()) {
            DeepSeekAskPersistFormatPipeline.encode(turns)
        } else {
            null
        }
        DeepSeekAskPersistDebugLog.d(
            "VM.loadSaved.in",
            "qId=$questionId existingTurns=${turns.size} " +
                "db.${DeepSeekAskPersistDebugLog.meta(raw)} " +
                "seed.${DeepSeekAskPersistDebugLog.meta(seedDisplay)} " +
                "current.${DeepSeekAskPersistDebugLog.meta(currentEncoded)}",
        )
        val merged = DeepSeekAskLoadSeedPipeline.resolveRaw(
            dbRaw = DeepSeekAskLoadSeedPipeline.resolveRaw(raw, currentEncoded),
            seedDisplay = seedDisplay,
        )
        if (merged == null) {
            DeepSeekAskPersistDebugLog.w("VM.loadSaved", "qId=$questionId → merged=null (no restore)")
            return null
        }
        DeepSeekAskPersistDebugLog.d(
            "VM.loadSaved.merged",
            "qId=$questionId chosen.${DeepSeekAskPersistDebugLog.meta(merged)} preview=${DeepSeekAskPersistDebugLog.preview(merged)}",
        )
        if (currentEncoded != null &&
            DeepSeekAskLoadSeedPipeline.richness(merged) <= DeepSeekAskLoadSeedPipeline.richness(currentEncoded) &&
            merged.length <= currentEncoded.length
        ) {
            DeepSeekAskPersistDebugLog.d(
                "VM.loadSaved.keepCurrent",
                "qId=$questionId keep existing turns=${turns.size}",
            )
            return _displayText.value.takeIf { it.isNotBlank() }
        }
        restoreSession(questionText, merged)
        return _displayText.value.takeIf { it.isNotBlank() }
    }

    private suspend fun loadRawPersisted(questionId: Int, note: String?): String? {
        val analysis = getAnalysisUseCase(questionId).getOrNull()
        val askLegacy = getAskLegacyUseCase(questionId)
        val resolved = DeepSeekAskPersistPipeline.resolveLoadText(
            analysis = analysis,
            askLegacy = askLegacy ?: DeepSeekAskPersistPipeline.extractFromAskNote(note)
        )
        DeepSeekAskPersistDebugLog.d(
            "VM.loadRaw",
            "qId=$questionId analysis.${DeepSeekAskPersistDebugLog.meta(analysis)} " +
                "legacy.${DeepSeekAskPersistDebugLog.meta(askLegacy)} " +
                "resolved.${DeepSeekAskPersistDebugLog.meta(resolved)}",
        )
        return resolved
    }

    suspend fun getSavedAnswer(questionId: Int, note: String? = null): String? =
        loadRawPersisted(questionId, note)?.let { raw ->
            DeepSeekAskDisplayPipeline.fromTurns(
                DeepSeekAskPersistFormatPipeline.decode("", raw)
            ).ifBlank { raw }
        }

    suspend fun saveAndWait(questionId: Int, displayText: String): String? {
        val normalized = displayText.trim()
        DeepSeekAskPersistDebugLog.d(
            "VM.save.begin",
            "qId=$questionId turns=${DeepSeekAskPersistDebugLog.turnsSummary(turns)} " +
                "display.${DeepSeekAskPersistDebugLog.meta(normalized)}",
        )
        if (normalized.isBlank() && turns.isEmpty()) {
            DeepSeekAskPersistDebugLog.w("VM.save", "qId=$questionId aborted: blank")
            return null
        }
        val persisted = DeepSeekAskSavePipeline.resolvePersistText(turns, normalized)
        if (persisted.isBlank()) {
            DeepSeekAskPersistDebugLog.w("VM.save", "qId=$questionId aborted: persist blank")
            return null
        }
        val saveResult = saveAnalysisUseCase(questionId, persisted)
        if (!saveResult.isSuccess) {
            DeepSeekAskPersistDebugLog.e(
                "VM.save",
                "qId=$questionId saveAnalysis FAILED ${saveResult.exceptionOrNull()?.message}",
                saveResult.exceptionOrNull(),
            )
            return null
        }
        saveAskLegacyUseCase(questionId, persisted)
        val readBack = getAnalysisUseCase(questionId).getOrNull()
        DeepSeekAskPersistDebugLog.d(
            "VM.save.ok",
            "qId=$questionId persisted.${DeepSeekAskPersistDebugLog.meta(persisted)} " +
                "readBack.${DeepSeekAskPersistDebugLog.meta(readBack)} " +
                "readBackEq=${readBack == persisted} " +
                "preview=${DeepSeekAskPersistDebugLog.preview(persisted, 120)}",
        )
        return persisted
    }

    fun ask(questionText: String) {
        viewModelScope.launch {
            _isParsing.value = true
            _errorMessage.value = null
            val isFollowUp = turns.isNotEmpty()
            if (!isFollowUp) {
                firstQuestion = DeepSeekAskSessionRestorePipeline.firstQuestionText(questionText, examAnchor)
            }
            val resolved = DeepSeekAskFollowUpPipeline.resolve(
                firstQuestion = firstQuestion,
                currentQuestionInput = questionText,
                isFollowUp = isFollowUp,
                examAnchor = examAnchor
            )
            DeepSeekAskPersistDebugLog.d(
                "VM.ask",
                "followUp=$isFollowUp kind=${resolved.kind} priorTurns=${turns.size} " +
                    "input=${DeepSeekAskPersistDebugLog.preview(questionText, 48)} " +
                    "userContent=${DeepSeekAskPersistDebugLog.preview(resolved.userContent, 48)}",
            )
            val nextUser = resolved.userContent ?: run {
                DeepSeekAskPersistDebugLog.w("VM.ask", "skipped: userContent=null")
                _isParsing.value = false
                return@launch
            }
            val messages = DeepSeekMultiTurnMessagesPipeline.build(
                priorTurns = turns.toList(),
                nextUserContent = nextUser,
                examAnchor = examAnchor
            )
            val useWebSearch = _webSearchEnabled.value
            runCatching {
                api.chat(
                    messages = messages,
                    enableThinking = resolved.enableThinking,
                    useWebSearch = useWebSearch,
                )
            }
                .onSuccess { response ->
                    turns.add(DeepSeekChatTurn(user = nextUser, assistant = response))
                    DeepSeekAskPersistDebugLog.d(
                        "VM.ask.ok",
                        "nowTurns=${turns.size} ${DeepSeekAskPersistDebugLog.turnsSummary(turns)}",
                    )
                    publishTurns()
                }
                .onFailure {
                    DeepSeekAskPersistDebugLog.e("VM.ask.fail", it.message ?: "unknown", it)
                    _errorMessage.value = "${PARSE_FAILED_PREFIX}: ${it.message ?: "未知错误"}"
                }
            _isParsing.value = false
        }
    }

    fun setWebSearchEnabled(enabled: Boolean) {
        if (!_isParsing.value) {
            _webSearchEnabled.value = enabled
        }
    }

    fun updateAssistantByMessageIndex(messageIndex: Int, text: String) {
        var cursor = 0
        for (i in turns.indices) {
            val userLen = if (turns[i].user.trim().isNotEmpty()) 1 else 0
            val hasAssistant = turns[i].assistant.trim().isNotEmpty() || text.isNotEmpty()
            if (userLen == 1 && cursor == messageIndex) return
            if (userLen == 1) cursor++
            if (hasAssistant) {
                if (cursor == messageIndex) {
                    turns[i] = turns[i].copy(assistant = text)
                    publishTurns()
                    return
                }
                cursor++
            }
        }
    }

    fun parsingSuffix(): String = PARSING

    private fun publishTurns() {
        _chatTurns.value = turns.toList()
        _displayText.value = DeepSeekAskDisplayPipeline.fromTurns(turns)
    }
}
