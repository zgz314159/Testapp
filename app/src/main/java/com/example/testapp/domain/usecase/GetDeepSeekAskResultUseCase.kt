package com.example.testapp.domain.usecase

import com.example.testapp.domain.repository.QuestionAskRepository
import javax.inject.Inject

class GetDeepSeekAskResultUseCase @Inject constructor(
    private val repository: QuestionAskRepository
) {
    suspend operator fun invoke(questionId: Int): String? = repository.getDeepSeekResult(questionId)
}

class SaveDeepSeekAskResultUseCase @Inject constructor(
    private val repository: QuestionAskRepository
) {
    suspend operator fun invoke(questionId: Int, result: String) = repository.saveDeepSeekResult(questionId, result)
}

class GetSparkAskResultUseCase @Inject constructor(
    private val repository: QuestionAskRepository
) {
    suspend operator fun invoke(questionId: Int): String? = repository.getSparkResult(questionId)
}

class SaveSparkAskResultUseCase @Inject constructor(
    private val repository: QuestionAskRepository
) {
    suspend operator fun invoke(questionId: Int, result: String) = repository.saveSparkResult(questionId, result)
}

class GetBaiduAskResultUseCase @Inject constructor(
    private val repository: QuestionAskRepository
) {
    suspend operator fun invoke(questionId: Int): String? = repository.getBaiduResult(questionId)
}

class SaveBaiduAskResultUseCase @Inject constructor(
    private val repository: QuestionAskRepository
) {
    suspend operator fun invoke(questionId: Int, result: String) = repository.saveBaiduResult(questionId, result)
}