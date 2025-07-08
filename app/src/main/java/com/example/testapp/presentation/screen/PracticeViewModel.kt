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

    private val _selectedOptions = MutableStateFlow<List<List<Int>>>(emptyList())
    val selectedOptions: StateFlow<List<List<Int>>> = _selectedOptions.asStateFlow()

    private val _progressLoaded = MutableStateFlow(false)
    val progressLoaded: StateFlow<Boolean> = _progressLoaded.asStateFlow()

    private val _showResultList = MutableStateFlow<List<Boolean>>(emptyList())
    val showResultList: StateFlow<List<Boolean>> = _showResultList.asStateFlow()

    private var progressId: String = "default"
    private var questionSourceId: String = "default"
    private var randomPracticeEnabled: Boolean = false



    fun setRandomPractice(enabled: Boolean) {
        randomPracticeEnabled = enabled
    }
    fun setProgressId(id: String, questionsId: String = id, loadQuestions: Boolean = true) {
        progressId = id
        questionSourceId = questionsId
        _progressLoaded.value = false
        if (loadQuestions) {
            viewModelScope.launch {
                if (randomPracticeEnabled) {
                    clearPracticeProgressUseCase(progressId)
                }
                getQuestionsUseCase(questionSourceId).collect { qs ->
                    android.util.Log.d(
                        "PracticeDebug",
                        "getQuestionsUseCase 收到题目数量: ${qs.size}"
                    )
                    val list = if (randomPracticeEnabled) qs.shuffled() else qs
                    android.util.Log.d(
                        "PracticeDebug",
                        "加载题库: progressId=$progressId random=$randomPracticeEnabled"
                    )
                    _questions.value = list
                    loadProgress()
                }
            }
        }

    }

    private fun loadProgress() {
        viewModelScope.launch {
            getPracticeProgressFlowUseCase(progressId).collect { progress ->
                android.util.Log.d(
                    "PracticeDebug",
                    "loadProgress: progress=$progress, progressId=$progressId"
                )
                if (progress != null && !_progressLoaded.value) {
                    _currentIndex.value = progress.currentIndex.coerceAtMost(_questions.value.size - 1)
                    // answeredList 补齐长度
                    _answeredList.value = if (progress.answeredList.size >= _questions.value.size) {
                        progress.answeredList.take(_questions.value.size).toList()
                    } else {
                        (progress.answeredList + List(_questions.value.size - progress.answeredList.size) { -1 }).toList()
                    }
                    _selectedOptions.value = emptyList()
                    _selectedOptions.value = if (progress.selectedOptions.size >= _questions.value.size) {
                        progress.selectedOptions.take(_questions.value.size).toList()
                    } else {
                        (progress.selectedOptions +
                                List(_questions.value.size - progress.selectedOptions.size) { emptyList() }).toList()
                    }
                    _showResultList.value = emptyList()
                    _showResultList.value = if (progress.showResultList.size >= _questions.value.size) {
                        progress.showResultList.take(_questions.value.size).toList()
                    } else {
                        (progress.showResultList +
                                List(_questions.value.size - progress.showResultList.size) { false }).toList()
                    }
                    android.util.Log.d(
                        "PracticeDebug",
                        "恢复进度: currentIndex=${progress.currentIndex}, answeredList=${progress.answeredList}, selectedOptions=${progress.selectedOptions}, showResultList=${progress.showResultList}"
                    )
                } else if (progress == null && !_progressLoaded.value) {
                    android.util.Log.d("PracticeDebug", "no existing progress, initializing")
                    _currentIndex.value = 0
                    _answeredList.value = List(_questions.value.size) { -1 }
                    _selectedOptions.value = List(_questions.value.size) { emptyList() }
                    _showResultList.value = List(_questions.value.size) { false }
                    saveProgress()
                }
                _progressLoaded.value = true
            }
        }
    }

    fun answerQuestion(option: Int) {
        val idx = _currentIndex.value
        android.util.Log.d("PracticeDebug", "answerQuestion index=$idx option=$option")
        val updatedAnswered = if (!_answeredList.value.contains(idx)) _answeredList.value + idx else _answeredList.value
        val updatedSelected = _selectedOptions.value.toMutableList().apply {
            if (size > idx) this[idx] = listOf(option) else add(listOf(option))
        }
        _answeredList.value = updatedAnswered
        _selectedOptions.value = updatedSelected
        // ✅ 关键点：标记当前题目已经显示了答题结果
        updateShowResult(idx, true)
        saveProgress()
    }

    fun toggleOption(option: Int) {
        val idx = _currentIndex.value
        val list = _selectedOptions.value.toMutableList()
        while (list.size <= idx) list.add(emptyList())
        val current = list[idx].toMutableList()
        if (current.contains(option)) current.remove(option) else current.add(option)
        list[idx] = current
        _selectedOptions.value = list
        saveProgress()
    }


    fun nextQuestion() {
        if (_currentIndex.value < _questions.value.size - 1) {
            android.util.Log.d("PracticeDebug", "nextQuestion from ${_currentIndex.value}")
            _currentIndex.value += 1
            saveProgress()
        }
    }

    fun prevQuestion() {
        if (_currentIndex.value > 0) {
            android.util.Log.d("PracticeDebug", "prevQuestion from ${_currentIndex.value}")
            _currentIndex.value -= 1
            saveProgress()
        }
    }
    fun goToQuestion(index: Int) {
        if (index in _questions.value.indices) {
            android.util.Log.d("PracticeDebug", "goToQuestion $index")
            _currentIndex.value = index
            saveProgress()
        }
    }


    fun saveProgress() {
        viewModelScope.launch {
            android.util.Log.d(
                "PracticeDebug",
                "saveProgress: index=${_currentIndex.value} answered=${_answeredList.value} selected=${_selectedOptions.value} showResult=${_showResultList.value}"
            )
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
            android.util.Log.d("PracticeDebug", "clearProgress called for $progressId")
            clearPracticeProgressUseCase(progressId)
            _currentIndex.value = 0
            _answeredList.value = emptyList()
            _selectedOptions.value = emptyList()
            _showResultList.value = emptyList()
            _progressLoaded.value = false
        }
    }

    fun updateShowResult(index: Int, value: Boolean) {
        android.util.Log.d("PracticeDebug", "updateShowResult index=$index value=$value")
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
                // 刷新题库并保持当前的进度 ID
                setProgressId(progressId, questionSourceId)
            }
        }
    }

    fun loadWrongQuestions(fileName: String) {
        viewModelScope.launch {
            // 获取指定文件下的错题并加载进度
            getWrongBookUseCase().collect { wrongList ->
                val filtered = wrongList.filter { it.question.fileName == fileName }
                val list = filtered.map { it.question }
                _questions.value = if (randomPracticeEnabled) list.shuffled() else list
                // 重置进度相关状态
                loadProgress()
            }
        }
    }
    fun loadFavoriteQuestions(fileName: String) {
        viewModelScope.launch {
            getFavoriteQuestionsUseCase().collect { favList ->
                val filtered = favList.filter { it.question.fileName == fileName }
                val list = filtered.map { it.question }
                _questions.value = if (randomPracticeEnabled) list.shuffled() else list
                loadProgress()
            }
        }
    }
}
