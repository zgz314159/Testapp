package com.example.testapp.presentation.screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.usecase.ClearPracticeProgressUseCase
import com.example.testapp.domain.usecase.ClearExamProgressUseCase
import com.example.testapp.domain.usecase.GetQuestionsUseCase
import com.example.testapp.domain.usecase.GetPracticeProgressFlowUseCase
import com.example.testapp.domain.usecase.RemoveFavoriteQuestionsByFileNameUseCase
import com.example.testapp.domain.usecase.RemoveWrongQuestionsByFileNameUseCase
import com.example.testapp.domain.usecase.RemoveHistoryRecordsByFileNameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getQuestionsUseCase: GetQuestionsUseCase,
    private val clearPracticeProgressUseCase: ClearPracticeProgressUseCase,
    private val clearExamProgressUseCase: ClearExamProgressUseCase,
    private val removeFavoriteQuestionsByFileNameUseCase: RemoveFavoriteQuestionsByFileNameUseCase,
    private val removeWrongQuestionsByFileNameUseCase: RemoveWrongQuestionsByFileNameUseCase,
    private val removeHistoryRecordsByFileNameUseCase: RemoveHistoryRecordsByFileNameUseCase,
    private val getPracticeProgressFlowUseCase: GetPracticeProgressFlowUseCase,
) : ViewModel() {
    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()

    private val _fileNames = MutableStateFlow<List<String>>(emptyList())
    val fileNames: StateFlow<List<String>> = _fileNames.asStateFlow()
    private val _practiceProgress = MutableStateFlow<Map<String, Int>>(emptyMap())
    val practiceProgress: StateFlow<Map<String, Int>> = _practiceProgress.asStateFlow()

    private val progressJobs = mutableMapOf<String, kotlinx.coroutines.Job>()
    init {
        viewModelScope.launch {
            getQuestionsUseCase().collect { list ->
                _questions.value = list
                val names = list.mapNotNull { it.fileName }.distinct()
                _fileNames.value = names
                updateProgressCollectors(names)
                Log.d("HomeVM", "[collect] fileNames: $_fileNames, questions.size: ${list.size}")
            }
        }
    }

    private fun updateProgressCollectors(names: List<String>) {
        val toRemove = progressJobs.keys - names.toSet()
        toRemove.forEach {
            progressJobs[it]?.cancel()
            progressJobs.remove(it)
            _practiceProgress.update { map -> map - it }
        }
        val toAdd = names.filter { it !in progressJobs }
        toAdd.forEach { name ->
            progressJobs[name] = viewModelScope.launch {
                getPracticeProgressFlowUseCase("practice_${name}").collect { progress ->
                    val idx = progress?.currentIndex?.plus(1) ?: 0
                    _practiceProgress.update { map -> map + (name to idx) }
                }
            }
        }
    }

    fun deleteFileAndData(fileName: String, onDeleted: (() -> Unit)? = null) {
        viewModelScope.launch {
            Log.d("HomeVM", "[deleteFileAndData] before: fileNames=$_fileNames, selectedFile=$fileName")
            getQuestionsUseCase.deleteQuestionsByFileName(fileName)
            // 使用带前缀的进度 ID，避免与其他模式冲突
            clearPracticeProgressUseCase("practice_${fileName}")
            clearExamProgressUseCase("exam_${fileName}")
            removeFavoriteQuestionsByFileNameUseCase(fileName) // 一行，批量删收藏
            removeWrongQuestionsByFileNameUseCase(fileName) // 新增，删除对应错题
            removeHistoryRecordsByFileNameUseCase("practice_${fileName}")
            removeHistoryRecordsByFileNameUseCase("exam_${fileName}")
            // 等待数据库变更后再 collect 一次，确保刷新
            val list = getQuestionsUseCase().first()
            _questions.value = list
            val names = list.mapNotNull { it.fileName }.distinct()
            _fileNames.value = names
            updateProgressCollectors(names)
            Log.d("HomeVM", "[deleteFileAndData] after: fileNames=$_fileNames, questions.size=${list.size}")
            onDeleted?.invoke()
        }
    }
}
