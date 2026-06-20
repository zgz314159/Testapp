package com.example.testapp.uicommon.component

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.testapp.core.util.prepareRichDisplayText
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.component.LocalFontSize
import java.net.URLEncoder
import kotlin.math.max
import ru.noties.jlatexmath.JLatexMathDrawable

private val LatexPattern = Regex("""(\$\$?.+?\$\$?|\\(?:frac|sqrt|theta|sum|cos|sin|tan|text)\b|[A-Za-z0-9)\]}][_^][{]?[A-Za-z0-9+\-]+)""")
private val HeadingRegex = Regex("""^(#{1,6})\s+(.+)$""")
private val BoldHeadingRegex = Regex("""^\s*\*\*(.+?)\*\*\s*$""")
private val NumberedSectionHeadingRegex = Regex("""^\s*\d+[.)]\s+.+[\uFF1A:]\s*$""")
private val BulletRegex = Regex("""^\s*[-*]\s+(.+)$""")
private val OrderedRegex = Regex("""^\s*\d+[.)]\s+(.+)$""")
private val QuoteRegex = Regex("""^\s*>\s?(.*)$""")
private val BoldRegex = Regex("""\*\*(.+?)\*\*""")
private val SvgFallbackCommands = Regex("""\\(matrix|pmatrix|bmatrix|vmatrix|integral|begin|end|overbrace|underbrace)\b""")
private val FormulaUnitRegex = Regex("""\s*[\uFF08(]\s*((?:\\text[{][^}]+[}]|\\(?:Omega|mu|degree|circ)|[A-Za-z\u03A9\u03BC\u00B0/%\u00B7.\-])+)\s*[\uFF09)]\s*$""")
private val PlainUnitFormulaRegex = Regex("""^[A-Za-z\u03A9\u03BC\u00B0/%\u00B7.\-]{1,8}$""")
private val TextUnitFormulaRegex = Regex("""\\text[{]\s*([A-Za-z\u03A9\u03BC\u00B0/%\u00B7.\-]{1,8})\s*[}]""")
private val LatexTextCommandRegex = Regex("""\\text[{]([^}]*)[}]""")
private val LatexUnitSymbolRegex = Regex("""\\(?:Omega|mu|degree|circ)\b""")
private val SimpleSymbolFormulaRegex = Regex("""^(\\[A-Za-z]+|[A-Za-z])(?:_[{]?([A-Za-z0-9]+)[}]?)?$""")
private val SimpleFunctionFormulaRegex = Regex("""^\\(sin|cos|tan)\s*(\\[A-Za-z]+|[A-Za-z])$""")
private val TallInlineFormulaRegex = Regex("""\\(frac|dfrac|tfrac|sqrt)\b""")
private val StackedInlineFormulaRegex = Regex("""(?:[_^][{][^}]+[}]|[_^][A-Za-z0-9])""")
private val FractionCommandRegex = Regex("""\\(?:dfrac|tfrac|frac)\b""")
private const val InlineFormulaFontScale = 1.0f
private const val BlockFormulaFontScale = 1.05f
private const val InlineFormulaBitmapPadding = 0
private const val BlockFormulaBitmapPadding = 2
private val InlineFormulaYOffset = (9).dp

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
            text = text.toCollapsedPreview(),
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

fun String.containsLatex(): Boolean = LatexPattern.containsMatchIn(this)

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
                RichBlockView(
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

@Composable
private fun RichBlockView(
    block: RichBlock,
    color: Color,
    fontSize: TextUnit,
    fontFamily: FontFamily?,
    lineSpacingMultiplier: Float,
    letterSpacing: Float
) {
    when (block) {
        is RichBlock.Heading -> RichInlineFlow(
            inlines = block.inlines,
            color = color,
            fontSize = (fontSize.value + (7 - block.level).coerceAtLeast(1)).sp,
            fontFamily = fontFamily,
            baseWeight = FontWeight.SemiBold,
            lineSpacingMultiplier = lineSpacingMultiplier,
            letterSpacing = letterSpacing
        )
        is RichBlock.Paragraph -> RichInlineFlow(
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
            RichInlineFlow(
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
                RichInlineFlow(
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
        is RichBlock.MathBlock -> SmartFormula(
            formula = block.formula,
            displayMode = true,
            modifier = Modifier.fillMaxWidth(),
            color = color,
            fontSize = fontSize,
            fontFamily = fontFamily
        )
    }
}

@Composable
private fun RichInlineFlow(
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
            SmartFormula(
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

@Composable
private fun SmartFormula(
    formula: String,
    displayMode: Boolean,
    modifier: Modifier,
    color: Color,
    fontSize: TextUnit,
    fontFamily: FontFamily?,
    maxWidthThreshold: Dp = 300.dp,
    forceLongInlineStyle: Boolean = false
) {
    val density = LocalDensity.current
    val thresholdPx = with(density) { maxWidthThreshold.roundToPx() }
    val formulaParts = remember(formula) { formula.preprocessFormula().splitTrailingUnit() }
    val useLongInlineStyle = !displayMode && (forceLongInlineStyle || formulaParts.latex.shouldUseLongInlineStyle())
    var renderedWidthPx by remember(formula) { mutableStateOf(0) }
    var showDialog by remember(formula) { mutableStateOf(false) }

    val plainUnit = formulaParts.latex.toPlainUnitOrNull()
    if (!displayMode && plainUnit != null) {
        Text(
            text = plainUnit,
            modifier = modifier,
            color = color,
            fontSize = fontSize,
            lineHeight = (fontSize.value * 1.32f).sp,
            fontFamily = fontFamily
        )
        return
    }

    BoxWithConstraints(modifier = modifier) {
        val availableMaxWidth = this.maxWidth
        val maxWidthPx = with(density) { availableMaxWidth.toPx() }.takeIf { it > 0f }
        val overflows = useLongInlineStyle || renderedWidthPx > thresholdPx || (displayMode && maxWidthPx?.let { renderedWidthPx > it } == true)
        val scale = maxWidthPx
            ?.takeIf { overflows && renderedWidthPx > 0 }
            ?.let { (it / renderedWidthPx).coerceIn(0.58f, 1f) }
            ?: 1f
        val formulaColor = if (overflows) Color(0xFFB86A00) else color

        Row(
            modifier = Modifier.clickable(enabled = overflows) { showDialog = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            NativeFormula(
                formula = formulaParts.latex,
                displayMode = displayMode,
                modifier = Modifier
                    .graphicsLayer(scaleX = scale, scaleY = scale)
                    .onGloballyPositioned { renderedWidthPx = it.size.width },
                color = formulaColor,
                fontSize = fontSize,
                fontFamily = fontFamily
            )
            formulaParts.unit?.let { unit ->
                Spacer(Modifier.width(3.dp))
                Text(
                    text = unit,
                    color = formulaColor,
                    fontSize = fontSize,
                    lineHeight = (fontSize.value * 1.32f).sp,
                    fontFamily = fontFamily
                )
            }
        }
    }

    if (showDialog) {
        Dialog(
            onDismissRequest = { showDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NativeFormula(
                        formula = formulaParts.latex,
                        displayMode = true,
                        modifier = Modifier,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = (fontSize.value * 1.12f).sp,
                        fontFamily = fontFamily
                    )
                    formulaParts.unit?.let { unit ->
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = unit,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = (fontSize.value * 1.12f).sp,
                            lineHeight = (fontSize.value * 1.12f * 1.32f).sp,
                            fontFamily = fontFamily
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NativeFormula(
    formula: String,
    displayMode: Boolean,
    modifier: Modifier,
    color: Color,
    fontSize: TextUnit,
    fontFamily: FontFamily?
) {
    val calibratedFontSize = (fontSize.value * if (displayMode) BlockFormulaFontScale else InlineFormulaFontScale).sp
    val density = LocalDensity.current
    val fontSizePx = with(density) { calibratedFontSize.toPx() }
    val bitmapPadding = if (displayMode) BlockFormulaBitmapPadding else InlineFormulaBitmapPadding
    val renderFormula = remember(formula) { formula.toJLatexRenderableFormula() }

    if (formula.shouldUseSvgFallback()) {
        SvgFormula(
            formula = renderFormula,
            modifier = if (displayMode) modifier else modifier.offset(y = InlineFormulaYOffset),
            color = color,
            fontSize = calibratedFontSize
        )
        return
    }

    val bitmap = remember(renderFormula, color, fontSizePx, bitmapPadding) {
        runCatching {
            JLatexMathDrawable.builder(renderFormula)
                .textSize(fontSizePx.coerceAtLeast(1f))
                .color(color.toArgb())
                .padding(bitmapPadding)
                .build()
                .toImageBitmap()
        }.getOrNull()
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = if (displayMode) modifier else modifier.offset(y = InlineFormulaYOffset),
            contentScale = ContentScale.Fit
        )
    } else {
        SvgFormula(
            formula = renderFormula,
            modifier = if (displayMode) modifier else modifier.offset(y = InlineFormulaYOffset),
            color = color,
            fontSize = calibratedFontSize
        )
    }
}

@Composable
private fun SvgFormula(
    formula: String,
    modifier: Modifier,
    color: Color,
    fontSize: TextUnit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val fontSizePx = with(density) { fontSize.toPx() }
    val svgData = remember(formula, color, fontSizePx) {
        formula.toSvgDataUri(
            color = color.toSvgColor(),
            fontSizePx = fontSizePx
        )
    }
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(svgData)
            .decoderFactory(SvgDecoder.Factory())
            .build(),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}

private fun String.shouldUseSvgFallback(): Boolean {
    return SvgFallbackCommands.containsMatchIn(this)
}

private fun String.shouldUseLongInlineStyle(): Boolean {
    return FractionCommandRegex.findAll(this).take(2).count() >= 2
}

private fun String.containsFractionCommand(): Boolean {
    return FractionCommandRegex.containsMatchIn(this)
}

private fun String.isPlainUnitFormula(): Boolean {
    return toPlainUnitOrNull() != null
}

private fun String.toPlainUnitOrNull(): String? {
    val trimmed = trim()
    if (PlainUnitFormulaRegex.matches(trimmed)) return trimmed
    val latexUnit = trimmed.toDisplayUnitText()
    if (PlainUnitFormulaRegex.matches(latexUnit)) return latexUnit
    return TextUnitFormulaRegex.matchEntire(trimmed)?.groupValues?.get(1)?.toDisplayUnitText()
}

private fun String.toPlainInlineTextOrNull(): String? {
    toPlainUnitOrNull()?.let { return it }
    if (contains("=") || contains("^") || containsFractionCommand()) return null
    SimpleFunctionFormulaRegex.matchEntire(trim())?.let { match ->
        return "${match.groupValues[1]}${match.groupValues[2].toPlainSymbolOrNull() ?: return null}"
    }
    return SimpleSymbolFormulaRegex.matchEntire(trim())?.let { match ->
        val base = match.groupValues[1].toPlainSymbolOrNull() ?: return null
        val subscript = match.groupValues.getOrNull(2)
            ?.takeIf(String::isNotEmpty)
            ?.toSubscriptText()
            .orEmpty()
        base + subscript
    }
}

private fun String.toPlainSymbolOrNull(): String? {
    return when (this) {
        "\\alpha" -> "\u03B1"
        "\\beta" -> "\u03B2"
        "\\theta" -> "\u03B8"
        "\\phi", "\\varphi" -> "\u03C6"
        "\\rho" -> "\u03C1"
        "\\sigma" -> "\u03C3"
        "\\Omega" -> "\u03A9"
        "\\mu" -> "\u03BC"
        else -> takeIf { length == 1 && it[0].isLetter() }
    }
}

private fun String.toSubscriptText(): String {
    return map { char ->
        when (char) {
            '0' -> '\u2080'
            '1' -> '\u2081'
            '2' -> '\u2082'
            '3' -> '\u2083'
            '4' -> '\u2084'
            '5' -> '\u2085'
            '6' -> '\u2086'
            '7' -> '\u2087'
            '8' -> '\u2088'
            '9' -> '\u2089'
            'a' -> '\u2090'
            'e' -> '\u2091'
            'h' -> '\u2095'
            'i' -> '\u1D62'
            'j' -> '\u2C7C'
            'k' -> '\u2096'
            'l' -> '\u2097'
            'm' -> '\u2098'
            'n' -> '\u2099'
            'o' -> '\u2092'
            'p' -> '\u209A'
            'r' -> '\u1D63'
            's' -> '\u209B'
            't' -> '\u209C'
            'u' -> '\u1D64'
            'v' -> '\u1D65'
            'x' -> '\u2093'
            else -> char
        }
    }.joinToString("")
}

private fun String.estimatedInlineFormulaWidth(fontSize: TextUnit): TextUnit {
    val compact = replace(LatexTextCommandRegex, "$1")
        .replace(Regex("""\\[A-Za-z]+"""), "x")
        .replace(Regex("""[{}]"""), "")
    val units = (compact.length * 0.45f + 0.6f).coerceIn(1.4f, 10f)
    return (fontSize.value * units).sp
}

private fun String.estimatedInlineFormulaHeight(fontSize: TextUnit): TextUnit {
    val scale = when {
        TallInlineFormulaRegex.containsMatchIn(this) -> 1.65f
        StackedInlineFormulaRegex.containsMatchIn(this) -> 1.35f
        else -> 1.15f
    }
    return (fontSize.value * scale).sp
}

private data class FormulaParts(
    val latex: String,
    val unit: String?
)

private fun String.preprocessFormula(): String {
    return replace("*", " ").trim()
}

private fun String.splitTrailingUnit(): FormulaParts {
    val match = FormulaUnitRegex.find(this) ?: return FormulaParts(this, null)
    return FormulaParts(
        latex = removeRange(match.range).trimEnd(),
        unit = "(${match.groupValues[1].toDisplayUnitText()})"
    )
}

private fun String.toDisplayUnitText(): String {
    val textExpanded = LatexTextCommandRegex.replace(this) { match ->
        match.groupValues[1].trim()
    }
    val symbolExpanded = LatexUnitSymbolRegex.replace(textExpanded) { match ->
        when (match.value) {
            "\\Omega" -> "\u03A9"
            "\\mu" -> "\u03BC"
            else -> "\u00B0"
        }
    }
    return symbolExpanded.replace("\\cdot", "\u00B7")
}

private fun String.toJLatexRenderableFormula(): String {
    return replace("\\Omega", "\\mbox{\u03A9}")
}

private fun String.toSvgDataUri(
    color: String,
    fontSizePx: Float
): String {
    val escapedText = escapeSvgText()
    val width = (escapedText.length * fontSizePx * 0.58f).coerceAtLeast(fontSizePx * 2f)
    val height = fontSizePx * 1.65f
    val baseline = fontSizePx * 1.18f
    val svg = """
        <svg xmlns="http://www.w3.org/2000/svg" width="$width" height="$height" viewBox="0 0 $width $height">
          <text x="0" y="$baseline" fill="$color" font-size="$fontSizePx" font-family="sans-serif">$escapedText</text>
        </svg>
    """.trimIndent()
    return "data:image/svg+xml;charset=utf-8," + URLEncoder.encode(svg, "UTF-8")
}

private fun String.escapeSvgText(): String {
    return replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
}

private fun Color.toSvgColor(): String {
    val red = (red * 255).toInt().coerceIn(0, 255)
    val green = (green * 255).toInt().coerceIn(0, 255)
    val blue = (blue * 255).toInt().coerceIn(0, 255)
    return "#%02X%02X%02X".format(red, green, blue)
}

private fun JLatexMathDrawable.toImageBitmap(): ImageBitmap {
    val width = max(1, intrinsicWidth)
    val height = max(1, intrinsicHeight)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    setBounds(0, 0, width, height)
    draw(Canvas(bitmap))
    return bitmap.asImageBitmap()
}

private fun String.toCollapsedPreview(): String {
    return replaceAsteriskBreaks()
        .replace(Regex("""\$\$(.*?)\$\$""", RegexOption.DOT_MATCHES_ALL), " $1 ")
        .replace(Regex("""\$(.*?)\$"""), "$1")
        .replace(HeadingRegex, "$2")
        .replace(Regex("""(?m)^\s*>\s?"""), "")
        .replace(Regex("""(?m)^\s*[-*]\s+"""), "\u2022 ")
        .replace(Regex("""(?m)^\s*\d+[.)]\s+"""), "")
        .replace("**", "")
}

private fun String.replaceAsteriskBreaks(): String {
    return replace("**", "\n\n").replace("*", "\n")
}
