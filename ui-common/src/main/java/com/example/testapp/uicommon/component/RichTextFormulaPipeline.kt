package com.example.testapp.uicommon.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import java.net.URLEncoder

internal val RichTextLatexPattern = Regex("""(\$\$?.+?\$\$?|\\(?:frac|sqrt|theta|sum|cos|sin|tan|text)\b|[A-Za-z0-9)\]}][_^][{]?[A-Za-z0-9+\-]+)""")
internal val RichTextHeadingRegex = Regex("""^(#{1,6})\s+(.+)$""")

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

internal const val InlineFormulaFontScale = 1.0f
internal const val BlockFormulaFontScale = 1.05f
internal const val InlineFormulaBitmapPadding = 0
internal const val BlockFormulaBitmapPadding = 2

internal data class FormulaParts(
    val latex: String,
    val unit: String?
)

internal fun String.shouldUseSvgFallback(): Boolean = SvgFallbackCommands.containsMatchIn(this)

internal fun String.shouldUseLongInlineStyle(): Boolean =
    FractionCommandRegex.findAll(this).take(2).count() >= 2

private fun String.containsFractionCommand(): Boolean = FractionCommandRegex.containsMatchIn(this)

internal fun String.toPlainUnitOrNull(): String? {
    val trimmed = trim()
    if (PlainUnitFormulaRegex.matches(trimmed)) return trimmed
    val latexUnit = trimmed.toDisplayUnitText()
    if (PlainUnitFormulaRegex.matches(latexUnit)) return latexUnit
    return TextUnitFormulaRegex.matchEntire(trimmed)?.groupValues?.get(1)?.toDisplayUnitText()
}

internal fun String.toPlainInlineTextOrNull(): String? {
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

private fun String.toPlainSymbolOrNull(): String? = when (this) {
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

private fun String.toSubscriptText(): String = map { char ->
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

internal fun String.estimatedInlineFormulaWidth(fontSize: TextUnit): TextUnit {
    val compact = replace(LatexTextCommandRegex, "$1")
        .replace(Regex("""\\[A-Za-z]+"""), "x")
        .replace(Regex("""[{}]"""), "")
    val units = (compact.length * 0.45f + 0.6f).coerceIn(1.4f, 10f)
    return (fontSize.value * units).sp
}

internal fun String.estimatedInlineFormulaHeight(fontSize: TextUnit): TextUnit {
    val scale = when {
        TallInlineFormulaRegex.containsMatchIn(this) -> 1.65f
        StackedInlineFormulaRegex.containsMatchIn(this) -> 1.35f
        else -> 1.15f
    }
    return (fontSize.value * scale).sp
}

internal fun String.preprocessFormula(): String = replace("*", " ").trim()

internal fun String.splitTrailingUnit(): FormulaParts {
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

internal fun String.toJLatexRenderableFormula(): String = replace("\\Omega", "\\mbox{\u03A9}")

internal fun String.toSvgDataUri(color: String, fontSizePx: Float): String {
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

private fun String.escapeSvgText(): String = replace("&", "&amp;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")
    .replace("\"", "&quot;")
    .replace("'", "&apos;")

internal fun Color.toSvgColor(): String {
    val red = (red * 255).toInt().coerceIn(0, 255)
    val green = (green * 255).toInt().coerceIn(0, 255)
    val blue = (blue * 255).toInt().coerceIn(0, 255)
    return "#%02X%02X%02X".format(red, green, blue)
}

internal fun String.toRichTextCollapsedPreview(): String = replaceRichTextAsteriskBreaks()
    .replace(Regex("""\$\$(.*?)\$\$""", RegexOption.DOT_MATCHES_ALL), " $1 ")
    .replace(Regex("""\$(.*?)\$"""), "$1")
    .replace(RichTextHeadingRegex, "$2")
    .replace(Regex("""(?m)^\s*>\s?"""), "")
    .replace(Regex("""(?m)^\s*[-*]\s+"""), "\u2022 ")
    .replace(Regex("""(?m)^\s*\d+[.)]\s+"""), "")
    .replace("**", "")

private fun String.replaceRichTextAsteriskBreaks(): String =
    replace("**", "\n\n").replace("*", "\n")
