package com.example.testapp.core.util

/**
 * 「答案标签侧重练习」过滤串的唯一编解码入口。
 *
 * 设置页 UI 与出题过滤（[transformQuestionVariantsForFillSettings]）
 * 必须共用本对象，禁止各自手写分隔符正则——历史上两端正则漂移
 * 曾导致多选标签失效与「暂无题目」。
 */
object AnswerTagFilterCodec {

    /** 兼容历史数据：全角/半角逗号、顿号、分号及空白均视为分隔符。 */
    private val SEPARATOR_REGEX = Regex("[,，、；;\\s]+")

    /** 规范分隔符；解码端所有版本均可识别。 */
    private const val CANONICAL_SEPARATOR = "、"

    fun decode(raw: String): List<String> =
        raw.split(SEPARATOR_REGEX)
            .map { it.trim() }
            .filter { it.isNotBlank() }

    fun encode(tags: List<String>): String =
        tags.map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(CANONICAL_SEPARATOR)
}
