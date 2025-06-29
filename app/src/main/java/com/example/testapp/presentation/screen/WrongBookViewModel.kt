package com.example.testapp.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.domain.model.WrongQuestion
import com.example.testapp.domain.usecase.GetWrongBookUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WrongBookViewModel @Inject constructor(
    getWrongBookUseCase: GetWrongBookUseCase
) : ViewModel() {
    private val _wrongQuestions = MutableStateFlow<List<WrongQuestion>>(emptyList())
    val wrongQuestions: StateFlow<List<WrongQuestion>> = _wrongQuestions.asStateFlow()

    init {
        viewModelScope.launch {
            getWrongBookUseCase().collect {
                _wrongQuestions.value = it
            }
        }
    }
}
