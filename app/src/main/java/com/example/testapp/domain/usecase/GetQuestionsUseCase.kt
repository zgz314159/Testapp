package com.example.testapp.domain.usecase

import com.example.testapp.domain.model.Question
import com.example.testapp.domain.repository.QuestionRepository
import kotlinx.coroutines.flow.Flow

class GetQuestionsUseCase(private val repository: QuestionRepository) {
    operator fun invoke(quizId: String? = null): Flow<List<Question>> =
        when {
            quizId == "favorite" -> repository.getFavoriteQuestions()
            quizId.isNullOrBlank() -> repository.getQuestions()
            else -> repository.getQuestionsByFileName(quizId)
        }

    suspend fun deleteQuestionsByFileName(fileName: String) {
        repository.deleteQuestionsByFileName(fileName)
    }
}
