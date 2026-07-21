package com.example.testapp.uicommon.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import ru.noties.jlatexmath.JLatexMathDrawable

private val InlineFormulaYOffset = 9.dp

@Composable
internal fun RichTextSmartFormula(
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
        val overflows = useLongInlineStyle || renderedWidthPx > thresholdPx ||
            (displayMode && maxWidthPx?.let { renderedWidthPx > it } == true)
        val scale = maxWidthPx
            ?.takeIf { overflows && renderedWidthPx > 0 }
            ?.let { (it / renderedWidthPx).coerceIn(0.58f, 1f) }
            ?: 1f
        val formulaColor = if (overflows) MaterialTheme.colorScheme.tertiary else color

        Row(
            modifier = Modifier.clickable(enabled = overflows) { showDialog = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            RichTextNativeFormula(
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
                    RichTextNativeFormula(
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
internal fun RichTextNativeFormula(
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
        RichTextSvgFormula(
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
        RichTextSvgFormula(
            formula = renderFormula,
            modifier = if (displayMode) modifier else modifier.offset(y = InlineFormulaYOffset),
            color = color,
            fontSize = calibratedFontSize
        )
    }
}

@Composable
internal fun RichTextSvgFormula(
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
    val imageRequest = remember(context, svgData) {
        ImageRequest.Builder(context)
            .data(svgData)
            .decoderFactory(SvgDecoder.Factory())
            .build()
    }
    AsyncImage(
        model = imageRequest,
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}
