package com.example.testapp.domain.usecase

import com.example.testapp.domain.model.ExportData
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.WrongQuestion
import com.example.testapp.domain.model.FavoriteQuestion
import com.example.testapp.data.local.entity.QuestionAnalysisEntity
import com.example.testapp.data.local.entity.QuestionNoteEntity
import com.example.testapp.domain.repository.WrongBookRepository
import com.example.testapp.domain.repository.FavoriteQuestionRepository
import com.example.testapp.domain.repository.QuestionRepository
import com.example.testapp.domain.repository.QuestionAnalysisRepository
import com.example.testapp.domain.repository.QuestionNoteRepository
import javax.inject.Inject

/**
 * 导入题库用例
 */
class ImportQuestionsUseCase @Inject constructor(
    private val questionRepository: QuestionRepository,
    private val wrongBookRepository: WrongBookRepository,
    private val favoriteQuestionRepository: FavoriteQuestionRepository,
    private val questionAnalysisRepository: QuestionAnalysisRepository,
    private val questionNoteRepository: QuestionNoteRepository
) {
    suspend operator fun invoke(exportData: ExportData): Result<String> {
        return try {
            // 先批量导入所有题目
            val questionsToImport = exportData.questions.map { exportQuestion ->
                Question(
                    id = 0, // 让数据库自动生成新ID
                    content = exportQuestion.content,
                    type = exportQuestion.type,
                    options = exportQuestion.options,
                    answer = exportQuestion.answer,
                    explanation = exportQuestion.explanation,
                    fileName = exportQuestion.fileName
                )
            }
            
            // 使用批量导入方法
            questionRepository.importQuestions(questionsToImport)
            
            // 根据导出类型添加到对应的库
            exportData.questions.forEach { exportQuestion ->
                val question = Question(
                    id = 0, // 让数据库自动生成新ID
                    content = exportQuestion.content,
                    type = exportQuestion.type,
                    options = exportQuestion.options,
                    answer = exportQuestion.answer,
                    explanation = exportQuestion.explanation,
                    fileName = exportQuestion.fileName
                )
                
                when (exportData.exportType) {
                    "wrong_book" -> {
                        wrongBookRepository.add(WrongQuestion(question = question, selected = emptyList()))
                    }
                    "favorite" -> {
                        favoriteQuestionRepository.add(FavoriteQuestion(question))
                    }
                }
            }
            
            Result.success("成功导入 ${questionsToImport.size} 道题目")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
