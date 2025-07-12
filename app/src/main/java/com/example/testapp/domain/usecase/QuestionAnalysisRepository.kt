package com.example.testapp.domain.usecase

import com.example.testapp.domain.repository.QuestionAnalysisRepository
import javax.inject.Inject

class GetQuestionAnalysisUseCase @Inject constructor(
    private val repository: QuestionAnalysisRepository
) {
    suspend operator fun invoke(questionId: Int): String? = repository.getAnalysis(questionId)
}

class SaveQuestionAnalysisUseCase @Inject constructor(
    private val repository: QuestionAnalysisRepository
) {
    suspend operator fun invoke(questionId: Int, analysis: String) = repository.saveAnalysis(questionId, analysis)
}