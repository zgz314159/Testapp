package com.example.testapp.domain.repository

interface QuestionAskRepository {
    suspend fun getDeepSeekResult(questionId: Int): String?
    suspend fun saveDeepSeekResult(questionId: Int, result: String)
    suspend fun getSparkResult(questionId: Int): String?
    suspend fun saveSparkResult(questionId: Int, result: String)
    suspend fun getBaiduResult(questionId: Int): String?
    suspend fun saveBaiduResult(questionId: Int, result: String)
    suspend fun deleteByQuestionId(questionId: Int)
}