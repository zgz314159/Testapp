package com.example.testapp.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.WrongQuestion
import com.example.testapp.domain.model.HistoryRecord
import com.example.testapp.domain.usecase.GetQuestionsUseCase
import com.example.testapp.domain.usecase.AddWrongQuestionUseCase
import com.example.testapp.domain.usecase.AddHistoryRecordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExamViewModel @Inject constructor(
    private val getQuestionsUseCase: GetQuestionsUseCase,
    private val addWrongQuestionUseCase: AddWrongQuestionUseCase,
    private val addHistoryRecordUseCase: AddHistoryRecordUseCase
) : ViewModel() {
    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _selectedOptions = MutableStateFlow<List<Int>>(emptyList())
    val selectedOptions: StateFlow<List<Int>> = _selectedOptions.asStateFlow()

    fun loadQuestions(quizId: String, count: Int) {
        viewModelScope.launch {
            getQuestionsUseCase(quizId).collect { list ->
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
                _currentIndex.value = 0
            }
        }
    }

    fun selectOption(option: Int) {
        val idx = _currentIndex.value
        val list = _selectedOptions.value.toMutableList()
        if (idx in list.indices) {
            list[idx] = option
            _selectedOptions.value = list
        }
    }

    fun nextQuestion() {
        if (_currentIndex.value < _questions.value.size - 1) {
            _currentIndex.value += 1
        }
    }

    fun prevQuestion() {
        if (_currentIndex.value > 0) {
            _currentIndex.value -= 1
        }
    }

    fun goToQuestion(index: Int) {
        if (index in _questions.value.indices) {
            _currentIndex.value = index
        }
    }

    suspend fun gradeExam(): Int {
        val qs = _questions.value
        val selections = _selectedOptions.value
        var score = 0
        for (i in qs.indices) {
            val correct = answerLetterToIndex(qs[i].answer)
            val sel = selections.getOrElse(i) { -1 }
            if (sel == correct) {
                score++
            } else if (sel != -1) {
                addWrongQuestionUseCase(WrongQuestion(qs[i], sel))
            }
        }
        addHistoryRecordUseCase(HistoryRecord(score, qs.size))
        return score
    }
}

private fun answerLetterToIndex(answer: String): Int? {
    return answer.trim().uppercase().firstOrNull()?.let { it - 'A' }
}