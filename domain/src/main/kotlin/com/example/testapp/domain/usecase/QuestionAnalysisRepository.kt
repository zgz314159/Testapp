package com.example.testapp.domain.usecase

import com.example.testapp.domain.repository.QuestionAnalysisRepository
import javax.inject.Inject

class GetQuestionAnalysisUseCase @Inject constructor(
    private val repository: QuestionAnalysisRepository
) {
    suspend operator fun invoke(questionId: Int): Result<String?> = runCatching {
        repository.getAnalysis(questionId)
    }
}

class SaveQuestionAnalysisUseCase @Inject constructor(
    private val repository: QuestionAnalysisRepository
) {
    suspend operator fun invoke(questionId: Int, analysis: String): Result<Unit> = runCatching {
        repository.saveAnalysis(questionId, analysis)
    }
}

class GetSparkAnalysisUseCase @Inject constructor(
    private val repository: QuestionAnalysisRepository
) {
    suspend operator fun invoke(questionId: Int): Result<String?> = runCatching {
        repository.getSparkAnalysis(questionId)
    }
}

class SaveSparkAnalysisUseCase @Inject constructor(
    private val repository: QuestionAnalysisRepository
) {
    suspend operator fun invoke(questionId: Int, analysis: String): Result<Unit> = runCatching {
        repository.saveSparkAnalysis(questionId, analysis)
    }
}

class GetBaiduAnalysisUseCase @Inject constructor(
    private val repository: QuestionAnalysisRepository
) {
    suspend operator fun invoke(questionId: Int): Result<String?> = runCatching {
        repository.getBaiduAnalysis(questionId)
    }
}

class SaveBaiduAnalysisUseCase @Inject constructor(
    private val repository: QuestionAnalysisRepository
) {
    suspend operator fun invoke(questionId: Int, analysis: String): Result<Unit> = runCatching {
        repository.saveBaiduAnalysis(questionId, analysis)
    }
}