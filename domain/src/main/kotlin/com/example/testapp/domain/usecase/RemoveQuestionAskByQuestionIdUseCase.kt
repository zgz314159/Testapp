package com.example.testapp.domain.usecase

import com.example.testapp.domain.repository.QuestionAskRepository
import javax.inject.Inject

class RemoveQuestionAskByQuestionIdUseCase @Inject constructor(
    private val repo: QuestionAskRepository
) {
    suspend operator fun invoke(questionId: Int) = repo.deleteByQuestionId(questionId)
}
