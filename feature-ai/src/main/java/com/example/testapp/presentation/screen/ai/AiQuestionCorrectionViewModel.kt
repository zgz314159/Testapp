package com.example.testapp.presentation.screen.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.domain.model.QuestionCorrectionRequest
import com.example.testapp.domain.model.QuestionCorrectionSuggestion
import com.example.testapp.domain.usecase.CorrectQuestionWithAiUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AiQuestionCorrectionUiState {
    data object Idle : AiQuestionCorrectionUiState
    data object Loading : AiQuestionCorrectionUiState
    data class Success(val suggestion: QuestionCorrectionSuggestion) : AiQuestionCorrectionUiState
    data class Error(val message: String) : AiQuestionCorrectionUiState
}

@HiltViewModel
class AiQuestionCorrectionViewModel @Inject constructor(
    private val correctQuestionWithAi: CorrectQuestionWithAiUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AiQuestionCorrectionUiState>(AiQuestionCorrectionUiState.Idle)
    val uiState: StateFlow<AiQuestionCorrectionUiState> = _uiState.asStateFlow()

    fun correct(request: QuestionCorrectionRequest) {
        if (_uiState.value is AiQuestionCorrectionUiState.Loading) return
        viewModelScope.launch {
            _uiState.value = AiQuestionCorrectionUiState.Loading
            correctQuestionWithAi(request)
                .onSuccess { _uiState.value = AiQuestionCorrectionUiState.Success(it) }
                .onFailure {
                    _uiState.value = AiQuestionCorrectionUiState.Error(
                        it.message?.take(160) ?: "AI纠题失败",
                    )
                }
        }
    }

    fun dismissPreview() {
        _uiState.value = AiQuestionCorrectionUiState.Idle
    }
}
