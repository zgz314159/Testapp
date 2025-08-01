package com.example.testapp.data.repository

import com.example.testapp.data.local.dao.QuestionAskDao
import com.example.testapp.data.local.entity.QuestionAskEntity
import com.example.testapp.domain.repository.QuestionAskRepository
import javax.inject.Inject

class QuestionAskRepositoryImpl @Inject constructor(
    private val dao: QuestionAskDao
) : QuestionAskRepository {
    override suspend fun getDeepSeekResult(questionId: Int): String? {
        return dao.getDeepSeekResult(questionId)
    }

    override suspend fun saveDeepSeekResult(questionId: Int, result: String) {
        dao.upsert(QuestionAskEntity(questionId, result, getSparkResult(questionId) ?: "", getBaiduResult(questionId) ?: ""))
    }

    override suspend fun getSparkResult(questionId: Int): String? {
        return dao.getSparkResult(questionId)
    }

    override suspend fun saveSparkResult(questionId: Int, result: String) {
        dao.upsert(QuestionAskEntity(questionId, getDeepSeekResult(questionId) ?: "", result, getBaiduResult(questionId) ?: ""))
    }

    override suspend fun getBaiduResult(questionId: Int): String? {
        return dao.getBaiduResult(questionId)
    }

    override suspend fun saveBaiduResult(questionId: Int, result: String) {
        dao.upsert(QuestionAskEntity(questionId, getDeepSeekResult(questionId) ?: "", getSparkResult(questionId) ?: "", result))
    }
    
    override suspend fun deleteByQuestionId(questionId: Int) {
        dao.deleteByQuestionId(questionId)
    }
}