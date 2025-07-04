package com.example.testapp.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.domain.model.WrongQuestion
import com.example.testapp.domain.repository.WrongBookRepository
import com.example.testapp.domain.usecase.GetWrongBookUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WrongBookViewModel @Inject constructor(
    getWrongBookUseCase: GetWrongBookUseCase,
    private val wrongBookRepository: WrongBookRepository
) : ViewModel() {
    private val _wrongQuestions = MutableStateFlow<List<WrongQuestion>>(emptyList())
    val wrongQuestions: StateFlow<List<WrongQuestion>> = _wrongQuestions.asStateFlow()

    init {
        viewModelScope.launch {
            getWrongBookUseCase().collect {
                android.util.Log.d("WrongBookViewModel", "收到错题本数据，数量: ${it.size}, 内容: $it")
                _wrongQuestions.value = it
            }
        }
    }

    fun addWrongQuestion(wrong: WrongQuestion) {
        android.util.Log.d("WrongBookViewModel", "addWrongQuestion调用: $wrong")
        viewModelScope.launch {
            android.util.Log.d("WrongBookViewModel", "addWrongQuestion-协程: $wrong")
            wrongBookRepository.add(wrong)
            android.util.Log.d("WrongBookViewModel", "addWrongQuestion-保存完成: $wrong")
        }
    }
}
