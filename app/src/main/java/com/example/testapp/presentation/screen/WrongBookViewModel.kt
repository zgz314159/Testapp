package com.example.testapp.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.domain.model.WrongQuestion
import com.example.testapp.domain.repository.WrongBookRepository
import com.example.testapp.domain.usecase.GetWrongBookUseCase
import com.example.testapp.domain.usecase.RemoveWrongQuestionsByFileNameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WrongBookViewModel @Inject constructor(
    getWrongBookUseCase: GetWrongBookUseCase,
    private val wrongBookRepository: WrongBookRepository,
    private val removeWrongQuestionsByFileNameUseCase: RemoveWrongQuestionsByFileNameUseCase
) : ViewModel() {
    private val _wrongQuestions = MutableStateFlow<List<WrongQuestion>>(emptyList())
    val wrongQuestions: StateFlow<List<WrongQuestion>> = _wrongQuestions.asStateFlow()
    private val _fileNames = MutableStateFlow<List<String>>(emptyList())
    val fileNames: StateFlow<List<String>> = _fileNames.asStateFlow()

    init {
        viewModelScope.launch {
            getWrongBookUseCase().collect {
                
                _wrongQuestions.value = it
                _fileNames.value = it.mapNotNull { w -> w.question.fileName }.distinct()
            }
        }
    }

    fun addWrongQuestion(wrong: WrongQuestion) {
        
        viewModelScope.launch {
            
            wrongBookRepository.add(wrong)
            
        }
    }
    // 新增：按文件名删除错题
    fun removeByFileName(fileName: String) {
        viewModelScope.launch { removeWrongQuestionsByFileNameUseCase(fileName) }
    }
}
