package com.example.testapp.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.data.network.DeepSeekApiService
import com.example.testapp.domain.model.Question
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
    private val api: DeepSeekApiService
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
            _analysis.value = index to "解析中..."
            runCatching { api.analyze(question) }
                .onSuccess {
                    android.util.Log.d("DeepSeekViewModel", "Analysis success: $it")
                    _analysis.value = index to it
                }
                .onFailure {
                    android.util.Log.e("DeepSeekViewModel", "Analysis failed", it)
                    _analysis.value = index to "解析失败: ${'$'}{it.message}"
                }
        }
    }

    fun clear() {
        _analysis.value = null
    }
}