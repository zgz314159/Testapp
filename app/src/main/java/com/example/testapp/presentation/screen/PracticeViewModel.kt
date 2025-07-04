package com.example.testapp.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.domain.model.PracticeProgress
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.usecase.ClearPracticeProgressUseCase
import com.example.testapp.domain.usecase.GetPracticeProgressFlowUseCase
import com.example.testapp.domain.usecase.GetQuestionsUseCase
import com.example.testapp.domain.usecase.SavePracticeProgressUseCase
import com.example.testapp.domain.usecase.SaveQuestionsUseCase
import com.example.testapp.domain.usecase.GetWrongBookUseCase
import com.example.testapp.domain.usecase.GetFavoriteQuestionsUseCase

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PracticeViewModel @Inject constructor(
    private val getQuestionsUseCase: GetQuestionsUseCase,
    private val getPracticeProgressFlowUseCase: GetPracticeProgressFlowUseCase,
    private val savePracticeProgressUseCase: SavePracticeProgressUseCase,
    private val clearPracticeProgressUseCase: ClearPracticeProgressUseCase,
    private val saveQuestionsUseCase: SaveQuestionsUseCase, // 新增注入
    private val getWrongBookUseCase: GetWrongBookUseCase,
    private val getFavoriteQuestionsUseCase: GetFavoriteQuestionsUseCase
) : ViewModel() {
    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _answeredList = MutableStateFlow<List<Int>>(emptyList())
    val answeredList: StateFlow<List<Int>> = _answeredList.asStateFlow()

    private val _selectedOptions = MutableStateFlow<List<Int>>(emptyList())
    val selectedOptions: StateFlow<List<Int>> = _selectedOptions.asStateFlow()

    private val _progressLoaded = MutableStateFlow(false)
    val progressLoaded: StateFlow<Boolean> = _progressLoaded.asStateFlow()

    private val _showResultList = MutableStateFlow<List<Boolean>>(emptyList())
    val showResultList: StateFlow<List<Boolean>> = _showResultList.asStateFlow()

    private var progressId: String = "default"

    fun setProgressId(id: String, loadQuestions: Boolean = true) {
        progressId = id
        if (loadQuestions) {
            viewModelScope.launch {
                getQuestionsUseCase(progressId).collect { qs ->
                    android.util.Log.d("PracticeDebug", "getQuestionsUseCase 收到题目数量: ${qs.size}")
                    _questions.value = qs
                    loadProgress()
                }
            }
        }
        else {
            // 如果不加载题目，则直接加载进度
            loadProgress()
        }
    }

    private fun loadProgress() {
        viewModelScope.launch {
            getPracticeProgressFlowUseCase(progressId).collect { progress ->
                android.util.Log.d("PracticeDebug", "loadProgress: progress=$progress, progressId=$progressId")
                if (progress != null && !_progressLoaded.value) {
                    _currentIndex.value = progress.currentIndex
                    _answeredList.value = progress.answeredList
                    _selectedOptions.value = progress.selectedOptions
                    _showResultList.value = progress.showResultList
                    android.util.Log.d(
                        "PracticeDebug",
                        "恢复进度: currentIndex=${progress.currentIndex}, answeredList=${progress.answeredList}, selectedOptions=${progress.selectedOptions}, showResultList=${progress.showResultList}"
                    )
                }
                _progressLoaded.value = true
            }
        }
    }

    fun answerQuestion(option: Int) {
        val idx = _currentIndex.value
        val updatedAnswered = if (!_answeredList.value.contains(idx)) _answeredList.value + idx else _answeredList.value
        val updatedSelected = _selectedOptions.value.toMutableList().apply {
            if (size > idx) this[idx] = option else add(option)
        }
        _answeredList.value = updatedAnswered
        _selectedOptions.value = updatedSelected
        saveProgress()
    }

    fun nextQuestion() {
        if (_currentIndex.value < _questions.value.size - 1) {
            _currentIndex.value += 1
            saveProgress()
        }
    }

    fun prevQuestion() {
        if (_currentIndex.value > 0) {
            _currentIndex.value -= 1
            saveProgress()
        }
    }

    fun saveProgress() {
        viewModelScope.launch {
            savePracticeProgressUseCase(
                PracticeProgress(
                    id = progressId,
                    currentIndex = _currentIndex.value,
                    answeredList = _answeredList.value,
                    selectedOptions = _selectedOptions.value,
                    showResultList = _showResultList.value,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun clearProgress() {
        viewModelScope.launch {
            clearPracticeProgressUseCase(progressId)
            _currentIndex.value = 0
            _answeredList.value = emptyList()
            _selectedOptions.value = emptyList()
            _showResultList.value = emptyList()
            _progressLoaded.value = false
        }
    }

    fun updateShowResult(index: Int, value: Boolean) {
        val list = _showResultList.value.toMutableList()
        while (list.size <= index) list.add(false)
        list[index] = value
        _showResultList.value = list
        saveProgress()
    }

    fun updateQuestionContent(index: Int, newContent: String) {
        val updatedList = _questions.value.toMutableList()
        if (index in updatedList.indices) {
            val old = updatedList[index]
            updatedList[index] = old.copy(content = newContent)
            _questions.value = updatedList
            // 持久化到本地 JSON 文件
            viewModelScope.launch {
                saveQuestionsUseCase(old.fileName ?: "default.json", updatedList.filter { it.fileName == old.fileName })
            }
        }
    }

    fun updateQuestionAllFields(index: Int, newContent: String, newOptions: List<String>, newAnswer: String, newExplanation: String) {
        val updatedList = _questions.value.toMutableList()
        if (index in updatedList.indices) {
            val old = updatedList[index]
            updatedList[index] = old.copy(
                content = newContent,
                options = newOptions,
                answer = newAnswer,
                explanation = newExplanation
            )
            _questions.value = updatedList
            // 持久化到本地 JSON 文件
            viewModelScope.launch {
                saveQuestionsUseCase(old.fileName ?: "default.json", updatedList.filter { it.fileName == old.fileName })
                // 保存后强制刷新题库内容
                setProgressId(progressId)
            }
        }
    }

    fun loadWrongQuestions(fileName: String) {
        viewModelScope.launch {
            // 获取指定文件下的错题并加载进度
            getWrongBookUseCase().collect { wrongList ->
                val filtered = wrongList.filter { it.question.fileName == fileName }
                _questions.value = filtered.map { it.question }
                // 重置进度相关状态
                loadProgress()
            }
        }
    }
    fun loadFavoriteQuestions(fileName: String) {
        viewModelScope.launch {
            getFavoriteQuestionsUseCase().collect { favList ->
                val filtered = favList.filter { it.question.fileName == fileName }
                _questions.value = filtered.map { it.question }
                loadProgress()
            }
        }
    }
}
