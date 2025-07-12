package com.example.testapp.data.repository

import com.example.testapp.data.local.dao.QuestionAnalysisDao
import com.example.testapp.data.local.entity.QuestionAnalysisEntity
import com.example.testapp.domain.repository.QuestionAnalysisRepository
import javax.inject.Inject

class QuestionAnalysisRepositoryImpl @Inject constructor(
    private val dao: QuestionAnalysisDao
) : QuestionAnalysisRepository {
    /** In-memory cache to avoid hitting the database repeatedly. */
    private val cache = mutableMapOf<Int, String>()

    override suspend fun getAnalysis(questionId: Int): String? {
        val start = System.currentTimeMillis()
        cache[questionId]?.let {
            android.util.Log.d("QuestionAnalysisRepo", "cache hit id=$questionId")
            return it
        }
        val cachedCheckDuration = System.currentTimeMillis() - start
        android.util.Log.d(
            "QuestionAnalysisRepo",
            "cache miss id=$questionId, check duration=${cachedCheckDuration} ms"
        )

        val dbStart = System.currentTimeMillis()
        val result = dao.getAnalysis(questionId)
        val dbDuration = System.currentTimeMillis() - dbStart
        android.util.Log.d(
            "QuestionAnalysisRepo",
            "db get duration=${dbDuration} ms resultLength=${result?.length}"
        )

        if (result != null) cache[questionId] = result
        return result
    }

    override suspend fun saveAnalysis(questionId: Int, analysis: String) {
        val dbStart = System.currentTimeMillis()
        dao.upsert(QuestionAnalysisEntity(questionId, analysis))
        val dbDuration = System.currentTimeMillis() - dbStart
        android.util.Log.d(
            "QuestionAnalysisRepo",
            "db save duration=${dbDuration} ms"
        )
        cache[questionId] = analysis
    }
}