package com.example.testapp.uicommon.design

import androidx.compose.ui.graphics.Color

/** Cursor / GitHub diff「新增行」风格答对高亮色（单一数据源）。 */
object AnswerCorrectHighlightTokens {
    val containerLight = Color(0xFFDFF7DF)
    val contentLight = Color(0xFF1A7F37)
    val containerDark = Color(0xFF033A16)
    val contentDark = Color(0xFF3FB950)
}

data class AnswerCorrectHighlightColors(
    val container: Color,
    val content: Color
)

object AnswerCorrectHighlightColorPipeline {

    fun resolve(darkTheme: Boolean): AnswerCorrectHighlightColors {
        val t = AnswerCorrectHighlightTokens
        return if (darkTheme) {
            AnswerCorrectHighlightColors(t.containerDark, t.contentDark)
        } else {
            AnswerCorrectHighlightColors(t.containerLight, t.contentLight)
        }
    }
}
