package com.example.testapp.domain.repository

interface QuestionAnalysisRepository {
    suspend fun getAnalysis(questionId: Int): String?
    suspend fun saveAnalysis(questionId: Int, analysis: String)
}