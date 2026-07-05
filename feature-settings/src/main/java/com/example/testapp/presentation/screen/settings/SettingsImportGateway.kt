package com.example.testapp.presentation.screen.settings

import android.content.Context
import android.net.Uri
import com.example.testapp.core.common.LocalizedResult
import com.example.testapp.domain.repository.FavoriteQuestionRepository
import com.example.testapp.domain.repository.QuestionRepository
import com.example.testapp.domain.repository.WrongBookRepository

interface SettingsImportGateway {
    suspend fun importQuestionsFromUris(
        context: Context,
        uris: List<Uri>,
        questionRepository: QuestionRepository,
        onProgress: (Float) -> Unit,
        onMessage: (LocalizedResult) -> Unit,
    ): ImportResult

    suspend fun importQuestionsFromFiles(
        context: Context,
        files: List<java.io.File>,
        questionRepository: QuestionRepository,
        onProgress: (Float) -> Unit,
        onMessage: (LocalizedResult) -> Unit,
    ): ImportResult

    suspend fun importWrongBookFromUri(
        context: Context,
        uri: Uri,
        wrongBookRepository: WrongBookRepository,
        onMessage: (LocalizedResult) -> Unit,
    ): Boolean

    suspend fun importFavoritesFromUri(
        context: Context,
        uri: Uri,
        favoriteRepository: FavoriteQuestionRepository,
        onMessage: (LocalizedResult) -> Unit,
    ): Boolean
}
