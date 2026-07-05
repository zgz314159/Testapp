package com.example.testapp.presentation.screen.settings

import android.content.Context
import android.net.Uri
import com.example.testapp.core.common.LocalizedResult
import com.example.testapp.domain.repository.FavoriteQuestionRepository
import com.example.testapp.domain.repository.QuestionRepository
import com.example.testapp.domain.repository.WrongBookRepository

interface SettingsJsonExportGateway {
    suspend fun exportQuestionsToFile(
        context: Context,
        uri: Uri,
        questionRepository: QuestionRepository,
        onResult: (Boolean) -> Unit,
        onMessage: (LocalizedResult) -> Unit,
    )

    suspend fun exportWrongBookToUri(
        context: Context,
        uri: Uri,
        wrongBookRepository: WrongBookRepository,
        onResult: (Boolean) -> Unit,
        onMessage: (LocalizedResult) -> Unit,
    )

    suspend fun exportFavoritesToUri(
        context: Context,
        uri: Uri,
        favoriteRepository: FavoriteQuestionRepository,
        onResult: (Boolean) -> Unit,
        onMessage: (LocalizedResult) -> Unit,
    )
}
