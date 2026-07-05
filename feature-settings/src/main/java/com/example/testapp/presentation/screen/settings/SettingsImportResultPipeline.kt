package com.example.testapp.presentation.screen.settings

import com.example.testapp.core.common.LocalizedResult
import com.example.testapp.domain.IOConstants

/** 批量导入结果聚合 — ImportCoordinator 专用，无 Android 依赖。 */
object SettingsImportResultPipeline {

    data class BatchAccumulator(
        var totalImported: Int = 0,
        var duplicateFiles: List<String>? = null,
        val failedFiles: MutableList<String> = mutableListOf(),
    )

    fun finalizeBatch(
        acc: BatchAccumulator,
        onMessage: (LocalizedResult) -> Unit,
    ): ImportResult {
        val success = acc.totalImported > 0
        val errorMessage = when {
            acc.duplicateFiles?.isNotEmpty() == true && acc.failedFiles.isEmpty() -> acc.duplicateFiles!! + acc.failedFiles
            acc.duplicateFiles?.isNotEmpty() == true -> acc.duplicateFiles
            acc.failedFiles.isNotEmpty() -> acc.failedFiles
            else -> null
        }
        val finalSuccess = success || (acc.duplicateFiles?.isNotEmpty() == true)
        if (finalSuccess) {
            onMessage(LocalizedResult(IOConstants.IMPORT_SUCCESS))
        } else {
            onMessage(LocalizedResult("import_failed_detail", listOf(errorMessage?.joinToString("\n") ?: "")))
        }
        return ImportResult(finalSuccess, errorMessage)
    }
}
