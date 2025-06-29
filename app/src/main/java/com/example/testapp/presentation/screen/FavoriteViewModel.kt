package com.example.testapp.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.domain.usecase.AddFavoriteQuestionUseCase
import com.example.testapp.domain.usecase.RemoveFavoriteQuestionUseCase
import com.example.testapp.domain.usecase.GetFavoriteQuestionsUseCase
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.usecase.GetQuestionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val getFavoriteQuestionsUseCase: GetFavoriteQuestionsUseCase,
    private val addFavoriteQuestionUseCase: AddFavoriteQuestionUseCase,
    private val removeFavoriteQuestionUseCase: RemoveFavoriteQuestionUseCase,
    private val getQuestionsUseCase: GetQuestionsUseCase
) : ViewModel() {
    private val _favoriteQuestions = MutableStateFlow<List<Question>>(emptyList())
    val favoriteQuestions: StateFlow<List<Question>> = _favoriteQuestions.asStateFlow()

    init {
        viewModelScope.launch {
            getFavoriteQuestionsUseCase().combine(getQuestionsUseCase()) { favIds, allQuestions ->
                allQuestions.filter { favIds.contains(it.id) }
            }.collect {
                _favoriteQuestions.value = it
            }
        }
    }
    fun addFavorite(id: Int) {
        viewModelScope.launch { addFavoriteQuestionUseCase(id) }
    }
    fun removeFavorite(id: Int) {
        viewModelScope.launch { removeFavoriteQuestionUseCase(id) }
    }
}
