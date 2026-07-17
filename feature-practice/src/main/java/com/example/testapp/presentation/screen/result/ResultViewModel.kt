package com.example.testapp.presentation.screen.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.domain.model.HistoryRecord
import com.example.testapp.domain.usecase.GetHistoryListByFileNamesUseCase
import com.example.testapp.domain.usecase.GetHistoryListByFileUseCase
import com.example.testapp.domain.usecase.GetQuestionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val getHistoryListByFileUseCase: GetHistoryListByFileUseCase,
    private val getHistoryListByFileNamesUseCase: GetHistoryListByFileNamesUseCase,
    private val getQuestionsUseCase: GetQuestionsUseCase
) : ViewModel() {
    private val _history = MutableStateFlow<List<HistoryRecord>>(emptyList())
    val history: StateFlow<List<HistoryRecord>> = _history.asStateFlow()

    private val _totalQuestions = MutableStateFlow(0)
    val totalQuestions: StateFlow<Int> = _totalQuestions.asStateFlow()

    fun load(fileName: String) {
        viewModelScope.launch {
            val cleanFileName =
                fileName
                    .removePrefix("exam_")
                    .removePrefix("practice_")
                    .removePrefix("adaptive_")
            if (fileName.startsWith("adaptive_")) {
                _history.value = emptyList()
                _totalQuestions.value = getQuestionsUseCase(cleanFileName).first().size
                return@launch
            }
            val historyFlow = if (fileName.startsWith("exam_") || fileName.startsWith("practice_")) {
                getHistoryListByFileUseCase(fileName)
            } else {
                getHistoryListByFileNamesUseCase(listOf("exam_$cleanFileName", "practice_$cleanFileName"))
            }
            _history.value = historyFlow.first()
            _totalQuestions.value = getQuestionsUseCase(cleanFileName).first().size
        }
    }
}
