package com.example.testapp.uicommon.util

/**
 * 正确答案全屏编辑：可编辑正文与 DRAWING_* 标签分离，保存时再合并，避免丢图。
 */
object DrawingAnswerEditPipeline {
    private val TAG_REGEX = Regex("\\[(?:DRAWING_IMAGES|DRAWING_TABLE):[^\\]]*]")

    data class Split(val editableBody: String, val preservedTags: String)

    fun split(rawAnswer: String): Split {
        val tags = TAG_REGEX.findAll(rawAnswer).map { it.value }.toList()
        val body = stripDrawingTags(rawAnswer)
        val preserved = if (tags.isEmpty()) "" else tags.joinToString("\n")
        return Split(editableBody = body, preservedTags = preserved)
    }

    fun merge(editedBody: String, preservedTags: String): String {
        val body = editedBody.trim()
        val tags = preservedTags.trim()
        return when {
            body.isBlank() && tags.isBlank() -> ""
            tags.isBlank() -> body
            body.isBlank() -> tags
            else -> "$body\n$tags"
        }
    }
}
