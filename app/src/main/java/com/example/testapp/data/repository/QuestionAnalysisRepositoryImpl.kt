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
    private val sparkCache = mutableMapOf<Int, String>()
    private val baiduCache = mutableMapOf<Int, String>()
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
        val entity = dao.getEntity(questionId)
            ?.copy(analysis = analysis)
            ?: QuestionAnalysisEntity(questionId = questionId, analysis = analysis, sparkAnalysis = null, baiduAnalysis = null)
        dao.upsert(entity)
        val dbDuration = System.currentTimeMillis() - dbStart
        android.util.Log.d(
            "QuestionAnalysisRepo",
            "db save duration=${dbDuration} ms"
        )
        cache[questionId] = analysis
    }
    override suspend fun getSparkAnalysis(questionId: Int): String? {
        sparkCache[questionId]?.let { return it }
        val start = System.currentTimeMillis()
        val result = dao.getSparkAnalysis(questionId)
        val duration = System.currentTimeMillis() - start
        android.util.Log.d(
            "QuestionAnalysisRepo",
            "getSparkAnalysis duration=${duration} ms resultLength=${result?.length}"
        )
        if (result != null) sparkCache[questionId] = result
        return result
    }

    override suspend fun saveSparkAnalysis(questionId: Int, analysis: String) {
        val dbStart = System.currentTimeMillis()
        val entity = dao.getEntity(questionId)
            ?.copy(sparkAnalysis = analysis)
            ?: QuestionAnalysisEntity(questionId = questionId, analysis = "", sparkAnalysis = analysis, baiduAnalysis = null)
        dao.upsert(entity)
        val dbDuration = System.currentTimeMillis() - dbStart
        android.util.Log.d(
            "QuestionAnalysisRepo",
            "db save spark duration=${dbDuration} ms"
        )
        sparkCache[questionId] = analysis
    }

    override suspend fun getBaiduAnalysis(questionId: Int): String? {
        val start = System.currentTimeMillis()
        baiduCache[questionId]?.let {
            android.util.Log.d("QuestionAnalysisRepo", "baidu cache hit id=$questionId")
            return it
        }
        val result = dao.getBaiduAnalysis(questionId)
        val duration = System.currentTimeMillis() - start
        android.util.Log.d(
            "QuestionAnalysisRepo",
            "getBaiduAnalysis duration=${duration} ms resultLength=${result?.length}"
        )
        if (result != null) baiduCache[questionId] = result
        return result
    }

    override suspend fun saveBaiduAnalysis(questionId: Int, analysis: String) {
        val dbStart = System.currentTimeMillis()
        val entity = dao.getEntity(questionId)
            ?.copy(baiduAnalysis = analysis)
            ?: QuestionAnalysisEntity(questionId = questionId, analysis = "", sparkAnalysis = "", baiduAnalysis = analysis)
        dao.upsert(entity)
        val dbDuration = System.currentTimeMillis() - dbStart
        android.util.Log.d(
            "QuestionAnalysisRepo",
            "db save baidu duration=${dbDuration} ms"
        )
        baiduCache[questionId] = analysis
    }
}