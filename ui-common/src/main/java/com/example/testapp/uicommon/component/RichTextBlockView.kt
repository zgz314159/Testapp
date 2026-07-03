package com.example.testapp.uicommon.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun RichTextBlockView(
    block: RichBlock,
    color: Color,
    fontSize: TextUnit,
    fontFamily: FontFamily?,
    lineSpacingMultiplier: Float,
    letterSpacing: Float
) {
    when (block) {
        is RichBlock.Heading -> RichTextInlineFlow(
            inlines = block.inlines,
            color = color,
            fontSize = (fontSize.value + (7 - block.level).coerceAtLeast(1)).sp,
            fontFamily = fontFamily,
            baseWeight = FontWeight.SemiBold,
            lineSpacingMultiplier = lineSpacingMultiplier,
            letterSpacing = letterSpacing
        )
        is RichBlock.Paragraph -> RichTextInlineFlow(
            block.inlines, color, fontSize, fontFamily,
            lineSpacingMultiplier = lineSpacingMultiplier,
            letterSpacing = letterSpacing
        )
        is RichBlock.Quote -> Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "\u258C",
                color = color.copy(alpha = 0.45f),
                fontSize = fontSize,
                fontFamily = fontFamily,
                modifier = Modifier.padding(end = 6.dp)
            )
            RichTextInlineFlow(
                inlines = block.inlines,
                color = color.copy(alpha = 0.86f),
                fontSize = fontSize,
                fontFamily = fontFamily,
                modifier = Modifier.weight(1f),
                lineSpacingMultiplier = lineSpacingMultiplier,
                letterSpacing = letterSpacing
            )
        }
        is RichBlock.BulletList -> Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            block.items.forEachIndexed { index, item ->
                val prefix = item.label ?: if (block.ordered) "${index + 1}." else "\u2022"
                RichTextInlineFlow(
                    inlines = listOf(RichInline.Text("$prefix ")) + item.inlines,
                    color = color,
                    fontSize = fontSize,
                    fontFamily = fontFamily,
                    modifier = Modifier.fillMaxWidth(),
                    lineSpacingMultiplier = lineSpacingMultiplier,
                    letterSpacing = letterSpacing
                )
            }
        }
        is RichBlock.MathBlock -> RichTextSmartFormula(
            formula = block.formula,
            displayMode = true,
            modifier = Modifier.fillMaxWidth(),
            color = color,
            fontSize = fontSize,
            fontFamily = fontFamily
        )
    }
}
