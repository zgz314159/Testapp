package com.example.testapp.domain.repository

import com.example.testapp.domain.model.QuestionCorrectionRequest
import com.example.testapp.domain.model.QuestionCorrectionSuggestion

interface QuestionCorrectionRepository {
    suspend fun correct(request: QuestionCorrectionRequest): QuestionCorrectionSuggestion
}
