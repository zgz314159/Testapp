package com.example.testapp.presentation.screen.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import com.example.testapp.core.common.LocalizedResult
import com.example.testapp.domain.IOConstants
import com.example.testapp.domain.LocalizedException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/** Settings IO 统一 Uri 读写 — Import / JSON / Excel coordinators 共用。 */
@Singleton
class SettingsIoUriPipeline @Inject constructor() {

    fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val originalFileName = getFileNameFromUri(context, uri).orEmpty()
            val fileExtension = originalFileName.substringAfterLast('.', "")
                .takeIf { it.isNotBlank() }
            val suffix = if (fileExtension != null) {
                ".${fileExtension.lowercase()}"
            } else {
                uri.lastPathSegment?.substringAfterLast('.', "")?.takeIf { it.isNotBlank() }?.let { ".${it.lowercase()}" }
            }
            val tempFile = File.createTempFile("import_", suffix, context.cacheDir)
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
        } catch (_: SecurityException) {
            null
        } catch (_: Exception) {
            null
        }
    }

    fun getFileNameFromUri(context: Context, uri: Uri): String? {
        return try {
            val resolver = context.contentResolver
            val cursor = resolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) {
                        val displayName = it.getString(idx)
                        if (!displayName.isNullOrBlank()) return displayName
                    }
                }
            }
            uri.lastPathSegment ?: context.getString(
                com.example.testapp.feature.settings.R.string.unknown_file_template,
                System.currentTimeMillis(),
            )
        } catch (_: Exception) {
            context.getString(
                com.example.testapp.feature.settings.R.string.unknown_file_template,
                System.currentTimeMillis(),
            )
        }
    }

    fun takePersistableReadPermission(context: Context, uri: Uri) {
        try {
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (_: SecurityException) {
        } catch (_: UnsupportedOperationException) {
        }
    }

    fun writeJsonToUri(context: Context, uri: Uri, json: String) {
        val out = context.contentResolver.openOutputStream(uri)
            ?: throw LocalizedException(IOConstants.CANNOT_WRITE_FILE)
        out.use { it.write(json.toByteArray()) }
    }

    suspend fun writeWorkbookToUri(
        context: Context,
        uri: Uri,
        sheets: Map<String, List<List<String>>>,
        highlightedRowsBySheet: Map<String, Set<Int>> = emptyMap(),
    ) {
        val workbook = org.apache.poi.xssf.usermodel.XSSFWorkbook()
        val editedRowStyle = workbook.createCellStyle().apply {
            setFont(workbook.createFont().apply { color = org.apache.poi.ss.usermodel.Font.COLOR_RED.toShort() })
        }

        for ((name, rows) in sheets) {
            val sanitizedName = sanitizeSheetName(name)
            val sheet = workbook.createSheet(sanitizedName)
            val highlightedRows = highlightedRowsBySheet[sanitizedName].orEmpty()
            for ((rowIndex, columns) in rows.withIndex()) {
                val row = sheet.createRow(rowIndex)
                for ((columnIndex, cellValue) in columns.withIndex()) {
                    val cell = row.createCell(columnIndex)
                    cell.setCellValue(toExcelCellValue(cellValue))
                    if (rowIndex in highlightedRows) cell.cellStyle = editedRowStyle
                }
            }
        }

        withContext(Dispatchers.IO) {
            context.contentResolver.openOutputStream(uri)?.use { out -> workbook.write(out) }
                ?: throw LocalizedException(IOConstants.CANNOT_WRITE_FILE)
            workbook.close()
        }
    }

    suspend fun importQuizFile(
        context: Context,
        file: File,
        displayName: String,
        importBlock: suspend (List<Pair<File, String>>) -> Int,
    ): QuizFileImportOutcome {
        if (!file.exists() || file.length() == 0L) {
            return QuizFileImportOutcome(
                importedCount = 0,
                failureMessage = context.getString(
                    com.example.testapp.feature.settings.R.string.file_empty_template,
                    displayName,
                ),
            )
        }
        return try {
            val importedCount = withContext(Dispatchers.IO) {
                importBlock(listOf(file to displayName))
            }
            if (importedCount > 0) {
                QuizFileImportOutcome(importedCount = importedCount)
            } else {
                QuizFileImportOutcome(
                    importedCount = 0,
                    failureMessage = context.getString(
                        com.example.testapp.feature.settings.R.string.file_no_valid_data_template,
                        displayName,
                    ),
                )
            }
        } catch (e: LocalizedException) {
            val reason = e.args.joinToString(";").take(50).ifBlank {
                context.getString(com.example.testapp.feature.settings.R.string.unknown_error)
            }
            QuizFileImportOutcome(
                importedCount = 0,
                failureMessage = context.getString(
                    com.example.testapp.feature.settings.R.string.file_failed_with_reason,
                    displayName,
                    reason,
                ),
            )
        } catch (e: Exception) {
            val reason = e.message?.take(30)
                ?: context.getString(com.example.testapp.feature.settings.R.string.unknown_error)
            QuizFileImportOutcome(
                importedCount = 0,
                failureMessage = context.getString(
                    com.example.testapp.feature.settings.R.string.file_parse_failed_template,
                    displayName,
                    reason,
                ),
            )
        }
    }

    suspend fun importFromRepositoryUri(
        context: Context,
        uri: Uri,
        importBlock: suspend (File) -> Int,
        onMessage: (LocalizedResult) -> Unit,
    ): Boolean {
        val file = withContext(Dispatchers.IO) { uriToFile(context, uri) }
        val count = if (file != null) importBlock(file) else 0
        val success = count > 0
        onMessage(
            if (success) {
                LocalizedResult(IOConstants.IMPORT_SUCCESS)
            } else {
                LocalizedResult(IOConstants.IMPORT_FAILED_PREFIX)
            }
        )
        return success
    }

    private fun sanitizeSheetName(name: String): String {
        var result = name.replace(Regex("[\\\\/:*?\\[\\]]"), "_")
        if (result.length > 31) result = result.substring(0, 31)
        return if (result.isBlank()) "Sheet" else result
    }

    private fun toExcelCellValue(value: String): String {
        val sanitized = value.replace("\u0000", "")
        return if (sanitized.length > MAX_EXCEL_CELL_TEXT_LENGTH) {
            sanitized.take(MAX_EXCEL_CELL_TEXT_LENGTH)
        } else {
            sanitized
        }
    }

    private companion object {
        const val MAX_EXCEL_CELL_TEXT_LENGTH = 32_767
    }
}

data class QuizFileImportOutcome(
    val importedCount: Int,
    val failureMessage: String? = null,
)
