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

@HiltViewModel
class DeepSeekViewModel @Inject constructor(
    private val api: DeepSeekApiService
) : ViewModel() {
    private val _analysis = MutableStateFlow<String?>(null)
    val analysis: StateFlow<String?> = _analysis.asStateFlow()

    fun analyze(question: Question) {
        viewModelScope.launch {
            _analysis.value = "解析中..."
            runCatching { api.analyze(question) }
                .onSuccess { _analysis.value = it }
                .onFailure { _analysis.value = "解析失败: ${'$'}{it.message}" }
        }
    }

    fun clear() {
        _analysis.value = null
    }
}