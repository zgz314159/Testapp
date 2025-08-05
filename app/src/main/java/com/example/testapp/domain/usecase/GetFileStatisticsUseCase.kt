package com.example.testapp.domain.usecase

import com.example.testapp.domain.repository.QuestionRepository
import com.example.testapp.domain.repository.WrongBookRepository
import com.example.testapp.domain.repository.FavoriteQuestionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class FileStatistics(
    val questionCount: Int = 0,
    val wrongCount: Int = 0,
    val favoriteCount: Int = 0
)

class GetFileStatisticsUseCase @Inject constructor(
    private val questionRepository: QuestionRepository,
    private val wrongBookRepository: WrongBookRepository,
    private val favoriteRepository: FavoriteQuestionRepository
) {
    operator fun invoke(): Flow<Map<String, FileStatistics>> {
        return combine(
            questionRepository.getQuestions(),
            wrongBookRepository.getAll(),
            favoriteRepository.getAll()
        ) { questions, wrongQuestions, favoriteQuestions ->
            
            val questionCounts = questions
                .groupBy { it.fileName ?: "" }
                .mapValues { it.value.size }
            
            val wrongCounts = wrongQuestions
                .groupBy { it.question.fileName ?: "" }
                .mapValues { it.value.size }
            
            val favoriteCounts = favoriteQuestions
                .groupBy { it.question.fileName ?: "" }
                .mapValues { it.value.size }
            
            val fileStats = mutableMapOf<String, FileStatistics>()
            
            // 合并所有文件名
            val allFileNames = (questionCounts.keys + wrongCounts.keys + favoriteCounts.keys).toSet()
            
            allFileNames.forEach { fileName ->
                if (fileName.isNotEmpty()) {
                    fileStats[fileName] = FileStatistics(
                        questionCount = questionCounts[fileName] ?: 0,
                        wrongCount = wrongCounts[fileName] ?: 0,
                        favoriteCount = favoriteCounts[fileName] ?: 0
                    )
                }
            }
            
            fileStats
        }
    }
}
