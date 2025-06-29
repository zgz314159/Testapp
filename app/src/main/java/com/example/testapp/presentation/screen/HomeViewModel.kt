package com.example.testapp.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.usecase.GetQuestionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getQuestionsUseCase: GetQuestionsUseCase
) : ViewModel() {
    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()

    private val _fileNames = MutableStateFlow<List<String>>(emptyList())
    val fileNames: StateFlow<List<String>> = _fileNames.asStateFlow()

    init {
        viewModelScope.launch {
            getQuestionsUseCase().collect { list ->
                _questions.value = list
                // 提取所有不重复的 fileName，过滤 null
                _fileNames.value = list.mapNotNull { it.fileName }.distinct()
            }
        }
    }

    fun deleteFileAndData(fileName: String) {
        viewModelScope.launch {
            // 1. 删除所有属于该文件的题目（假设 Question 有 fileName 字段）
            getQuestionsUseCase.deleteQuestionsByFileName(fileName)
            // 2. 重新加载题库列表
            getQuestionsUseCase().collect { list ->
                _questions.value = list
                _fileNames.value = list.mapNotNull { it.fileName }.distinct()
            }
        }
    }
}
