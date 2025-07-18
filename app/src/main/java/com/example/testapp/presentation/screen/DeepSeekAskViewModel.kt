package com.example.testapp.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.data.network.deepseek.DeepSeekApiService
import com.example.testapp.domain.usecase.GetQuestionNoteUseCase
import com.example.testapp.domain.usecase.SaveQuestionNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class DeepSeekAskViewModel @Inject constructor(
    private val api: DeepSeekApiService,
    private val getNoteUseCase: GetQuestionNoteUseCase,
    private val saveNoteUseCase: SaveQuestionNoteUseCase
) : ViewModel() {
    private val _result = MutableStateFlow("解析中...")
    val result: StateFlow<String> = _result.asStateFlow()

    suspend fun getSavedNote(questionId: Int): String? = getNoteUseCase(questionId)

    fun save(questionId: Int, note: String) {
        viewModelScope.launch { saveNoteUseCase(questionId, note) }
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