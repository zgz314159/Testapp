package com.example.testapp.presentation.screen.settings

import com.example.testapp.domain.model.AIAnalysisData
import com.example.testapp.domain.repository.QuestionAnalysisRepository
import com.example.testapp.domain.repository.QuestionAskRepository
import com.example.testapp.domain.repository.QuestionNoteRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

data class SupplementaryData(
    val analysis: AIAnalysisData?,
    val note: String
)

@Singleton
class SupplementaryDataBatchLoader @Inject constructor() {

    suspend fun loadAll(
        questionIds: List<Int>,
        analysisRepo: QuestionAnalysisRepository,
        askRepo: QuestionAskRepository,
        noteRepo: QuestionNoteRepository
    ): Map<Int, SupplementaryData> = coroutineScope {
        buildMap {
            questionIds.chunked(BATCH_SIZE).forEach { chunk ->
                chunk.map { id ->
                    async { id to loadOne(id, analysisRepo, askRepo, noteRepo) }
                }.awaitAll().forEach { (id, data) -> put(id, data) }
            }
        }
    }

    private suspend fun loadOne(
        questionId: Int,
        analysisRepo: QuestionAnalysisRepository,
        askRepo: QuestionAskRepository,
        noteRepo: QuestionNoteRepository
    ): SupplementaryData {
        val stored = analysisRepo.getByQuestionId(questionId).firstOrNull()
        val analysis = AIAnalysisData(
            deepSeekAnalysis = stored?.deepSeekAnalysis?.takeIf { it.isNotBlank() }
                ?: askRepo.getDeepSeekResult(questionId)?.takeIf { it.isNotBlank() },
            sparkAnalysis = stored?.sparkAnalysis?.takeIf { it.isNotBlank() }
                ?: askRepo.getSparkResult(questionId)?.takeIf { it.isNotBlank() },
            baiduAnalysis = stored?.baiduAnalysis?.takeIf { it.isNotBlank() }
                ?: askRepo.getBaiduResult(questionId)?.takeIf { it.isNotBlank() }
        ).takeIf { !it.deepSeekAnalysis.isNullOrBlank() || !it.sparkAnalysis.isNullOrBlank() || !it.baiduAnalysis.isNullOrBlank() }
        val note = noteRepo.getNote(questionId).orEmpty()
        return SupplementaryData(analysis = analysis, note = note)
    }

    private companion object {
        private const val BATCH_SIZE = 30
    }
}
