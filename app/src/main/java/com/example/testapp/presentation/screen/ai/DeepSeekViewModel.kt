package com.example.testapp.presentation.screen.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import com.example.testapp.data.network.deepseek.DeepSeekApiService
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.usecase.GetQuestionAnalysisUseCase
import com.example.testapp.domain.usecase.SaveQuestionAnalysisUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@HiltViewModel
class DeepSeekViewModel @Inject constructor(
    private val api: DeepSeekApiService,
    private val getAnalysis: GetQuestionAnalysisUseCase,
    private val saveAnalysisUseCase: SaveQuestionAnalysisUseCase
) : ViewModel() {
    companion object {
        const val PARSING = "解析中..."
        const val PARSE_FAILED_PREFIX = "解析失败"
    }
    private val _analysis = MutableStateFlow<Pair<Int, String>?>(null)
    val analysis: StateFlow<Pair<Int, String>?> = _analysis.asStateFlow()

    /** Limit concurrent API calls to avoid saturating bandwidth */
    private val semaphore = Semaphore(permits = 2)

    suspend fun getSavedAnalysis(questionId: Int): String? = getAnalysis(questionId).getOrNull()
    fun analyze(index: Int, question: Question) {

        viewModelScope.launch {

            val cacheStart = System.currentTimeMillis()
            val cached = getAnalysis(question.id).getOrNull()
            val cacheDuration = System.currentTimeMillis() - cacheStart
            
            if (!cached.isNullOrBlank()) {
                
                _analysis.value = index to cached
                return@launch
            }

            _analysis.value = index to PARSING
            val apiStart = System.currentTimeMillis()
            runCatching {
                semaphore.withPermit {
                    withContext(Dispatchers.IO) { api.analyze(question) }
                }
            }
                .onSuccess {
                    val apiDuration = System.currentTimeMillis() - apiStart

                    _analysis.value = index to it
                    saveAnalysisUseCase(question.id, it)
                }
                .onFailure {
                    val apiDuration = System.currentTimeMillis() - apiStart

                    _analysis.value = index to "${PARSE_FAILED_PREFIX}: ${it.message}"
                }
        }
    }

    fun save(questionId: Int, analysis: String) {
        viewModelScope.launch {
            saveAnalysisUseCase(questionId, analysis)
        }
    }
    fun clear() {
        _analysis.value = null
    }
}