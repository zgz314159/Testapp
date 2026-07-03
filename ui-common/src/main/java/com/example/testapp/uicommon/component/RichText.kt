package com.example.testapp.uicommon.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.example.testapp.core.util.prepareRichDisplayText

@Composable
fun RichText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontSize: TextUnit = LocalFontSize.current,
    fontFamily: FontFamily? = LocalFontFamily.current,
    lineSpacingMultiplier: Float = 1.32f,
    letterSpacing: Float = 0f,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    if (maxLines != Int.MAX_VALUE) {
        Text(
            text = text.toRichTextCollapsedPreview(),
            modifier = modifier,
            color = color,
            fontSize = fontSize,
            fontFamily = fontFamily,
            maxLines = maxLines,
            overflow = overflow
        )
        return
    }

    RichTextContent(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontFamily = fontFamily,
        lineSpacingMultiplier = lineSpacingMultiplier,
        letterSpacing = letterSpacing
    )
}

fun String.containsLatex(): Boolean = RichTextLatexPattern.containsMatchIn(this)

@Composable
private fun RichTextContent(
    text: String,
    modifier: Modifier,
    color: Color,
    fontSize: TextUnit,
    fontFamily: FontFamily?,
    lineSpacingMultiplier: Float,
    letterSpacing: Float
) {
    val displayText = remember(text) { prepareRichDisplayText(text) }
    val blocks = remember(displayText) { RichTextParser.parseBlocks(displayText) }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        blocks.forEachIndexed { index, block ->
            key(index, block) {
                RichTextBlockView(
                    block = block,
                    color = color,
                    fontSize = fontSize,
                    fontFamily = fontFamily,
                    lineSpacingMultiplier = lineSpacingMultiplier,
                    letterSpacing = letterSpacing
                )
            }
        }
    }
}
