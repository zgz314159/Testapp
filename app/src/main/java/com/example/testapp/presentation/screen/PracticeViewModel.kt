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
import com.example.testapp.domain.usecase.GetQuestionAnalysisUseCase
import com.example.testapp.domain.usecase.GetQuestionNoteUseCase
import com.example.testapp.domain.usecase.SaveQuestionNoteUseCase
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
    private val getFavoriteQuestionsUseCase: GetFavoriteQuestionsUseCase,
    private val getQuestionAnalysisUseCase: GetQuestionAnalysisUseCase,
    private val getQuestionNoteUseCase: GetQuestionNoteUseCase,
    private val saveQuestionNoteUseCase: SaveQuestionNoteUseCase
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

    private val _analysisList = MutableStateFlow<List<String>>(emptyList())
    val analysisList: StateFlow<List<String>> = _analysisList.asStateFlow()

    private val _noteList = MutableStateFlow<List<String>>(emptyList())
    val noteList: StateFlow<List<String>> = _noteList.asStateFlow()

    private var progressId: String = ""

    val currentProgressId: String
        get() = progressId

    private var questionSourceId: String = ""
    private var randomPracticeEnabled: Boolean = false
    private var analysisLoaded: Boolean = false
    private var notesLoaded: Boolean = false

    init {
        // 应用启动时，清理任何旧的 default 记录，防止误删到别的表
        viewModelScope.launch {
            clearPracticeProgressUseCase("practice_default")
        }
    }


    fun setRandomPractice(enabled: Boolean) {
        randomPracticeEnabled = enabled
    }

    private fun ensurePrefix(id: String): String =
        if (id.startsWith("practice_")) id else "practice_$id"


    fun setProgressId(
        id: String,
        questionsId: String = id,
        loadQuestions: Boolean = true,
        questionCount: Int = 0
    ) {
        // 1. 统一给练习进度加 practice_ 前缀
        progressId = ensurePrefix(id)
        questionSourceId = questionsId
        _progressLoaded.value = false
        analysisLoaded = false
        notesLoaded = false
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
                    val ordered = if (randomPracticeEnabled) qs.shuffled() else qs
                    val trimmed = if (questionCount > 0) ordered.take(questionCount.coerceAtMost(ordered.size)) else ordered
                    android.util.Log.d(
                        "PracticeDebug",
                        "加载题库: progressId=$progressId random=$randomPracticeEnabled count=$questionCount"
                    )
                    _questions.value = trimmed
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
                    _currentIndex.value =
                        progress.currentIndex.coerceAtMost(_questions.value.size - 1)
                    // answeredList 直接使用已答题目下标列表，过滤非法值
                    _answeredList.value = progress.answeredList.filter { it in _questions.value.indices }
                    _selectedOptions.value = emptyList()
                    _selectedOptions.value =
                        if (progress.selectedOptions.size >= _questions.value.size) {
                            progress.selectedOptions.take(_questions.value.size).toList()
                        } else {
                            (progress.selectedOptions +
                                    List(_questions.value.size - progress.selectedOptions.size) { emptyList() }).toList()
                        }
                    _showResultList.value = emptyList()
                    _showResultList.value =
                        if (progress.showResultList.size >= _questions.value.size) {
                            progress.showResultList.take(_questions.value.size).toList()
                        } else {
                            (progress.showResultList +
                                    List(_questions.value.size - progress.showResultList.size) { false }).toList()
                        }

                    _analysisList.value = emptyList()
                    _analysisList.value =
                        if (progress.analysisList.size >= _questions.value.size) {
                            progress.analysisList.take(_questions.value.size).toList()
                        } else {
                            (progress.analysisList +
                                    List(_questions.value.size - progress.analysisList.size) { "" }).toList()
                        }

                    android.util.Log.d(
                        "PracticeDebug",
                        "恢复进度: currentIndex=${progress.currentIndex}, answeredList=${progress.answeredList}, selectedOptions=${progress.selectedOptions}, showResultList=${progress.showResultList}"
                    )
                } else if (progress == null && !_progressLoaded.value) {
                    android.util.Log.d("PracticeDebug", "no existing progress, initializing")
                    _currentIndex.value = 0
                    _answeredList.value = emptyList()
                    _selectedOptions.value = List(_questions.value.size) { emptyList() }
                    _showResultList.value = List(_questions.value.size) { false }
                    _analysisList.value = List(_questions.value.size) { "" }
                    saveProgress()
                }
                _progressLoaded.value = true
            }
            if (!analysisLoaded) {
                loadAnalysisFromRepository()
                analysisLoaded = true
            }
            if (!notesLoaded) {
                loadNotesFromRepository()
                notesLoaded = true
            }
        }
    }
    private suspend fun loadAnalysisFromRepository() {
        val qs = _questions.value
        val list = _analysisList.value.toMutableList()
        var changed = false
        qs.forEachIndexed { idx, q ->
            if (idx >= list.size) list.add("")
            if (list[idx].isBlank()) {
                val text = getQuestionAnalysisUseCase(q.id)
                if (!text.isNullOrBlank()) {
                    list[idx] = text
                    changed = true
                }
            }
        }
        if (changed) {
            _analysisList.value = list
            saveProgress()
        }
    }
    private suspend fun loadNotesFromRepository() {
        val qs = _questions.value
        val list = _noteList.value.toMutableList()
        var changed = false
        qs.forEachIndexed { idx, q ->
            if (idx >= list.size) list.add("")
            if (list[idx].isBlank()) {
                val text = getQuestionNoteUseCase(q.id)
                if (!text.isNullOrBlank()) {
                    list[idx] = text
                    changed = true
                }
            }
        }
        if (changed) {
            _noteList.value = list
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
                    analysisList = _analysisList.value,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun clearProgress() {
        viewModelScope.launch {
            android.util.Log.d("PracticeDebug", "clearProgress called for $progressId")
            clearPracticeProgressUseCase(progressId)
            resetLocalState()
            _progressLoaded.value = false
            analysisLoaded = false
            notesLoaded = false
        }
    }

    private fun resetLocalState() {
        val count = _questions.value.size
        _currentIndex.value = 0
        _answeredList.value = emptyList()
        _selectedOptions.value = List(count) { emptyList() }
        _showResultList.value = List(count) { false }
        _analysisList.value = List(count) { "" }
    }

    fun updateShowResult(index: Int, value: Boolean) {
        android.util.Log.d("PracticeDebug", "updateShowResult index=$index value=$value")
        val list = _showResultList.value.toMutableList()
        while (list.size <= index) list.add(false)
        list[index] = value
        _showResultList.value = list
        saveProgress()
    }
    fun updateAnalysis(index: Int, text: String) {
        android.util.Log.d("PracticeDebug", "updateAnalysis index=$index text=$text")
        val list = _analysisList.value.toMutableList()
        while (list.size <= index) list.add("")
        list[index] = text
        _analysisList.value = list
        saveProgress()
    }

    fun saveNote(questionId: Int, index: Int, text: String) {
        viewModelScope.launch {
            saveQuestionNoteUseCase(questionId, text)
        }
        val list = _noteList.value.toMutableList()
        while (list.size <= index) list.add("")
        list[index] = text
        _noteList.value = list
    }

    suspend fun getNote(questionId: Int): String? = getQuestionNoteUseCase(questionId)

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
