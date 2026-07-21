package com.example.testapp.domain.usecase

import com.example.testapp.domain.model.Question
import com.example.testapp.domain.repository.QuestionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

class GetQuestionsUseCase(private val repository: QuestionRepository) {
    operator fun invoke(quizId: String? = null): Flow<List<Question>> =
        when {
            quizId == "favorite" -> repository.getFavoriteQuestions()
            quizId.isNullOrBlank() -> repository.getQuestions()
            else -> repository.getQuestionsByFileName(quizId)
        }

    /**
     * Emits question-bank file names after ensuring the built-in assets/tiku seed
     * has been imported once (no-op when already initialized or when no assets).
     */
    fun fileNames(): Flow<List<String>> = flow {
        repository.ensureBuiltInQuestionsInitialized()
        emitAll(repository.getQuestionFileNames())
    }

    suspend fun deleteQuestionsByFileName(fileName: String) {
        
        try {
            repository.deleteQuestionsByFileName(fileName)
            
        } catch (e: Exception) {
            
            throw e
        }
    }
}
