package com.example.testapp.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.data.network.DeepSeekApiService
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
    private val saveAnalysis: SaveQuestionAnalysisUseCase
) : ViewModel() {
    private val _analysis = MutableStateFlow<Pair<Int, String>?>(null)
    val analysis: StateFlow<Pair<Int, String>?> = _analysis.asStateFlow()

    fun analyze(index: Int, question: Question) {

        viewModelScope.launch {
            android.util.Log.d("DeepSeekViewModel", "Analyze question id=${question.id}")
            android.util.Log.d(
                "DeepSeekViewModel",
                "QuestionJson=${Json.encodeToString(question)}"
            )
            val cacheStart = System.currentTimeMillis()
            val cached = getAnalysis(question.id)
            val cacheDuration = System.currentTimeMillis() - cacheStart
            android.util.Log.d("DeepSeekViewModel", "Check cache duration=${cacheDuration} ms")
            if (!cached.isNullOrBlank()) {
                android.util.Log.d("DeepSeekViewModel", "Use cached analysis")
                _analysis.value = index to cached
                return@launch
            }

            _analysis.value = index to "解析中..."
            val apiStart = System.currentTimeMillis()
            runCatching { api.analyze(question) }
                .onSuccess {
                    val apiDuration = System.currentTimeMillis() - apiStart
                    android.util.Log.d("DeepSeekViewModel", "API call duration=${apiDuration} ms")
                    android.util.Log.d("DeepSeekViewModel", "Analysis success: $it")
                    _analysis.value = index to it
                    saveAnalysis(question.id, it)
                }
                .onFailure {
                    val apiDuration = System.currentTimeMillis() - apiStart
                    android.util.Log.d("DeepSeekViewModel", "API call duration=${apiDuration} ms")
                    android.util.Log.e("DeepSeekViewModel", "Analysis failed", it)
                    _analysis.value = index to "解析失败: ${it.message}"
                }
        }
    }

    fun clear() {
        _analysis.value = null
    }
}