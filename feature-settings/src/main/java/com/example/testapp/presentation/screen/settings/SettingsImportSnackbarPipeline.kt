package com.example.testapp.presentation.screen.settings

data class ImportSnackbarResult(
    val message: String,
    val shouldNavigateHome: Boolean
)

data class ImportSnackbarMessages(
    val success: String,
    val failed: String,
    val partial: (successCount: Int, errorCount: Int, reasons: String) -> String,
    val failedWithReasons: (reasons: String) -> String
)

fun resolveImportSnackbarResult(
    totalCount: Int,
    success: Boolean,
    errorFiles: List<String>?,
    messages: ImportSnackbarMessages
): ImportSnackbarResult = when {
    success && errorFiles.isNullOrEmpty() -> ImportSnackbarResult(
        message = messages.success,
        shouldNavigateHome = true
    )
    success && !errorFiles.isNullOrEmpty() -> {
        val errorCount = errorFiles.size
        val successCount = totalCount - errorCount
        val reasons = errorFiles.take(2).joinToString("；") + if (errorFiles.size > 2) "…" else ""
        ImportSnackbarResult(
            message = messages.partial(successCount, errorCount, reasons),
            shouldNavigateHome = successCount > 0
        )
    }
    !success && !errorFiles.isNullOrEmpty() -> {
        val reasons = errorFiles.take(3).joinToString("；") + if (errorFiles.size > 3) "…" else ""
        ImportSnackbarResult(
            message = messages.failedWithReasons(reasons),
            shouldNavigateHome = false
        )
    }
    else -> ImportSnackbarResult(
        message = messages.failed,
        shouldNavigateHome = false
    )
}
