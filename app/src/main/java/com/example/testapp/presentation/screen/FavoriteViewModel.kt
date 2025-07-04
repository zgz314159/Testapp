package com.example.testapp.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.domain.usecase.AddFavoriteQuestionUseCase
import com.example.testapp.domain.usecase.RemoveFavoriteQuestionUseCase
import com.example.testapp.domain.usecase.GetFavoriteQuestionsUseCase
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.FavoriteQuestion
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
    private val _favoriteQuestions = MutableStateFlow<List<FavoriteQuestion>>(emptyList())
    val favoriteQuestions: StateFlow<List<FavoriteQuestion>> = _favoriteQuestions.asStateFlow()
    private val _fileNames = MutableStateFlow<List<String>>(emptyList())
    val fileNames: StateFlow<List<String>> = _fileNames.asStateFlow()

    init {
        viewModelScope.launch {
            getFavoriteQuestionsUseCase().collect {
                _favoriteQuestions.value = it
                _fileNames.value = it.mapNotNull { fav -> fav.question.fileName }.distinct()
            }
        }
    }
    fun addFavorite(question: com.example.testapp.domain.model.Question) {
        viewModelScope.launch { addFavoriteQuestionUseCase(FavoriteQuestion(question)) }
    }
    fun removeFavorite(id: Int) {
        viewModelScope.launch { removeFavoriteQuestionUseCase(id) }
    }
}
