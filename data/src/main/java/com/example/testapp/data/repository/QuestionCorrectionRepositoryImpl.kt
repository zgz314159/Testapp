package com.example.testapp.data.repository

import com.example.testapp.data.network.ai.AiBackend
import com.example.testapp.domain.model.QuestionCorrectionRequest
import com.example.testapp.domain.model.QuestionCorrectionSuggestion
import com.example.testapp.domain.repository.QuestionCorrectionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestionCorrectionRepositoryImpl @Inject constructor(
    private val aiBackend: AiBackend,
) : QuestionCorrectionRepository {
    override suspend fun correct(request: QuestionCorrectionRequest): QuestionCorrectionSuggestion =
        aiBackend.correctQuestion(request)
}
