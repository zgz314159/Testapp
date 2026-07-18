package com.example.testapp.data.repository

/**
 * 题库导入重复判定：同名（含忽略大小写）或仅扩展名不同视为同一题库。
 * 避免「xxx.xlsx」与「xxx」并存。名字有任何其它差异（如导出时间戳、副本编号）视为
 * 不同题库，允许导入——展示层完整显示文件名，由用户自行辨别。
 */
object ImportDuplicateFilePipeline {

    fun isDuplicate(existingFileNames: Collection<String>, requestedFileName: String): Boolean {
        if (requestedFileName.isBlank()) return false
        return existingFileNames.any { matchesSameQuizFile(it, requestedFileName) }
    }

    fun matchesSameQuizFile(storedFileName: String?, requestedFileName: String): Boolean {
        if (storedFileName.isNullOrBlank() || requestedFileName.isBlank()) return false

        val stored = normalizeBaseName(storedFileName)
        val requested = normalizeBaseName(requestedFileName)
        if (stored.equals(requested, ignoreCase = true)) return true

        val storedStem = stripKnownExtension(stored)
        val requestedStem = stripKnownExtension(requested)
        return storedStem.equals(requestedStem, ignoreCase = true)
    }

    private fun normalizeBaseName(fileName: String): String =
        fileName.replace('\\', '/').trim().substringAfterLast('/')

    private fun stripKnownExtension(fileName: String): String {
        val lower = fileName.lowercase()
        val suffixes = listOf(".xlsx", ".xls", ".docx", ".doc", ".json", ".sqlite", ".db", ".txt")
        for (suffix in suffixes) {
            if (lower.endsWith(suffix)) {
                return fileName.dropLast(suffix.length)
            }
        }
        return fileName
    }
}
