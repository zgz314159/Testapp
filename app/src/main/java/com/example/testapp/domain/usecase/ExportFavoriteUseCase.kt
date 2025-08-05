package com.example.testapp.domain.usecase

import com.example.testapp.domain.model.ExportData
import com.example.testapp.domain.model.QuestionExportData
import com.example.testapp.domain.model.AIAnalysisData
import com.example.testapp.domain.repository.FavoriteQuestionRepository
import com.example.testapp.domain.repository.QuestionAnalysisRepository
import com.example.testapp.domain.repository.QuestionNoteRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 导出收藏题库用例
 */
class ExportFavoriteUseCase @Inject constructor(
    private val favoriteQuestionRepository: FavoriteQuestionRepository,
    private val questionAnalysisRepository: QuestionAnalysisRepository,
    private val questionNoteRepository: QuestionNoteRepository
) {
    suspend operator fun invoke(fileName: String? = null): ExportData {
        val favoriteQuestions = favoriteQuestionRepository.getAll().first()
        
        val filteredQuestions = if (fileName != null) {
            favoriteQuestions.filter { it.question.fileName == fileName }
        } else {
            favoriteQuestions
        }
        
        val exportQuestions = filteredQuestions.map { favoriteQuestion ->
            val question = favoriteQuestion.question
            
            // 获取AI解析
            val analysis = questionAnalysisRepository.getByQuestionId(question.id).first()
            val aiAnalysis = if (analysis != null) {
                AIAnalysisData(
                    deepSeekAnalysis = analysis.analysis.takeIf { it.isNotBlank() },
                    sparkAnalysis = analysis.sparkAnalysis?.takeIf { it.isNotBlank() },
                    baiduAnalysis = analysis.baiduAnalysis?.takeIf { it.isNotBlank() }
                )
            } else null
            
            // 获取笔记
            val note = questionNoteRepository.getNote(question.id)
            
            QuestionExportData(
                id = question.id,
                content = question.content,
                type = question.type,
                options = question.options,
                answer = question.answer,
                explanation = question.explanation,
                fileName = question.fileName,
                analysis = aiAnalysis,
                note = note
            )
        }
        
        return ExportData(
            questions = exportQuestions,
            exportType = "favorite"
        )
    }
}
