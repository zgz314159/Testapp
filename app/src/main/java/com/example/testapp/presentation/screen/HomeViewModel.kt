package com.example.testapp.presentation.screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.usecase.ClearPracticeProgressUseCase
import com.example.testapp.domain.usecase.GetQuestionsUseCase
import com.example.testapp.domain.usecase.RemoveFavoriteQuestionsByFileNameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getQuestionsUseCase: GetQuestionsUseCase,
    private val clearPracticeProgressUseCase: ClearPracticeProgressUseCase,
    private val removeFavoriteQuestionsByFileNameUseCase: RemoveFavoriteQuestionsByFileNameUseCase
) : ViewModel() {
    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()

    private val _fileNames = MutableStateFlow<List<String>>(emptyList())
    val fileNames: StateFlow<List<String>> = _fileNames.asStateFlow()

    init {
        viewModelScope.launch {
            getQuestionsUseCase().collect { list ->
                _questions.value = list
                _fileNames.value = list.mapNotNull { it.fileName }.distinct()
                Log.d("HomeVM", "[collect] fileNames: $_fileNames, questions.size: ${list.size}")
            }
        }
    }

    fun deleteFileAndData(fileName: String, onDeleted: (() -> Unit)? = null) {
        viewModelScope.launch {
            Log.d("HomeVM", "[deleteFileAndData] before: fileNames=$_fileNames, selectedFile=$fileName")
            getQuestionsUseCase.deleteQuestionsByFileName(fileName)
            // 使用带前缀的进度 ID，避免与其他模式冲突
            clearPracticeProgressUseCase("practice_${fileName}")
            removeFavoriteQuestionsByFileNameUseCase(fileName) // 一行，批量删收藏
            // 等待数据库变更后再 collect 一次，确保刷新
            val list = getQuestionsUseCase().first()
            _questions.value = list
            val names = list.mapNotNull { it.fileName }.distinct()
            _fileNames.value = names
            Log.d("HomeVM", "[deleteFileAndData] after: fileNames=$_fileNames, questions.size=${list.size}")
            onDeleted?.invoke()
        }
    }
}
