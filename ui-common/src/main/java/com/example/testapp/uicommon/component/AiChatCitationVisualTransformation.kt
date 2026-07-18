package com.example.testapp.uicommon.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.TextUnit
import com.example.testapp.uicommon.design.AiChatCitationStylePipeline

/**
 * 回答排版样式（不改变文本内容与光标位置）：
 * - `[n]` 引用标记 → 小号蓝色；
 * - 行首「标签：」分节标题 → 加粗。
 */
class AiChatCitationVisualTransformation(
    private val citationColor: Color,
    private val citationFontSize: TextUnit,
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val styled = buildAnnotatedString {
            append(text)
            AiChatCitationStylePipeline.sectionTitleRanges(text.text).forEach { range ->
                addStyle(
                    style = SpanStyle(fontWeight = FontWeight.Bold),
                    start = range.first,
                    end = range.last + 1,
                )
            }
            AiChatCitationStylePipeline.ranges(text.text).forEach { range ->
                addStyle(
                    style = SpanStyle(color = citationColor, fontSize = citationFontSize),
                    start = range.first,
                    end = range.last + 1,
                )
            }
        }
        return TransformedText(styled, OffsetMapping.Identity)
    }
}
