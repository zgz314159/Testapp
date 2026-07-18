package com.example.testapp.domain.usecase

import com.example.testapp.domain.model.QuestionCorrectionRequest
import com.example.testapp.domain.model.QuestionCorrectionSuggestion
import com.example.testapp.domain.repository.QuestionCorrectionRepository
import javax.inject.Inject

class CorrectQuestionWithAiUseCase @Inject constructor(
    private val repository: QuestionCorrectionRepository,
) {
    suspend operator fun invoke(request: QuestionCorrectionRequest): Result<QuestionCorrectionSuggestion> =
        runCatching { repository.correct(request) }
}
