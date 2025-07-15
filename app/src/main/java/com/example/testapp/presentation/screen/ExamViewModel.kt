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
import com.example.testapp.util.answerLettersToIndices
import com.example.testapp.domain.usecase.GetQuestionNoteUseCase
import com.example.testapp.domain.usecase.SaveQuestionNoteUseCase
import com.example.testapp.domain.usecase.GetQuestionAnalysisUseCase

@HiltViewModel
class ExamViewModel @Inject constructor(
    private val getQuestionsUseCase: GetQuestionsUseCase,
    private val addWrongQuestionUseCase: AddWrongQuestionUseCase,
    private val addHistoryRecordUseCase: AddHistoryRecordUseCase,
    private val saveExamProgressUseCase: SaveExamProgressUseCase,
    private val getExamProgressFlowUseCase: GetExamProgressFlowUseCase,
    private val clearExamProgressUseCase: ClearExamProgressUseCase,
    private val getQuestionNoteUseCase: GetQuestionNoteUseCase,
    private val saveQuestionNoteUseCase: SaveQuestionNoteUseCase,
    private val getQuestionAnalysisUseCase: GetQuestionAnalysisUseCase
) : ViewModel() {
    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _selectedOptions = MutableStateFlow<List<List<Int>>>(emptyList())
    val selectedOptions: StateFlow<List<List<Int>>> = _selectedOptions.asStateFlow()

    private val _showResultList = MutableStateFlow<List<Boolean>>(emptyList())
    val showResultList: StateFlow<List<Boolean>> = _showResultList.asStateFlow()

    private val _analysisList = MutableStateFlow<List<String>>(emptyList())
    val analysisList: StateFlow<List<String>> = _analysisList.asStateFlow()

    private val _noteList = MutableStateFlow<List<String>>(emptyList())
    val noteList: StateFlow<List<String>> = _noteList.asStateFlow()

    private val _progressLoaded = MutableStateFlow(false)
    val progressLoaded: StateFlow<Boolean> = _progressLoaded.asStateFlow()

    private val _finished = MutableStateFlow(false)
    val finished: StateFlow<Boolean> = _finished.asStateFlow()

    private var progressId: String = "exam_default"
    private var progressSeed: Long = System.currentTimeMillis()

    private var quizIdInternal: String = ""

    private var notesLoaded: Boolean = false
    private var analysisLoaded: Boolean = false

    fun loadQuestions(quizId: String, count: Int, random: Boolean) {
        // 考试模式也使用前缀区分进度
        progressId = "exam_${quizId}"
        quizIdInternal = quizId
        _progressLoaded.value = false
        viewModelScope.launch {
            android.util.Log.d(
                "ExamDebug",
                "loadQuestions start id=$quizId count=$count random=$random"
            )
            val existing = getExamProgressFlowUseCase(progressId).firstOrNull()
            progressSeed = existing?.timestamp ?: System.currentTimeMillis()
           /* if (existing?.finished == true) {
                clearExamProgressUseCase(progressId)
            }*/
            getQuestionsUseCase(quizId).collect { list ->
                android.util.Log.d(
                    "ExamDebug",
                    "loadQuestions: received ${list.size} questions for $quizId seed=$progressSeed"
                )
                val ordered = if (random) list.shuffled(java.util.Random(progressSeed)) else list
                val trimmed = if (count > 0) ordered.take(count.coerceAtMost(ordered.size)) else ordered
                val finalList = if (random) {
                    trimmed.mapIndexed { idx, q ->
                        val correctIndex = answerLetterToIndex(q.answer)
                        if (correctIndex == null) {
                            q
                        } else {
                            val rand = java.util.Random(progressSeed + idx)
                            val pairs = q.options.mapIndexed { i, opt -> i to opt }.shuffled(rand)
                            val newOptions = pairs.map { it.second }
                            val newCorrect = pairs.indexOfFirst { it.first == correctIndex }
                            val newAnswer = ('A' + newCorrect).toString()
                            q.copy(options = newOptions, answer = newAnswer)
                        }
                    }
                } else {
                    trimmed
                }
                _questions.value = finalList
              /*  _selectedOptions.value = List(finalList.size) { emptyList() }
                _showResultList.value = List(finalList.size) { false }
                _analysisList.value = List(finalList.size) { "" }
                _noteList.value = List(finalList.size) { "" }
                _currentIndex.value = 0
                _finished.value = false*/
                loadProgress()
            }
        }
    }

    private fun loadProgress() {
        viewModelScope.launch {
            getExamProgressFlowUseCase(progressId).collect { progress ->
                android.util.Log.d("ExamDebug", "loadProgress: progress=$progress id=$progressId")
                if (progress != null && !_progressLoaded.value) {
                    val size = _questions.value.size
                    var changed = false
                    progressSeed = progress.timestamp
                    _currentIndex.value = progress.currentIndex.coerceAtMost(size - 1)

                    val selected = if (progress.selectedOptions.size >= size) {
                        progress.selectedOptions.take(size)
                    } else {
                        changed = true
                        progress.selectedOptions + List(size - progress.selectedOptions.size) { emptyList() }
                    }
                    _selectedOptions.value = selected

                    val showRes = if (progress.showResultList.size >= size) {
                        progress.showResultList.take(size)
                    } else {
                        changed = true
                        progress.showResultList + List(size - progress.showResultList.size) { false }
                    }
                    _showResultList.value = showRes

                    val analysis = if (progress.analysisList.size >= size) {
                        progress.analysisList.take(size)
                    } else {
                        changed = true
                        progress.analysisList + List(size - progress.analysisList.size) { "" }
                    }
                    _analysisList.value = analysis

                    val notes = if (progress.noteList.size >= size) {
                        progress.noteList.take(size)
                    } else {
                        changed = true
                        progress.noteList + List(size - progress.noteList.size) { "" }
                    }
                    _noteList.value = notes

                    _finished.value = progress.finished
                    if (changed) saveProgressInternal()
                } else if (progress == null && !_progressLoaded.value) {
                    // 初始化进度
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

    fun selectOption(option: Int) {
        val idx = _currentIndex.value
        android.util.Log.d("ExamDebug", "selectOption index=$idx option=$option")
        val list = _selectedOptions.value.toMutableList()
        while (list.size <= idx) list.add(emptyList())
        val current = list[idx].toMutableList()
        val isMulti = _questions.value.getOrNull(idx)?.type == "多选题"
        if (isMulti) {
            if (current.contains(option)) current.remove(option) else current.add(option)
        } else {
            current.clear(); current.add(option)
        }
        list[idx] = current
        _selectedOptions.value = list
        saveProgress()
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
        }
    }

    fun saveNote(questionId: Int, index: Int, text: String) {
        viewModelScope.launch { saveQuestionNoteUseCase(questionId, text) }
        val list = _noteList.value.toMutableList()
        while (list.size <= index) list.add("")
        list[index] = text
        _noteList.value = list
    }

    suspend fun getNote(questionId: Int): String? = getQuestionNoteUseCase(questionId)
    fun goToQuestion(index: Int) {
        if (index in _questions.value.indices) {
            android.util.Log.d("ExamDebug", "goToQuestion $index")
            _currentIndex.value = index
            saveProgress()
        }
    }

    fun updateShowResult(index: Int, value: Boolean) {
        android.util.Log.d("ExamDebug", "updateShowResult index=$index value=$value")
        val list = _showResultList.value.toMutableList()
        while (list.size <= index) list.add(false)
        list[index] = value
        _showResultList.value = list
        saveProgress()
    }

    fun updateAnalysis(index: Int, text: String) {
        android.util.Log.d("ExamDebug", "updateAnalysis index=$index text=$text")
        val list = _analysisList.value.toMutableList()
        while (list.size <= index) list.add("")
        list[index] = text
        _analysisList.value = list
        saveProgress()
    }



    suspend fun gradeExam(): Int {
        val qs = _questions.value
        val selections = _selectedOptions.value

        // 判空，任何数据为0直接返回
        if (qs.isEmpty() || selections.isEmpty() || _showResultList.value.isEmpty()) {
            android.util.Log.w("ExamViewModel", "gradeExam called with empty data, skipping grading")
            return 0
        }

        var score = 0
        val newShowResultList = _showResultList.value.toMutableList()

        for (i in qs.indices) {
            val correct = answerLettersToIndices(qs[i].answer)
            val sel = selections.getOrElse(i) { emptyList() }

            // 遍历每一道题
            if (sel.isNotEmpty()) {
                // 仅对已选题目判分并标记
                if (sel.sorted() == correct.sorted()) {
                    score++
                } else {
                    addWrongQuestionUseCase(WrongQuestion(qs[i], sel))
                }
                newShowResultList[i] = true    // 标记已批改
            } else {
                newShowResultList[i] = false
            }

        }
        addHistoryRecordUseCase(HistoryRecord(score, qs.size, "exam_${quizIdInternal}"))
        _showResultList.value = newShowResultList
        _finished.value = newShowResultList.all { it }
        android.util.Log.d("ExamDebug", "gradeExam score=$score total=${qs.size}")
        saveProgressInternal()
        return score
    }

    fun clearProgressAndReload() {
        viewModelScope.launch {
            clearExamProgressUseCase(progressId)
            // 重置状态
            _currentIndex.value = 0
            _selectedOptions.value = List(_questions.value.size) { emptyList() }
            _showResultList.value = List(_questions.value.size) { false }
            _analysisList.value = List(_questions.value.size) { "" }
            _noteList.value = List(_questions.value.size) { "" }
            _finished.value = false
            _progressLoaded.value = false
            progressSeed = System.currentTimeMillis()
            analysisLoaded = false
            notesLoaded = false
            loadProgress()
        }
    }


    fun resetAllStates() {
        val qs = _questions.value
        _currentIndex.value = 0
        _selectedOptions.value = List(qs.size) { emptyList() }
        _showResultList.value = List(qs.size) { false }
        _analysisList.value = List(qs.size) { "" }
        _noteList.value = List(qs.size) { "" }
        _finished.value = false
        _progressLoaded.value = true
    }


    fun clearProgress() {
        viewModelScope.launch {
            android.util.Log.d("ExamDebug", "clearProgress called for $progressId")
            clearExamProgressUseCase(progressId)
            _currentIndex.value = 0
            _selectedOptions.value = emptyList()
            _showResultList.value = emptyList()
            _analysisList.value = emptyList()
            _noteList.value = emptyList()
            _finished.value = false
            _progressLoaded.value = false
            progressSeed = System.currentTimeMillis()
            analysisLoaded = false
            notesLoaded = false
        }
    }

    private suspend fun saveProgressInternal() {
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
                analysisList = _analysisList.value,
                noteList = _noteList.value,
                finished = _finished.value,
                timestamp = progressSeed
            )
        )
    }

    private fun saveProgress() {
        viewModelScope.launch { saveProgressInternal() }
    }
}