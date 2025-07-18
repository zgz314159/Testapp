package com.example.testapp.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.data.network.deepseek.DeepSeekApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class DeepSeekAskViewModel @Inject constructor(
    private val api: DeepSeekApiService
) : ViewModel() {
    private val _result = MutableStateFlow("解析中...")
    val result: StateFlow<String> = _result.asStateFlow()

    fun ask(text: String) {
        viewModelScope.launch {
            _result.value = "解析中..."
            runCatching { api.ask(text) }
                .onSuccess { _result.value = it }
                .onFailure { _result.value = "解析失败: ${'$'}{it.message}" }
        }
    }
}