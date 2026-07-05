package com.example.testapp.core.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Preserve Unicode names and only replace characters invalid for Windows filenames.
fun sanitizeFileName(input: String): String {
    var s = input.replace(Regex("[<>:\"/\\\\|?*\\p{Cntrl}]"), "-")
    s = s.replace(Regex("\\s+"), " ").trim()
    s = s.trimEnd('.', ' ')
    if (s.isEmpty()) return "export.xlsx"
    return s
}

fun buildTimestampedExportFileName(sourceFileName: String?, fallbackName: String): String {
    val baseName = sourceFileName
        ?.substringBeforeLast('.', sourceFileName)
        ?.takeIf { it.isNotBlank() }
        ?: fallbackName
    val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))
    return sanitizeFileName("${baseName}_$timestamp.xlsx")
}

