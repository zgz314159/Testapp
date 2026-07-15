package com.example.testapp.data.repository.parser

import com.example.testapp.domain.QuestionTypes

/** Excel 导入边界：选择/判断答案与选项对齐，供答题索引判定使用。 */
object ExcelImportAnswerNormalizePipeline {

    private val CHOICE_LETTER_REGEX = Regex("[A-Za-zＡ-Ｚａ-ｚ]")
    private val IGNORED_CONTENT_PREFIXES = listOf("注意事项", "注意：", "备注：", "说明：")

    fun shouldSkipInstructionRow(content: String, rawType: String): Boolean {
        val trimmedContent = content.trim()
        if (trimmedContent.isBlank()) return true
        if (normalizeExcelHeader(trimmedContent) in setOf("题干", "题目", "内容", "题目内容")) return true
        if (rawType.isBlank() && IGNORED_CONTENT_PREFIXES.any { trimmedContent.startsWith(it) }) return true
        if (IGNORED_CONTENT_PREFIXES.any { normalizeExcelHeader(trimmedContent).startsWith(normalizeExcelHeader(it)) }) {
            return true
        }
        return false
    }

    fun normalizeChoiceAnswer(type: String, rawAnswer: String, optionCount: Int): String? {
        val trimmed = rawAnswer.trim()
        if (trimmed.isBlank()) return null

        if (QuestionTypes.isJudge(type)) {
            return normalizeJudgeAnswer(trimmed)
        }
        if (!QuestionTypes.isSingle(type) && !QuestionTypes.isMulti(type)) {
            return trimmed
        }

        val letters = CHOICE_LETTER_REGEX.findAll(trimmed)
            .map { it.value.uppercase().first().let { ch -> if (ch in 'Ａ'..'Ｚ') 'A' + (ch - 'Ａ') else ch } }
            .filter { it in 'A'..'Z' }
            .distinct()
            .sorted()
            .toList()

        if (letters.isEmpty()) return null
        if (optionCount > 0 && letters.any { it - 'A' >= optionCount }) return null
        return letters.joinToString("")
    }

    fun normalizeJudgeAnswer(raw: String): String? {
        val key = raw.trim().uppercase()
        return when {
            key in setOf("对", "正确", "T", "TRUE", "YES", "Y") ||
                key == "√" || key == "✓" || key == "\u221A" || key == "\u2713" -> "对"
            key in setOf("错", "错误", "F", "FALSE", "NO", "N") ||
                key == "×" || key == "✗" || key == "\u00D7" || key == "\u2717" -> "错"
            else -> null
        }
    }
}
