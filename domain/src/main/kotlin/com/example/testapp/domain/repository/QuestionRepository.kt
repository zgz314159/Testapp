package com.example.testapp.domain.repository

import com.example.testapp.domain.model.Question
import kotlinx.coroutines.flow.Flow

data class MarkdownCleanupPreview(
    val questionId: Int,
    val fileName: String?,
    val changedFields: List<String>,
    val beforeSnippet: String,
    val afterSnippet: String
)

interface QuestionRepository {
    fun getQuestions(): Flow<List<Question>>
    fun getQuestionFileNames(): Flow<List<String>>
    fun getFavoriteQuestions(): Flow<List<Question>>
    suspend fun importQuestions(list: List<Question>)
    suspend fun exportQuestions(): List<Question>
    suspend fun importFromFiles(files: List<java.io.File>): Int
    suspend fun importFromFilesWithOrigin(files: List<Pair<java.io.File, String>>): Int
    fun getQuestionsByFileName(fileName: String): Flow<List<Question>>
    suspend fun saveQuestionsToJson(fileName: String, questions: List<Question>)
    suspend fun previewMarkdownCleanup(limit: Int = 20): List<MarkdownCleanupPreview>
    suspend fun normalizeStoredMarkdown(): Int
    suspend fun deleteQuestionsByFileName(fileName: String)
    suspend fun deleteFileAndRelatedData(fileName: String)
    // Ensure built-in question bank (packaged in assets) is imported on first run.
    suspend fun ensureBuiltInQuestionsInitialized()
}
