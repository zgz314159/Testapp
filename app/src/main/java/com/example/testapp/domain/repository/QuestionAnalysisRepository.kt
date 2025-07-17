package com.example.testapp.domain.repository

interface QuestionAnalysisRepository {
    suspend fun getAnalysis(questionId: Int): String?
    suspend fun saveAnalysis(questionId: Int, analysis: String)
    suspend fun getSparkAnalysis(questionId: Int): String?
    suspend fun saveSparkAnalysis(questionId: Int, analysis: String)
}