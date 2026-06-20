package com.example.testapp.domain.usecase

import com.example.testapp.domain.repository.QuestionAnalysisRepository
import javax.inject.Inject

class RemoveQuestionAnalysisByQuestionIdUseCase @Inject constructor(
    private val repo: QuestionAnalysisRepository
) {
    suspend operator fun invoke(questionId: Int) = repo.deleteByQuestionId(questionId)
}
