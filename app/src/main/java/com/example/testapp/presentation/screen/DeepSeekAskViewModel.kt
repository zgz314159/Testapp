package com.example.testapp.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.data.network.deepseek.DeepSeekApiService
import com.example.testapp.domain.usecase.GetDeepSeekAskResultUseCase
import com.example.testapp.domain.usecase.SaveDeepSeekAskResultUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class DeepSeekAskViewModel @Inject constructor(
    private val api: DeepSeekApiService,
    private val getResultUseCase: GetDeepSeekAskResultUseCase,
    private val saveResultUseCase: SaveDeepSeekAskResultUseCase
) : ViewModel() {
    private val _result = MutableStateFlow("解析中...")
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
            _result.value = "解析中..."
            runCatching { api.ask(text) }
                .onSuccess { _result.value = it }
                .onFailure { _result.value = "解析失败: ${'$'}{it.message}" }
        }
    }
}