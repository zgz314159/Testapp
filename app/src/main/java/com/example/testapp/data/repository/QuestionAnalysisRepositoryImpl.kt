package com.example.testapp.data.repository

import com.example.testapp.data.local.dao.QuestionAnalysisDao
import com.example.testapp.data.local.entity.QuestionAnalysisEntity
import com.example.testapp.domain.repository.QuestionAnalysisRepository
import javax.inject.Inject

class QuestionAnalysisRepositoryImpl @Inject constructor(
    private val dao: QuestionAnalysisDao
) : QuestionAnalysisRepository {
    override suspend fun getAnalysis(questionId: Int): String? = dao.getAnalysis(questionId)

    override suspend fun saveAnalysis(questionId: Int, analysis: String) {
        dao.upsert(QuestionAnalysisEntity(questionId, analysis))
    }
}