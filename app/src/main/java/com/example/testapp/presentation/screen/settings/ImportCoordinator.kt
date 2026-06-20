package com.example.testapp.presentation.screen.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.testapp.R
import com.example.testapp.domain.repository.FavoriteQuestionRepository
import com.example.testapp.domain.repository.QuestionRepository
import com.example.testapp.domain.repository.WrongBookRepository
import com.example.testapp.core.common.LocalizedResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImportCoordinator @Inject constructor() {

    suspend fun importQuestionsFromUris(
        context: Context,
        uris: List<Uri>,
        questionRepository: QuestionRepository,
        onProgress: (Float) -> Unit,
        onMessage: (LocalizedResult) -> Unit
    ): ImportResult {
        var total = 0
        val count = uris.size.coerceAtLeast(1)
        var duplicateFiles: List<String>? = null
        val failedFiles = mutableListOf<String>()

        for ((idx, uri) in uris.withIndex()) {
            val originalFileName = withContext(Dispatchers.IO) {
                try {
                    context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (_: SecurityException) {
                } catch (_: UnsupportedOperationException) {
                }
                getFileNameFromUri(context, uri)
            }

            try {
                val file = withContext(Dispatchers.IO) { uriToFile(context, uri) }
                if (file != null && originalFileName != null) {
                    if (file.length() == 0L) {
                        failedFiles.add(context.getString(R.string.file_empty_template, originalFileName))
                    } else {
                        try {
                            val importedCount = withContext(Dispatchers.IO) {
                                questionRepository.importFromFilesWithOrigin(listOf(file to originalFileName))
                            }
                            if (importedCount > 0) {
                                total += importedCount
                            } else {
                                failedFiles.add(context.getString(R.string.file_no_valid_data_template, originalFileName))
                            }
                        } catch (e: com.example.testapp.domain.LocalizedException) {
                            val reason = e.args.joinToString(";").take(50).ifBlank {
                                context.getString(R.string.unknown_error)
                            }
                            failedFiles.add(context.getString(R.string.file_failed_with_reason, originalFileName, reason))
                        } catch (e: Exception) {
                            val reason = e.message?.take(30) ?: context.getString(R.string.unknown_error)
                            failedFiles.add(context.getString(R.string.file_parse_failed_template, originalFileName, reason))
                        } finally {
                            withContext(Dispatchers.IO) {
                                runCatching { file.delete() }
                            }
                        }
                    }
                } else {
                    failedFiles.add(context.getString(R.string.file_cannot_read_template, originalFileName))
                }
            } catch (e: Exception) {
                val reason = e.message?.take(20) ?: context.getString(R.string.unknown_error)
                failedFiles.add(context.getString(R.string.file_processing_exception_detail, originalFileName, reason))
            }

            onProgress((idx + 1f) / count)
        }

        val success = total > 0
        val errorMessage = when {
            duplicateFiles?.isNotEmpty() == true && failedFiles.isNotEmpty() -> duplicateFiles!! + failedFiles
            duplicateFiles?.isNotEmpty() == true -> duplicateFiles
            failedFiles.isNotEmpty() -> failedFiles
            else -> null
        }
        val finalSuccess = success || (duplicateFiles?.isNotEmpty() == true)

        if (finalSuccess) {
            onMessage(LocalizedResult(com.example.testapp.domain.IOConstants.IMPORT_SUCCESS))
        } else {
            onMessage(LocalizedResult("import_failed_detail", listOf(errorMessage?.joinToString("\n") ?: "")))
        }

        return ImportResult(finalSuccess, errorMessage)
    }

    suspend fun importQuestionsFromFiles(
        context: Context,
        files: List<java.io.File>,
        questionRepository: QuestionRepository,
        onProgress: (Float) -> Unit,
        onMessage: (LocalizedResult) -> Unit
    ): ImportResult {
        var total = 0
        val count = files.size.coerceAtLeast(1)
        var duplicateFiles: List<String>? = null
        val failedFiles = mutableListOf<String>()

        for ((idx, file) in files.withIndex()) {
            val originalFileName = file.name
            try {
                if (!file.exists() || file.length() == 0L) {
                    failedFiles.add(context.getString(R.string.file_empty_template, originalFileName))
                } else {
                    try {
                        val importedCount = withContext(Dispatchers.IO) {
                            questionRepository.importFromFilesWithOrigin(listOf(file to originalFileName))
                        }
                        if (importedCount > 0) {
                            total += importedCount
                        } else {
                            failedFiles.add(context.getString(R.string.file_no_valid_data_template, originalFileName))
                        }
                    } catch (e: com.example.testapp.domain.LocalizedException) {
                        val reason = e.args.joinToString(";").take(50).ifBlank {
                            context.getString(R.string.unknown_error)
                        }
                        failedFiles.add(context.getString(R.string.file_failed_with_reason, originalFileName, reason))
                    } catch (e: Exception) {
                        val reason = e.message?.take(30) ?: context.getString(R.string.unknown_error)
                        failedFiles.add(context.getString(R.string.file_parse_failed_template, originalFileName, reason))
                    }
                }
            } catch (e: Exception) {
                val reason = e.message?.take(20) ?: context.getString(R.string.unknown_error)
                failedFiles.add(context.getString(R.string.file_processing_exception_detail, originalFileName, reason))
            }
            onProgress((idx + 1f) / count)
        }

        val success = total > 0
        val errorMessage = when {
            duplicateFiles?.isNotEmpty() == true && failedFiles.isNotEmpty() -> duplicateFiles!! + failedFiles
            duplicateFiles?.isNotEmpty() == true -> duplicateFiles
            failedFiles.isNotEmpty() -> failedFiles
            else -> null
        }
        val finalSuccess = success || (duplicateFiles?.isNotEmpty() == true)

        if (finalSuccess) {
            onMessage(LocalizedResult(com.example.testapp.domain.IOConstants.IMPORT_SUCCESS))
        } else {
            onMessage(LocalizedResult("import_failed_detail", listOf(errorMessage?.joinToString("\n") ?: "")))
        }

        return ImportResult(finalSuccess, errorMessage)
    }

    suspend fun importWrongBookFromUri(
        context: Context,
        uri: Uri,
        wrongBookRepository: WrongBookRepository,
        onMessage: (LocalizedResult) -> Unit
    ): Boolean {
        val file = withContext(Dispatchers.IO) { uriToFile(context, uri) }
        val count = if (file != null) wrongBookRepository.importFromFile(file) else 0
        val success = count > 0
        onMessage(if (success) LocalizedResult(com.example.testapp.domain.IOConstants.IMPORT_SUCCESS) else LocalizedResult(com.example.testapp.domain.IOConstants.IMPORT_FAILED_PREFIX))
        return success
    }

    suspend fun importFavoritesFromUri(
        context: Context,
        uri: Uri,
        favoriteRepository: FavoriteQuestionRepository,
        onMessage: (LocalizedResult) -> Unit
    ): Boolean {
        val file = withContext(Dispatchers.IO) { uriToFile(context, uri) }
        val count = if (file != null) favoriteRepository.importFromFile(file) else 0
        val success = count > 0
        onMessage(if (success) LocalizedResult(com.example.testapp.domain.IOConstants.IMPORT_SUCCESS) else LocalizedResult(com.example.testapp.domain.IOConstants.IMPORT_FAILED_PREFIX))
        return success
    }

    // --- File utilities ---

    internal fun uriToFile(context: Context, uri: Uri): java.io.File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val originalFileName = getFileNameFromUri(context, uri).orEmpty()
            val fileExtension = originalFileName.substringAfterLast('.', "")
                .takeIf { it.isNotBlank() }
            val suffix = if (fileExtension != null) ".${fileExtension.lowercase()}" else
                uri.lastPathSegment?.substringAfterLast('.', "")?.takeIf { it.isNotBlank() }?.let { ".${it.lowercase()}" }
            val tempFile = java.io.File.createTempFile("import_", suffix, context.cacheDir)

            tempFile.outputStream().use { output ->
                inputStream.use { input ->
                    input.copyTo(output)
                }
            }

            if (tempFile.length() == 0L) {
                tempFile.delete()
                return null
            }
            tempFile
        } catch (e: SecurityException) {
            null
        } catch (e: Exception) {
            null
        }
    }

    internal fun getFileNameFromUri(context: Context, uri: Uri): String? {
        return try {
            val resolver = context.contentResolver
            val cursor = resolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val idx = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) {
                        val displayName = it.getString(idx)
                        if (!displayName.isNullOrBlank()) return displayName
                    }
                }
            }
            uri.lastPathSegment ?: context.getString(R.string.unknown_file_template, System.currentTimeMillis())
        } catch (e: Exception) {
            context.getString(R.string.unknown_file_template, System.currentTimeMillis())
        }
    }
}

data class ImportResult(
    val success: Boolean,
    val errorMessage: List<String>?
)

