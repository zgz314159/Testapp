package com.example.testapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.core.util.FavoriteSessionPipeline
import com.example.testapp.domain.model.Question
import com.example.testapp.presentation.screen.favorite.FavoriteViewModel

@Composable
fun rememberExamFavoriteBindings(
    favoriteViewModel: FavoriteViewModel = hiltViewModel(),
): Pair<(Int) -> Boolean, (Question) -> Unit> {
    LaunchedEffect(Unit) { favoriteViewModel.ensureFullListLoaded() }
    val favoriteQuestions by favoriteViewModel.favoriteQuestions.collectAsState()
    val isQuestionFavorite: (Int) -> Boolean = remember(favoriteQuestions) {
        { questionId -> FavoriteSessionPipeline.isFavorite(questionId, favoriteQuestions) }
    }
    val onToggleQuestionFavorite: (Question) -> Unit = remember(favoriteViewModel, favoriteQuestions) {
        { question ->
            val isFavorite = FavoriteSessionPipeline.isFavorite(question.id, favoriteQuestions)
            if (isFavorite) {
                favoriteViewModel.removeFavorite(question.id)
            } else {
                favoriteViewModel.addFavorite(question)
            }
        }
    }
    return isQuestionFavorite to onToggleQuestionFavorite
}
