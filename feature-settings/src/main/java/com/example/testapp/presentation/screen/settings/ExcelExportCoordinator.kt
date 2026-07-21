package com.example.testapp.presentation.screen.settings

import android.content.Context
import android.net.Uri
import com.example.testapp.core.common.LocalizedResult
import com.example.testapp.domain.IOConstants
import com.example.testapp.domain.repository.FavoriteQuestionRepository
import com.example.testapp.domain.repository.HistoryRepository
import com.example.testapp.domain.repository.QuestionAnalysisRepository
import com.example.testapp.domain.repository.QuestionAskRepository
import com.example.testapp.domain.repository.QuestionNoteRepository
import com.example.testapp.domain.repository.QuestionRepository
import com.example.testapp.domain.repository.WrongBookRepository
import com.example.testapp.feature.settings.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExcelExportCoordinator @Inject constructor(
    private val batchLoader: SupplementaryDataBatchLoader,
    private val sheetBuilder: ExcelSheetBuilder,
    private val ioUriPipeline: SettingsIoUriPipeline,
) : SettingsExcelExportGateway {

    override suspend fun exportQuestionsToExcelFile(
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
    ) {
        onLoading(true)
        try {
            withContext(Dispatchers.IO) {
                val questions = SettingsExcelExportPipeline.filterByFileName(
                    questionRepository.exportQuestions(),
                    fileName,
                ) { it.fileName }
                val supplementary = batchLoader.loadAll(
                    questionIds = questions.map { it.id },
                    analysisRepo = questionAnalysisRepository,
                    askRepo = questionAskRepository,
                    noteRepo = questionNoteRepository,
                )
                val exportRows = sheetBuilder.buildQuestionExportSnapshots(questions, supplementary)
                val sheetName = SettingsExcelExportPipeline.resolveSheetBaseName(fileName)
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
                        rows = exportRows,
                    )
                )
                val highlightedRows = SettingsExcelExportPipeline.editedQuestionHighlightRows(exportRows)
                ioUriPipeline.writeWorkbookToUri(
                    context = context,
                    uri = uri,
                    sheets = sheets,
                    highlightedRowsBySheet = sheets.keys.associateWith { highlightedRows },
                )
            }

            onLoading(false)
            onResult(true)
            onMessage(LocalizedResult(IOConstants.EXPORT_PREFIX))
        } catch (e: Exception) {
            failExport(onLoading, onResult, onMessage, e.message)
        } catch (t: Throwable) {
            failExport(onLoading, onResult, onMessage, t.message)
        }
    }

    override suspend fun exportWrongBookToExcelFile(
        context: Context,
        uri: Uri,
        fileName: String?,
        wrongBookRepository: WrongBookRepository,
        questionAnalysisRepository: QuestionAnalysisRepository,
        questionAskRepository: QuestionAskRepository,
        questionNoteRepository: QuestionNoteRepository,
        onResult: (Boolean) -> Unit,
        onMessage: (LocalizedResult) -> Unit,
    ) {
        try {
            val wrongs = SettingsExcelExportPipeline.filterByFileName(
                wrongBookRepository.getAll().firstOrNull() ?: emptyList(),
                fileName,
            ) { it.question.fileName }
            val supplementary = batchLoader.loadAll(
                questionIds = wrongs.map { it.question.id },
                analysisRepo = questionAnalysisRepository,
                askRepo = questionAskRepository,
                noteRepo = questionNoteRepository,
            )
            val defaultSheetName = SettingsExcelExportPipeline.resolveSheetBaseName(fileName)
                ?: context.getString(R.string.export_filename_prefix_wrongbook)
            val sheets = sheetBuilder.buildWrongBookExportSheets(
                context = context,
                wrongs = wrongs,
                supplementary = supplementary,
                defaultSheetName = defaultSheetName,
            )
            ioUriPipeline.writeWorkbookToUri(context, uri, sheets)
            onResult(true)
            onMessage(LocalizedResult(IOConstants.EXPORT_PREFIX))
        } catch (e: Exception) {
            onResult(false)
            onMessage(LocalizedResult(IOConstants.EXPORT_FAILED_PREFIX, listOf(e.message ?: "")))
        }
    }

    override suspend fun exportFavoritesToExcelFile(
        context: Context,
        uri: Uri,
        fileName: String?,
        favoriteRepository: FavoriteQuestionRepository,
        questionAnalysisRepository: QuestionAnalysisRepository,
        questionAskRepository: QuestionAskRepository,
        questionNoteRepository: QuestionNoteRepository,
        onResult: (Boolean) -> Unit,
        onMessage: (LocalizedResult) -> Unit,
    ) {
        try {
            val favorites = SettingsExcelExportPipeline.filterByFileName(
                favoriteRepository.getAll().firstOrNull() ?: emptyList(),
                fileName,
            ) { it.question.fileName }
            val supplementary = batchLoader.loadAll(
                questionIds = favorites.map { it.question.id },
                analysisRepo = questionAnalysisRepository,
                askRepo = questionAskRepository,
                noteRepo = questionNoteRepository,
            )
            val defaultSheetName = SettingsExcelExportPipeline.resolveSheetBaseName(fileName)
                ?: context.getString(R.string.export_filename_prefix_favorite)
            val sheets = sheetBuilder.buildFavoriteExportSheets(
                context = context,
                favorites = favorites,
                supplementary = supplementary,
                defaultSheetName = defaultSheetName,
            )
            ioUriPipeline.writeWorkbookToUri(context, uri, sheets)
            onResult(true)
            onMessage(LocalizedResult(IOConstants.EXPORT_PREFIX))
        } catch (e: Exception) {
            onResult(false)
            onMessage(LocalizedResult(IOConstants.EXPORT_FAILED_PREFIX, listOf(e.message ?: "")))
        }
    }

    override suspend fun exportHistoryToExcelFile(
        context: Context,
        uri: Uri,
        historyRepository: HistoryRepository,
        onResult: (Boolean) -> Unit,
        onMessage: (LocalizedResult) -> Unit,
    ) {
        try {
            val history = historyRepository.getAll().firstOrNull() ?: emptyList()
            val headers = listOf(
                context.getString(R.string.export_header_score),
                context.getString(R.string.export_header_total),
                context.getString(R.string.export_header_unanswered),
                context.getString(R.string.export_header_timestamp),
            )
            val sheets = HistoryExcelExportPipeline.buildSheets(
                history = history,
                headers = headers,
                sheetName = context.getString(R.string.export_sheet_history),
            )

            if (sheets.isEmpty()) {
                onResult(false)
                onMessage(LocalizedResult(IOConstants.EXPORT_FAILED_PREFIX, listOf()))
                return
            }
            ioUriPipeline.writeWorkbookToUri(context, uri, sheets)
            onResult(true)
            onMessage(LocalizedResult(IOConstants.EXPORT_PREFIX))
        } catch (e: Exception) {
            onResult(false)
            onMessage(LocalizedResult(IOConstants.EXPORT_FAILED_PREFIX, listOf(e.message ?: "")))
        }
    }

    private fun failExport(
        onLoading: (Boolean) -> Unit,
        onResult: (Boolean) -> Unit,
        onMessage: (LocalizedResult) -> Unit,
        errorMessage: String? = null,
    ) {
        onLoading(false)
        onResult(false)
        onMessage(
            LocalizedResult(
                IOConstants.EXPORT_FAILED_PREFIX,
                listOfNotNull(errorMessage?.takeIf { it.isNotBlank() }),
            )
        )
    }
}
