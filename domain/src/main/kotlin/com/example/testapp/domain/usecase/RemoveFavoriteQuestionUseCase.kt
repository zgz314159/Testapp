package com.example.testapp.domain.usecase

import com.example.testapp.domain.repository.FavoriteQuestionRepository
import javax.inject.Inject

class RemoveFavoriteQuestionUseCase @Inject constructor(
    private val repo: FavoriteQuestionRepository
) {
    suspend operator fun invoke(questionId: Int) = repo.remove(questionId)
}
