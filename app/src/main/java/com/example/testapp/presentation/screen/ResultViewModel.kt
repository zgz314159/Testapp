package com.example.testapp.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.domain.model.HistoryRecord
import com.example.testapp.domain.usecase.GetHistoryListByFileUseCase
import com.example.testapp.domain.usecase.GetHistoryListByFileNamesUseCase
import com.example.testapp.domain.usecase.GetHistoryListUseCase
import com.example.testapp.domain.usecase.GetWrongBookUseCase
import com.example.testapp.domain.usecase.GetQuestionsUseCase
import com.example.testapp.domain.model.WrongQuestion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val getHistoryListByFileUseCase: GetHistoryListByFileUseCase,
    private val getHistoryListByFileNamesUseCase: GetHistoryListByFileNamesUseCase,
    private val getHistoryListUseCase: GetHistoryListUseCase,
    private val getWrongBookUseCase: GetWrongBookUseCase,
    private val getQuestionsUseCase: GetQuestionsUseCase
) : ViewModel() {
    private val _history = MutableStateFlow<List<HistoryRecord>>(emptyList())
    val history: StateFlow<List<HistoryRecord>> = _history.asStateFlow()

    private val _allHistory = MutableStateFlow<List<HistoryRecord>>(emptyList())
    val allHistory: StateFlow<List<HistoryRecord>> = _allHistory.asStateFlow()

    private val _wrongBook = MutableStateFlow<List<WrongQuestion>>(emptyList())
    val wrongBook: StateFlow<List<WrongQuestion>> = _wrongBook.asStateFlow()

    private val _totalQuestions = MutableStateFlow<Int>(0)
    val totalQuestions: StateFlow<Int> = _totalQuestions.asStateFlow()

    fun load(fileName: String) {

        // 协程1: 加载当前文件的历史记录 - 最重要的数据源
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
        
        // 延迟启动其他协程，避免竞争条件
        viewModelScope.launch {
            kotlinx.coroutines.delay(50) // 延迟50ms
            getHistoryListUseCase().collect {
                _allHistory.value = it
                
            }
        }
        
        viewModelScope.launch {
            kotlinx.coroutines.delay(100) // 延迟100ms
            getWrongBookUseCase().collect {
                _wrongBook.value = it
                
            }
        }
        
        // 协程4: 加载完整题库信息用于计算"整张练习"统计
        viewModelScope.launch {
            kotlinx.coroutines.delay(150) // 延迟150ms
            val cleanFileName = fileName.removePrefix("exam_").removePrefix("practice_")
            getQuestionsUseCase(cleanFileName).collect { questions ->
                _totalQuestions.value = questions.size
                
            }
        }

    }
}