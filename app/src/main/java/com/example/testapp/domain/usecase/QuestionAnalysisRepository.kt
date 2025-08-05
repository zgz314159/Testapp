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

class GetSparkAnalysisUseCase @Inject constructor(
    private val repository: QuestionAnalysisRepository
) {
    suspend operator fun invoke(questionId: Int): String? = repository.getSparkAnalysis(questionId)
}

class SaveSparkAnalysisUseCase @Inject constructor(
    private val repository: QuestionAnalysisRepository
) {
    suspend operator fun invoke(questionId: Int, analysis: String) = repository.saveSparkAnalysis(questionId, analysis)
}

class GetBaiduAnalysisUseCase @Inject constructor(
    private val repository: QuestionAnalysisRepository
) {
    suspend operator fun invoke(questionId: Int): String? = repository.getBaiduAnalysis(questionId)
}

class SaveBaiduAnalysisUseCase @Inject constructor(
    private val repository: QuestionAnalysisRepository
) {
    suspend operator fun invoke(questionId: Int, analysis: String) = repository.saveBaiduAnalysis(questionId, analysis)
}