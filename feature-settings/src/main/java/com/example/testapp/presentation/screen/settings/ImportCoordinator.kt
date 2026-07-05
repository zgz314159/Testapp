package com.example.testapp.presentation.screen.settings

import android.content.Context
import android.net.Uri
import com.example.testapp.domain.repository.FavoriteQuestionRepository
import com.example.testapp.domain.repository.QuestionRepository
import com.example.testapp.domain.repository.WrongBookRepository
import com.example.testapp.core.common.LocalizedResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImportCoordinator @Inject constructor(
    private val ioUriPipeline: SettingsIoUriPipeline,
) : SettingsImportGateway {

    override suspend fun importQuestionsFromUris(
        context: Context,
        uris: List<Uri>,
        questionRepository: QuestionRepository,
        onProgress: (Float) -> Unit,
        onMessage: (LocalizedResult) -> Unit,
    ): ImportResult {
        val acc = SettingsImportResultPipeline.BatchAccumulator()
        val count = uris.size.coerceAtLeast(1)

        for ((idx, uri) in uris.withIndex()) {
            val originalFileName = withContext(Dispatchers.IO) {
                ioUriPipeline.takePersistableReadPermission(context, uri)
                ioUriPipeline.getFileNameFromUri(context, uri)
            }
            try {
                val file = withContext(Dispatchers.IO) { ioUriPipeline.uriToFile(context, uri) }
                if (file != null && originalFileName != null) {
                    processQuizFile(context, acc, file, originalFileName, questionRepository)
                    withContext(Dispatchers.IO) { runCatching { file.delete() } }
                } else {
                    acc.failedFiles.add(
                        context.getString(
                            com.example.testapp.feature.settings.R.string.file_cannot_read_template,
                            originalFileName,
                        )
                    )
                }
            } catch (e: Exception) {
                val reason = e.message?.take(20)
                    ?: context.getString(com.example.testapp.feature.settings.R.string.unknown_error)
                acc.failedFiles.add(
                    context.getString(
                        com.example.testapp.feature.settings.R.string.file_processing_exception_detail,
                        originalFileName,
                        reason,
                    )
                )
            }
            onProgress((idx + 1f) / count)
        }

        return SettingsImportResultPipeline.finalizeBatch(acc, onMessage)
    }

    override suspend fun importQuestionsFromFiles(
        context: Context,
        files: List<File>,
        questionRepository: QuestionRepository,
        onProgress: (Float) -> Unit,
        onMessage: (LocalizedResult) -> Unit,
    ): ImportResult {
        val acc = SettingsImportResultPipeline.BatchAccumulator()
        val count = files.size.coerceAtLeast(1)

        for ((idx, file) in files.withIndex()) {
            try {
                processQuizFile(context, acc, file, file.name, questionRepository)
            } catch (e: Exception) {
                val reason = e.message?.take(20)
                    ?: context.getString(com.example.testapp.feature.settings.R.string.unknown_error)
                acc.failedFiles.add(
                    context.getString(
                        com.example.testapp.feature.settings.R.string.file_processing_exception_detail,
                        file.name,
                        reason,
                    )
                )
            }
            onProgress((idx + 1f) / count)
        }

        return SettingsImportResultPipeline.finalizeBatch(acc, onMessage)
    }

    override suspend fun importWrongBookFromUri(
        context: Context,
        uri: Uri,
        wrongBookRepository: WrongBookRepository,
        onMessage: (LocalizedResult) -> Unit,
    ): Boolean = ioUriPipeline.importFromRepositoryUri(context, uri, wrongBookRepository::importFromFile, onMessage)

    override suspend fun importFavoritesFromUri(
        context: Context,
        uri: Uri,
        favoriteRepository: FavoriteQuestionRepository,
        onMessage: (LocalizedResult) -> Unit,
    ): Boolean = ioUriPipeline.importFromRepositoryUri(context, uri, favoriteRepository::importFromFile, onMessage)

    private suspend fun processQuizFile(
        context: Context,
        acc: SettingsImportResultPipeline.BatchAccumulator,
        file: File,
        displayName: String,
        questionRepository: QuestionRepository,
    ) {
        val outcome = ioUriPipeline.importQuizFile(context, file, displayName) { pairs ->
            questionRepository.importFromFilesWithOrigin(pairs)
        }
        acc.totalImported += outcome.importedCount
        outcome.failureMessage?.let(acc.failedFiles::add)
    }
}
