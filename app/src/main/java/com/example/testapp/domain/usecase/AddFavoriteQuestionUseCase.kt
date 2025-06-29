package com.example.testapp.domain.usecase

import com.example.testapp.domain.repository.FavoriteQuestionRepository
import javax.inject.Inject

class AddFavoriteQuestionUseCase @Inject constructor(
    private val repo: FavoriteQuestionRepository
) {
    suspend operator fun invoke(questionId: Int) = repo.add(questionId)
}
