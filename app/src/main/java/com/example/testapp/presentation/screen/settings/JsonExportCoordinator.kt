package com.example.testapp.presentation.screen.settings

import android.content.Context
import android.net.Uri
import com.example.testapp.domain.IOConstants
import com.example.testapp.domain.LocalizedException
import com.example.testapp.domain.repository.FavoriteQuestionRepository
import com.example.testapp.domain.repository.QuestionRepository
import com.example.testapp.domain.repository.WrongBookRepository
import com.example.testapp.core.common.LocalizedResult
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JsonExportCoordinator @Inject constructor() {

    suspend fun exportQuestionsToFile(
        context: Context,
        uri: Uri,
        questionRepository: QuestionRepository,
        onResult: (Boolean) -> Unit,
        onMessage: (LocalizedResult) -> Unit
    ) {
        try {
            val questions = questionRepository.exportQuestions()
            context.contentResolver.openOutputStream(uri)?.use { output ->
                val json = Json.encodeToString(questions)
                output.write(json.toByteArray())
            } ?: throw LocalizedException(IOConstants.CANNOT_WRITE_FILE)
            onResult(true)
            onMessage(LocalizedResult(IOConstants.EXPORT_PREFIX, listOf()))
        } catch (e: Exception) {
            onResult(false)
            onMessage(LocalizedResult(IOConstants.EXPORT_FAILED_PREFIX, listOf(e.message ?: "")))
        }
    }

    suspend fun exportWrongBookToUri(
        context: Context,
        uri: Uri,
        wrongBookRepository: WrongBookRepository,
        onResult: (Boolean) -> Unit,
        onMessage: (LocalizedResult) -> Unit
    ) {
        try {
            val wrongs = wrongBookRepository.getAll().firstOrNull() ?: emptyList()
            val out = context.contentResolver.openOutputStream(uri)
                ?: throw LocalizedException(IOConstants.CANNOT_WRITE_FILE)
            out.use { output ->
                val json = Json.encodeToString(wrongs)
                output.write(json.toByteArray())
            }
            onResult(true)
            onMessage(LocalizedResult(IOConstants.EXPORT_PREFIX))
        } catch (e: Exception) {
            onResult(false)
            onMessage(LocalizedResult(IOConstants.EXPORT_FAILED_PREFIX, listOf(e.message ?: "")))
        }
    }

    suspend fun exportFavoritesToUri(
        context: Context,
        uri: Uri,
        favoriteRepository: FavoriteQuestionRepository,
        onResult: (Boolean) -> Unit,
        onMessage: (LocalizedResult) -> Unit
    ) {
        try {
            val favorites = favoriteRepository.getAll().firstOrNull() ?: emptyList()
            val out = context.contentResolver.openOutputStream(uri)
                ?: throw LocalizedException(IOConstants.CANNOT_WRITE_FILE)
            out.use { output ->
                val json = Json.encodeToString(favorites)
                output.write(json.toByteArray())
            }
            onResult(true)
            onMessage(LocalizedResult(IOConstants.EXPORT_PREFIX))
        } catch (e: Exception) {
            onResult(false)
            onMessage(LocalizedResult(IOConstants.EXPORT_FAILED_PREFIX, listOf(e.message ?: "")))
        }
    }
}

