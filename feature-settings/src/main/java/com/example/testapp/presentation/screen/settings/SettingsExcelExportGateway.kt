package com.example.testapp.presentation.screen.settings

import android.content.Context
import android.net.Uri
import com.example.testapp.core.common.LocalizedResult
import com.example.testapp.domain.repository.FavoriteQuestionRepository
import com.example.testapp.domain.repository.HistoryRepository
import com.example.testapp.domain.repository.QuestionAnalysisRepository
import com.example.testapp.domain.repository.QuestionAskRepository
import com.example.testapp.domain.repository.QuestionNoteRepository
import com.example.testapp.domain.repository.QuestionRepository
import com.example.testapp.domain.repository.WrongBookRepository

interface SettingsExcelExportGateway {
    suspend fun exportQuestionsToExcelFile(
        context: Context,
        uri: Uri,
        fileName: String?,
        questionRepository: QuestionRepository,
        questionAnalysisRepository: QuestionAnalysisRepository,
        questionAskRepository: QuestionAskRepository,
        questionNoteRepository: QuestionNoteRepository,
        onLoading: (Boolean) -> Unit,
        onResult: (Boolean) -> Unit,
        onMessage: (LocalizedResult) -> Unit,
    )

    suspend fun exportWrongBookToExcelFile(
        context: Context,
        uri: Uri,
        fileName: String?,
        wrongBookRepository: WrongBookRepository,
        questionAnalysisRepository: QuestionAnalysisRepository,
        questionAskRepository: QuestionAskRepository,
        questionNoteRepository: QuestionNoteRepository,
        onResult: (Boolean) -> Unit,
        onMessage: (LocalizedResult) -> Unit,
    )

    suspend fun exportFavoritesToExcelFile(
        context: Context,
        uri: Uri,
        fileName: String?,
        favoriteRepository: FavoriteQuestionRepository,
        questionAnalysisRepository: QuestionAnalysisRepository,
        questionAskRepository: QuestionAskRepository,
        questionNoteRepository: QuestionNoteRepository,
        onResult: (Boolean) -> Unit,
        onMessage: (LocalizedResult) -> Unit,
    )

    suspend fun exportHistoryToExcelFile(
        context: Context,
        uri: Uri,
        historyRepository: HistoryRepository,
        onResult: (Boolean) -> Unit,
        onMessage: (LocalizedResult) -> Unit,
    )
}
