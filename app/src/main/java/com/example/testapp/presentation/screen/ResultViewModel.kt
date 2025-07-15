package com.example.testapp.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.domain.model.HistoryRecord
import com.example.testapp.domain.usecase.GetHistoryListByFileUseCase
import com.example.testapp.domain.usecase.GetHistoryListByFileNamesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val getHistoryListByFileUseCase: GetHistoryListByFileUseCase,
    private val getHistoryListByFileNamesUseCase: GetHistoryListByFileNamesUseCase
) : ViewModel() {
    private val _history = MutableStateFlow<List<HistoryRecord>>(emptyList())
    val history: StateFlow<List<HistoryRecord>> = _history.asStateFlow()

    fun load(fileName: String) {
        viewModelScope.launch {
            val flow = if (fileName.startsWith("exam_") || fileName.startsWith("practice_")) {
                getHistoryListByFileUseCase(fileName)
            } else {
                val names = listOf("exam_" + fileName, "practice_" + fileName)
                getHistoryListByFileNamesUseCase(names)
            }
            flow.collect {
                _history.value = it
            }
        }
    }
}