package com.example.testapp.presentation.screen.settings

/** Excel 导出编排辅助 — 纯函数，无 Android / POI 依赖。 */
object SettingsExcelExportPipeline {

    const val QUESTION_EXPORT_DATA_START_ROW = 4

    fun <T> filterByFileName(
        items: List<T>,
        fileName: String?,
        fileNameOf: (T) -> String?,
    ): List<T> = if (fileName.isNullOrBlank()) items else items.filter { fileNameOf(it) == fileName }

    fun editedQuestionHighlightRows(
        exportRows: List<QuestionExportSnapshot>,
        dataStartRow: Int = QUESTION_EXPORT_DATA_START_ROW,
    ): Set<Int> = exportRows.mapIndexedNotNull { index, row ->
        if (row.question.isEdited) index + dataStartRow else null
    }.toSet()

    fun resolveSheetBaseName(fileName: String?): String? =
        fileName?.substringBeforeLast('.')?.ifBlank { null }
}
