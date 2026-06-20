package com.example.testapp.domain.repository

import com.example.testapp.domain.model.AIAnalysisData
import kotlinx.coroutines.flow.Flow

interface QuestionAnalysisRepository {
    suspend fun getAnalysis(questionId: Int): String?
    suspend fun saveAnalysis(questionId: Int, analysis: String)
    suspend fun getSparkAnalysis(questionId: Int): String?
    suspend fun saveSparkAnalysis(questionId: Int, analysis: String)
    suspend fun getBaiduAnalysis(questionId: Int): String?
    suspend fun saveBaiduAnalysis(questionId: Int, analysis: String)
    suspend fun deleteByQuestionId(questionId: Int)
    // Domain module should not reference data layer entities; expose AI analysis data
    fun getByQuestionId(questionId: Int): Flow<AIAnalysisData?>
}
