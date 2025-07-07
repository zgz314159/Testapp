package com.example.testapp.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.WrongQuestion
import com.example.testapp.domain.model.HistoryRecord
import com.example.testapp.domain.usecase.GetQuestionsUseCase
import com.example.testapp.domain.usecase.AddWrongQuestionUseCase
import com.example.testapp.domain.usecase.AddHistoryRecordUseCase
import com.example.testapp.domain.usecase.SaveExamProgressUseCase
import com.example.testapp.domain.usecase.GetExamProgressFlowUseCase
import com.example.testapp.domain.usecase.ClearExamProgressUseCase
import kotlinx.coroutines.flow.firstOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.testapp.util.answerLetterToIndex

@HiltViewModel
class ExamViewModel @Inject constructor(
    private val getQuestionsUseCase: GetQuestionsUseCase,
    private val addWrongQuestionUseCase: AddWrongQuestionUseCase,
    private val addHistoryRecordUseCase: AddHistoryRecordUseCase,
    private val saveExamProgressUseCase: SaveExamProgressUseCase,
    private val getExamProgressFlowUseCase: GetExamProgressFlowUseCase,
    private val clearExamProgressUseCase: ClearExamProgressUseCase
) : ViewModel() {
    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _selectedOptions = MutableStateFlow<List<Int>>(emptyList())
    val selectedOptions: StateFlow<List<Int>> = _selectedOptions.asStateFlow()
    private val _showResultList = MutableStateFlow<List<Boolean>>(emptyList())
    val showResultList: StateFlow<List<Boolean>> = _showResultList.asStateFlow()

    private val _progressLoaded = MutableStateFlow(false)
    val progressLoaded: StateFlow<Boolean> = _progressLoaded.asStateFlow()

    private val _finished = MutableStateFlow(false)
    val finished: StateFlow<Boolean> = _finished.asStateFlow()

    private var progressId: String = "default"

    fun loadQuestions(quizId: String, count: Int) {
        // 考试模式也使用前缀区分进度
        progressId = "exam_${quizId}"
        _progressLoaded.value = false
        viewModelScope.launch {
            android.util.Log.d("ExamDebug", "loadQuestions start id=$quizId count=$count")
            val existing = getExamProgressFlowUseCase(progressId).firstOrNull()
            if (existing?.finished == true) {
                clearExamProgressUseCase(progressId)
            }
            getQuestionsUseCase(quizId).collect { list ->
                android.util.Log.d("ExamDebug", "loadQuestions: received ${list.size} questions for $quizId")
                val shuffled = list.shuffled().let { qs ->
                    if (count > 0) qs.take(count.coerceAtMost(qs.size)) else qs
                }
                val finalList = shuffled.map { q ->
                    val correctIndex = answerLetterToIndex(q.answer)
                    if (correctIndex == null) {
                        q
                    } else {
                        val pairs = q.options.mapIndexed { idx, opt -> idx to opt }.shuffled()
                        val newOptions = pairs.map { it.second }
                        val newCorrect = pairs.indexOfFirst { it.first == correctIndex }
                        val newAnswer = ('A' + newCorrect).toString()
                        q.copy(options = newOptions, answer = newAnswer)
                    }
                }
                _questions.value = finalList
                _selectedOptions.value = List(finalList.size) { -1 }
                _showResultList.value = List(finalList.size) { false }
                _currentIndex.value = 0
                _finished.value = false
                loadProgress()
            }
        }
    }

    private fun loadProgress() {
        viewModelScope.launch {
            getExamProgressFlowUseCase(progressId).collect { progress ->
                android.util.Log.d("ExamDebug", "loadProgress: progress=$progress id=$progressId")
                if (progress != null && !_progressLoaded.value) {
                    _currentIndex.value =
                        progress.currentIndex.coerceAtMost(_questions.value.size - 1)
                    _selectedOptions.value =
                        if (progress.selectedOptions.size >= _questions.value.size) {
                            progress.selectedOptions.take(_questions.value.size)
                        } else {
                            progress.selectedOptions + List(_questions.value.size - progress.selectedOptions.size) { -1 }
                        }
                    _showResultList.value =
                        if (progress.showResultList.size >= _questions.value.size) {
                            progress.showResultList
                        } else {
                            progress.showResultList + List(_questions.value.size - progress.showResultList.size) { false }
                        }
                    _finished.value = progress.finished
                } else if (progress == null && !_progressLoaded.value) {
                    // 初始化进度
                    saveProgress()
                }
                _progressLoaded.value = true
            }
        }
    }

    fun selectOption(option: Int) {
        val idx = _currentIndex.value
        android.util.Log.d("ExamDebug", "selectOption index=$idx option=$option")
        val list = _selectedOptions.value.toMutableList()
        if (idx in list.indices) {
            list[idx] = option
            _selectedOptions.value = list
            saveProgress()
        }
    }

    fun nextQuestion() {
        if (_currentIndex.value < _questions.value.size - 1) {
            android.util.Log.d("ExamDebug", "nextQuestion from ${_currentIndex.value}")
            _currentIndex.value += 1
            saveProgress()
        }
    }

    fun prevQuestion() {
        if (_currentIndex.value > 0) {
            android.util.Log.d("ExamDebug", "prevQuestion from ${_currentIndex.value}")
            _currentIndex.value -= 1
            saveProgress()
        }
    }

    fun goToQuestion(index: Int) {
        if (index in _questions.value.indices) {
            android.util.Log.d("ExamDebug", "goToQuestion $index")
            _currentIndex.value = index
            saveProgress()
        }
    }

    suspend fun gradeExam(): Int {
        val qs = _questions.value
        val selections = _selectedOptions.value
        var score = 0
        val newShowResultList = _showResultList.value.toMutableList()

        for (i in qs.indices) {
            val correct = answerLetterToIndex(qs[i].answer)
            val sel = selections.getOrElse(i) { -1 }
            if (sel != -1) {// 已作答题
                if (sel == correct) {
                    score++
                } else {
                    addWrongQuestionUseCase(WrongQuestion(qs[i], sel))
                }
                newShowResultList[i] = true // 只对已答题批改
            }
            // 未答的题保持原showResultList[i]（一般是false）
        }
        addHistoryRecordUseCase(HistoryRecord(score, qs.size))
        _showResultList.value = newShowResultList
        _finished.value = newShowResultList.all { it }
        android.util.Log.d("ExamDebug", "gradeExam score=$score total=${qs.size}")
        saveProgress()
        return score
    }

    fun clearProgress() {
        viewModelScope.launch {
            android.util.Log.d("ExamDebug", "clearProgress called for $progressId")
            clearExamProgressUseCase(progressId)
            _currentIndex.value = 0
            _selectedOptions.value = emptyList()
            _showResultList.value = emptyList()
            _finished.value = false
            _progressLoaded.value = false
        }
    }

    private fun saveProgress() {
        viewModelScope.launch {
            android.util.Log.d(
                "ExamDebug",
                "saveProgress: index=${_currentIndex.value} selected=${_selectedOptions.value} showResult=${_showResultList.value} finished=${_finished.value}"
            )
            saveExamProgressUseCase(
                com.example.testapp.domain.model.ExamProgress(
                    id = progressId,
                    currentIndex = _currentIndex.value,
                    selectedOptions = _selectedOptions.value,
                    showResultList = _showResultList.value,
                    finished = _finished.value,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }
}