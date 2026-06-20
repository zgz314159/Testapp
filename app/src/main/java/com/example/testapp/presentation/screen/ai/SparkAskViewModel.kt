package com.example.testapp.presentation.screen.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.data.network.spark.SparkApiService
import com.example.testapp.domain.usecase.GetSparkAskResultUseCase
import com.example.testapp.domain.usecase.SaveSparkAskResultUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SparkAskViewModel @Inject constructor(
    private val api: SparkApiService,
    private val getResultUseCase: GetSparkAskResultUseCase,
    private val saveResultUseCase: SaveSparkAskResultUseCase
) : ViewModel() {
    companion object {
        private const val PARSING = "解析中..."
        private const val PARSE_FAILED_PREFIX = "解析失败"
    }

    private val _result = MutableStateFlow(PARSING)
    val result: StateFlow<String> = _result.asStateFlow()

    fun reset() {
        _result.value = ""
    }

    suspend fun getSavedNote(questionId: Int): String? = getResultUseCase(questionId)

    fun save(questionId: Int, note: String) {
        viewModelScope.launch { saveResultUseCase(questionId, note) }
    }

    fun ask(text: String) {
        viewModelScope.launch {
            _result.value = PARSING
            runCatching { api.ask(text) }
                .onSuccess { _result.value = it }
                .onFailure { _result.value = "$PARSE_FAILED_PREFIX: ${it.message ?: "未知错误"}" }
        }
    }
}