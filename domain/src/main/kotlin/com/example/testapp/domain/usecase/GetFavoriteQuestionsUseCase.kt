package com.example.testapp.domain.usecase

import com.example.testapp.domain.model.FavoriteQuestion
import com.example.testapp.domain.repository.FavoriteQuestionRepository
import javax.inject.Inject

class GetFavoriteQuestionsUseCase @Inject constructor(
    private val repo: FavoriteQuestionRepository
) {
    operator fun invoke() = repo.getAll()
}
