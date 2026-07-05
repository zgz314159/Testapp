package com.example.testapp.data.repository

import com.example.testapp.data.local.dao.QuestionAnalysisDao
import com.example.testapp.data.local.entity.QuestionAnalysisEntity
import com.example.testapp.domain.repository.QuestionAnalysisRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class QuestionAnalysisRepositoryImpl @Inject constructor(
    private val dao: QuestionAnalysisDao
) : QuestionAnalysisRepository {
    /** In-memory cache to avoid hitting the database repeatedly. */
    private val cache = mutableMapOf<Int, String>()
    private val sparkCache = mutableMapOf<Int, String>()
    private val baiduCache = mutableMapOf<Int, String>()

    private fun mergeEntities(questionId: Int, entities: List<QuestionAnalysisEntity>): QuestionAnalysisEntity? {
        if (entities.isEmpty()) return null

        var deepSeek: String? = null
        var spark: String? = null
        var baidu: String? = null

        entities.forEach { entity ->
            if (!entity.analysis.isBlank()) deepSeek = entity.analysis
            if (!entity.sparkAnalysis.isNullOrBlank()) spark = entity.sparkAnalysis
            if (!entity.baiduAnalysis.isNullOrBlank()) baidu = entity.baiduAnalysis
        }

        return QuestionAnalysisEntity(
            questionId = questionId,
            analysis = deepSeek.orEmpty(),
            sparkAnalysis = spark,
            baiduAnalysis = baidu
        )
    }

    private suspend fun loadMergedEntity(questionId: Int): QuestionAnalysisEntity? {
        return mergeEntities(questionId, dao.getEntities(questionId))
    }

    private suspend fun saveMergedEntity(entity: QuestionAnalysisEntity) {
        dao.deleteByQuestionId(entity.questionId)
        dao.insert(entity.copy(id = 0))
    }

    override suspend fun getAnalysis(questionId: Int): String? {
        cache[questionId]?.let {
            return it
        }

        val result = loadMergedEntity(questionId)?.analysis?.takeIf { it.isNotBlank() }

        if (result != null) cache[questionId] = result
        return result
    }

    override suspend fun saveAnalysis(questionId: Int, analysis: String) {
        val entity = loadMergedEntity(questionId)
            ?.copy(analysis = analysis)
            ?: QuestionAnalysisEntity(questionId = questionId, analysis = analysis, sparkAnalysis = null, baiduAnalysis = null)
        saveMergedEntity(entity)

        cache[questionId] = analysis
    }
    override suspend fun getSparkAnalysis(questionId: Int): String? {
        sparkCache[questionId]?.let { return it }
        val result = loadMergedEntity(questionId)?.sparkAnalysis?.takeIf { it.isNotBlank() }

        if (result != null) sparkCache[questionId] = result
        return result
    }

    override suspend fun saveSparkAnalysis(questionId: Int, analysis: String) {
        val entity = loadMergedEntity(questionId)
            ?.copy(sparkAnalysis = analysis)
            ?: QuestionAnalysisEntity(questionId = questionId, analysis = "", sparkAnalysis = analysis, baiduAnalysis = null)
        saveMergedEntity(entity)

        sparkCache[questionId] = analysis
    }

    override suspend fun getBaiduAnalysis(questionId: Int): String? {
        baiduCache[questionId]?.let {
            return it
        }
        val result = loadMergedEntity(questionId)?.baiduAnalysis?.takeIf { it.isNotBlank() }

        if (result != null) baiduCache[questionId] = result
        return result
    }

    override suspend fun saveBaiduAnalysis(questionId: Int, analysis: String) {
        val entity = loadMergedEntity(questionId)
            ?.copy(baiduAnalysis = analysis)
            ?: QuestionAnalysisEntity(questionId = questionId, analysis = "", sparkAnalysis = "", baiduAnalysis = analysis)
        saveMergedEntity(entity)

        baiduCache[questionId] = analysis
    }

    override suspend fun deleteByQuestionId(questionId: Int) {
        dao.deleteByQuestionId(questionId)
        cache.remove(questionId)
        sparkCache.remove(questionId)
        baiduCache.remove(questionId)
    }

    override fun getByQuestionId(questionId: Int): Flow<com.example.testapp.domain.model.AIAnalysisData?> {
        return dao.getByQuestionId(questionId).map { entities ->
            val entity = mergeEntities(questionId, entities) ?: return@map null
            com.example.testapp.domain.model.AIAnalysisData(
                deepSeekAnalysis = entity.analysis.takeIf { it.isNotBlank() },
                sparkAnalysis = entity.sparkAnalysis?.takeIf { it.isNotBlank() },
                baiduAnalysis = entity.baiduAnalysis?.takeIf { it.isNotBlank() }
            )
        }
    }
}
