package com.example.testapp.presentation.screen.settings

import android.content.Context
import android.net.Uri
import com.example.testapp.R
import com.example.testapp.domain.IOConstants
import com.example.testapp.domain.repository.FavoriteQuestionRepository
import com.example.testapp.domain.repository.HistoryRepository
import com.example.testapp.domain.repository.QuestionAnalysisRepository
import com.example.testapp.domain.repository.QuestionAskRepository
import com.example.testapp.domain.repository.QuestionNoteRepository
import com.example.testapp.domain.repository.QuestionRepository
import com.example.testapp.domain.repository.WrongBookRepository
import com.example.testapp.core.common.LocalizedResult
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExcelExportCoordinator @Inject constructor(
    private val batchLoader: SupplementaryDataBatchLoader
) {
    private val sheetBuilder = ExcelSheetBuilder()

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
        onMessage: (LocalizedResult) -> Unit
    ) {
        onLoading(true)
        try {
            val questions = questionRepository.exportQuestions().let { exportedQuestions ->
                if (fileName.isNullOrBlank()) exportedQuestions
                else exportedQuestions.filter { it.fileName == fileName }
            }
            val supplementary = batchLoader.loadAll(
                questionIds = questions.map { it.id },
                analysisRepo = questionAnalysisRepository,
                askRepo = questionAskRepository,
                noteRepo = questionNoteRepository
            )
            val exportRows = sheetBuilder.buildQuestionExportSnapshots(questions, supplementary)

            val sheetName = fileName?.substringBeforeLast('.')?.ifBlank { null }
                ?: context.getString(R.string.export_sheet_question)
            val description = if (fileName.isNullOrBlank()) {
                context.getString(R.string.export_quiz_all_description)
            } else {
                context.getString(R.string.export_quiz_single_description, fileName)
            }

            val sheets = mapOf(
                sheetBuilder.sanitizeSheetName(sheetName) to sheetBuilder.buildStructuredQuestionExportRows(
                    context = context,
                    title = sheetName,
                    description = description,
                    rows = exportRows
                )
            )

            if (sheets.isEmpty()) return writeFallback(onLoading, onResult, onMessage)

            val highlightedRows = exportRows.mapIndexedNotNull { index, row ->
                if (row.question.isEdited) index + QUESTION_EXPORT_DATA_START_ROW else null
            }.toSet()
            sheetBuilder.writeWorkbookToUri(
                context = context,
                uri = uri,
                sheets = sheets,
                highlightedRowsBySheet = sheets.keys.associateWith { highlightedRows }
            )

            onLoading(false)
            onResult(true)
            onMessage(LocalizedResult(IOConstants.EXPORT_PREFIX))
        } catch (e: Exception) {
            onLoading(false); onResult(false)
            onMessage(LocalizedResult(IOConstants.EXPORT_FAILED_PREFIX, listOf(e.message ?: "")))
        } catch (t: Throwable) {
            onLoading(false); onResult(false)
            onMessage(LocalizedResult(IOConstants.EXPORT_FAILED_PREFIX, listOf(t.message ?: "")))
        }
    }

    suspend fun exportWrongBookToExcelFile(
        context: Context,
        uri: Uri,
        fileName: String?,
        wrongBookRepository: WrongBookRepository,
        questionAnalysisRepository: QuestionAnalysisRepository,
        questionAskRepository: QuestionAskRepository,
        questionNoteRepository: QuestionNoteRepository,
        onResult: (Boolean) -> Unit,
        onMessage: (LocalizedResult) -> Unit
    ) {
        try {
            val wrongs = (wrongBookRepository.getAll().firstOrNull() ?: emptyList()).let { wrongQuestions ->
                if (fileName.isNullOrBlank()) wrongQuestions else wrongQuestions.filter { it.question.fileName == fileName }
            }
            val supplementary = batchLoader.loadAll(
                questionIds = wrongs.map { it.question.id },
                analysisRepo = questionAnalysisRepository,
                askRepo = questionAskRepository,
                noteRepo = questionNoteRepository
            )
            val sheets = sheetBuilder.buildWrongBookExportSheets(
                context = context, wrongs = wrongs, supplementary = supplementary,
                defaultSheetName = fileName?.substringBeforeLast('.')?.ifBlank { null }
                    ?: context.getString(R.string.export_filename_prefix_wrongbook)
            )
            sheetBuilder.writeWorkbookToUri(context, uri, sheets)
            onResult(true)
            onMessage(LocalizedResult(IOConstants.EXPORT_PREFIX))
        } catch (e: Exception) {
            onResult(false)
            onMessage(LocalizedResult(IOConstants.EXPORT_FAILED_PREFIX, listOf(e.message ?: "")))
        }
    }

    suspend fun exportFavoritesToExcelFile(
        context: Context,
        uri: Uri,
        fileName: String?,
        favoriteRepository: FavoriteQuestionRepository,
        questionAnalysisRepository: QuestionAnalysisRepository,
        questionAskRepository: QuestionAskRepository,
        questionNoteRepository: QuestionNoteRepository,
        onResult: (Boolean) -> Unit,
        onMessage: (LocalizedResult) -> Unit
    ) {
        try {
            val favorites = (favoriteRepository.getAll().firstOrNull() ?: emptyList()).let { favoriteQuestions ->
                if (fileName.isNullOrBlank()) favoriteQuestions else favoriteQuestions.filter { it.question.fileName == fileName }
            }
            val supplementary = batchLoader.loadAll(
                questionIds = favorites.map { it.question.id },
                analysisRepo = questionAnalysisRepository,
                askRepo = questionAskRepository,
                noteRepo = questionNoteRepository
            )
            val sheets = sheetBuilder.buildFavoriteExportSheets(
                context = context, favorites = favorites, supplementary = supplementary,
                defaultSheetName = fileName?.substringBeforeLast('.')?.ifBlank { null }
                    ?: context.getString(R.string.export_filename_prefix_favorite)
            )
            sheetBuilder.writeWorkbookToUri(context, uri, sheets)
            onResult(true)
            onMessage(LocalizedResult(IOConstants.EXPORT_PREFIX))
        } catch (e: Exception) {
            onResult(false)
            onMessage(LocalizedResult(IOConstants.EXPORT_FAILED_PREFIX, listOf(e.message ?: "")))
        }
    }

    suspend fun exportHistoryToExcelFile(
        context: Context,
        uri: Uri,
        historyRepository: HistoryRepository,
        onResult: (Boolean) -> Unit,
        onMessage: (LocalizedResult) -> Unit
    ) {
        try {
            val history = historyRepository.getAll().firstOrNull() ?: emptyList()
            val headers = listOf(
                context.getString(R.string.export_header_score),
                context.getString(R.string.export_header_total),
                context.getString(R.string.export_header_unanswered),
                context.getString(R.string.export_header_timestamp)
            )
            val sheets = (historyRepository as? com.example.testapp.data.repository.HistoryRepositoryImpl)
                ?.prepareExportSheetForHistory(history, headers, context.getString(R.string.export_sheet_history))
                ?: emptyMap()

            if (sheets.isEmpty()) { onResult(false); onMessage(LocalizedResult(IOConstants.EXPORT_FAILED_PREFIX, listOf())); return }
            sheetBuilder.writeWorkbookToUri(context, uri, sheets)
            onResult(true)
            onMessage(LocalizedResult(IOConstants.EXPORT_PREFIX))
        } catch (e: Exception) {
            onResult(false)
            onMessage(LocalizedResult(IOConstants.EXPORT_FAILED_PREFIX, listOf(e.message ?: "")))
        }
    }

    private fun writeFallback(
        onLoading: (Boolean) -> Unit,
        onResult: (Boolean) -> Unit,
        onMessage: (LocalizedResult) -> Unit
    ) {
        onLoading(false); onResult(false)
        onMessage(LocalizedResult(IOConstants.EXPORT_FAILED_PREFIX, listOf()))
    }

    private companion object {
        const val QUESTION_EXPORT_DATA_START_ROW = 4
    }
}
