package com.example.testapp.uicommon.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

private data class InlineTextLayout(
    val text: AnnotatedString,
    val formulas: List<InlineFormulaSpan>
)

private data class InlineFormulaSpan(
    val id: String,
    val latex: String,
    val forceLongStyle: Boolean,
    val placeholderWidth: TextUnit,
    val placeholderHeight: TextUnit
)

@Composable
internal fun RichTextInlineFlow(
    inlines: List<RichInline>,
    color: Color,
    fontSize: TextUnit,
    fontFamily: FontFamily?,
    modifier: Modifier = Modifier,
    baseWeight: FontWeight = FontWeight.Normal,
    lineSpacingMultiplier: Float = 1.32f,
    letterSpacing: Float = 0f
) {
    val inlineText = remember(inlines, baseWeight, fontSize) {
        buildInlineTextLayout(inlines, baseWeight, fontSize)
    }
    val inlineContent = inlineText.formulas.associate { formula ->
        formula.id to InlineTextContent(
            placeholder = Placeholder(
                width = formula.placeholderWidth,
                height = formula.placeholderHeight,
                placeholderVerticalAlign = PlaceholderVerticalAlign.AboveBaseline
            )
        ) {
            RichTextSmartFormula(
                formula = formula.latex,
                displayMode = false,
                modifier = Modifier,
                color = color,
                fontSize = fontSize,
                fontFamily = fontFamily,
                forceLongInlineStyle = formula.forceLongStyle
            )
        }
    }

    Text(
        text = inlineText.text,
        modifier = modifier.fillMaxWidth(),
        style = TextStyle(
            color = color,
            fontSize = fontSize,
            lineHeight = (fontSize.value * lineSpacingMultiplier).sp,
            letterSpacing = letterSpacing.sp,
            fontFamily = fontFamily
        ),
        inlineContent = inlineContent
    )
}

private fun buildInlineTextLayout(
    inlines: List<RichInline>,
    baseWeight: FontWeight,
    fontSize: TextUnit
): InlineTextLayout {
    val formulas = mutableListOf<InlineFormulaSpan>()
    val text = buildAnnotatedString {
        inlines.forEachIndexed { index, inline ->
            when (inline) {
                is RichInline.Text -> {
                    pushStyle(
                        SpanStyle(
                            fontWeight = if (inline.bold) FontWeight.Bold else baseWeight
                        )
                    )
                    append(inline.value)
                    pop()
                }
                is RichInline.Math -> {
                    val formula = inline.formula.preprocessFormula()
                    val plainTextFormula = formula.toPlainInlineTextOrNull()
                    if (plainTextFormula != null) {
                        append(plainTextFormula)
                    } else {
                        val id = "math_$index"
                        formulas += InlineFormulaSpan(
                            id = id,
                            latex = formula,
                            forceLongStyle = inline.forceLongStyle,
                            placeholderWidth = formula.estimatedInlineFormulaWidth(fontSize),
                            placeholderHeight = formula.estimatedInlineFormulaHeight(fontSize)
                        )
                        appendInlineContent(id, formula)
                    }
                }
            }
        }
    }
    return InlineTextLayout(text = text, formulas = formulas)
}
