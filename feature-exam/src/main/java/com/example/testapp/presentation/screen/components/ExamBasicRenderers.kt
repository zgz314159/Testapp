package com.example.testapp.presentation.screen.exam.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.feature.exam.R
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.component.LocalFontSize
import com.example.testapp.uicommon.component.StemImagesSection
import com.example.testapp.uicommon.component.InlineBlankVisualTransformation
import com.example.testapp.uicommon.component.buildInlineBlankEditorSpec
import com.example.testapp.uicommon.component.buildInlineEditorRawText
import com.example.testapp.uicommon.component.buildResultQuestionAnnotatedString
import com.example.testapp.uicommon.component.decodeCorrectAnswers
import com.example.testapp.uicommon.component.decodeUserAnswers
import com.example.testapp.uicommon.component.defaultInlineEditorSelection
import com.example.testapp.uicommon.component.encodeUserAnswers
import com.example.testapp.uicommon.component.normalizeInlineEditorValue
import com.example.testapp.uicommon.component.splitInlineEditorRawText
import com.example.testapp.uicommon.component.EditingBlue
import com.example.testapp.uicommon.component.RichText
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput as composePointerInput
import androidx.compose.foundation.gestures.detectTapGestures as composeDetectTapGestures
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.geometry.Offset

internal val CorrectGreen = Color(0xFF16A34A)
private val BlankRegex = Regex("_{2,}|（\\s*）|\\(\\s*\\)|【\\s*】|\\[\\s*]")

@Composable
fun RichText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontSize: TextUnit = LocalFontSize.current,
    fontFamily: FontFamily? = LocalFontFamily.current,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontFamily = fontFamily,
        maxLines = maxLines,
        overflow = overflow
    )
}

@Composable
fun ExamAnalysisSection(
    text: String?,
    collapsed: Boolean,
    scrollState: ScrollState,
    backgroundColor: Color,
    onToggle: () -> Unit,
    onDoubleTap: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null
) {
    if (text.isNullOrBlank()) return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(8.dp)
            .then(
                if (!collapsed) {
                    Modifier.heightIn(max = 400.dp).verticalScroll(scrollState)
                } else {
                    Modifier
                }
            )
            .pointerInput(text, collapsed) {
                detectTapGestures(
                    onTap = { onToggle() },
                    onDoubleTap = { onDoubleTap?.invoke() },
                    onLongPress = { onLongPress?.invoke() }
                )
            }
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = LocalFontSize.current,
            fontFamily = LocalFontFamily.current,
            maxLines = if (collapsed) 1 else Int.MAX_VALUE,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun StemContent(
    content: String,
    stemImages: List<String>,
    fontSize: TextUnit,
    fontFamily: FontFamily?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        RichText(
            text = content,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = fontSize,
            fontFamily = fontFamily
        )
        if (stemImages.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stemImages.joinToString("\n"),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = (fontSize.value * 0.85f).sp,
                fontFamily = fontFamily
            )
        }
    }
}

@Composable
fun InlineBlankQuestionContent(
    content: String,
    answerText: String,
    correctAnswer: String,
    questionFontSize: Float,
    lineSpacingMultiplier: Float = 1.3f,
    letterSpacing: Float = 0f,
    showResult: Boolean,
    onAnswerChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val matches = remember(content) { BlankRegex.findAll(content).toList() }
    val baseTextStyle = questionTextStyle(questionFontSize, lineSpacingMultiplier, letterSpacing)
    if (matches.isEmpty()) {
        Text(
            text = content,
            style = baseTextStyle,
            modifier = modifier.fillMaxWidth()
        )
        return
    }
    val blankCount = matches.size
    val userParts = remember(answerText, blankCount) { decodeUserAnswers(answerText, blankCount) }
    val correctParts = remember(correctAnswer, blankCount) { decodeCorrectAnswers(correctAnswer, blankCount) }
    val errorColor = MaterialTheme.colorScheme.error

    if (showResult) {
        val resultAnnotatedText = remember(content, matches, userParts, correctParts, questionFontSize, errorColor) {
            buildResultQuestionAnnotatedString(content = content, matches = matches, userParts = userParts, correctParts = correctParts, questionFontSize = questionFontSize, errorColor = errorColor)
        }
        Text(text = resultAnnotatedText, style = baseTextStyle, modifier = modifier.fillMaxWidth())
        return
    }

    InlineBlankSingleEditor(
        content = content,
        matches = matches,
        userParts = userParts,
        questionTextStyle = baseTextStyle,
        onAnswerChange = onAnswerChange,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun InlineBlankSingleEditor(
    content: String,
    matches: List<MatchResult>,
    userParts: List<String>,
    questionTextStyle: TextStyle,
    onAnswerChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val editorSpec = remember(content, matches) { buildInlineBlankEditorSpec(content, matches) }
    val density = LocalDensity.current
    val externalRawText = remember(userParts, editorSpec.blankCount) { buildInlineEditorRawText(userParts, editorSpec.blankCount) }
    var editorValue by remember(content) {
        mutableStateOf(TextFieldValue(text = externalRawText, selection = defaultInlineEditorSelection(externalRawText, editorSpec.blankCount)))
    }
    val visualTransformation = remember(editorSpec) { InlineBlankVisualTransformation(editorSpec) }
    val transformedEditorText = remember(editorValue.text, visualTransformation) { visualTransformation.filter(AnnotatedString(editorValue.text)) }
    val cursorHeightPx = with(density) { questionTextStyle.fontSize.toPx() }
    var isFocused by remember { mutableStateOf(false) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(externalRawText, editorSpec.blankCount) {
        if (editorValue.text != externalRawText) {
            editorValue = TextFieldValue(text = externalRawText, selection = defaultInlineEditorSelection(externalRawText, editorSpec.blankCount))
        }
    }

    BasicTextField(
        value = editorValue,
        onValueChange = { incomingValue ->
            val normalizedValue = normalizeInlineEditorValue(candidate = incomingValue, previous = editorValue, blankCount = editorSpec.blankCount)
            editorValue = normalizedValue
            onAnswerChange(encodeUserAnswers(splitInlineEditorRawText(normalizedValue.text, editorSpec.blankCount)))
        },
        textStyle = questionTextStyle,
        cursorBrush = SolidColor(Color.Transparent),
        modifier = modifier
            .focusRequester(focusRequester)
            .composePointerInput(transformedEditorText) {
                composeDetectTapGestures { tapOffset ->
                    focusRequester.requestFocus()
                    val layout = textLayoutResult ?: return@composeDetectTapGestures
                    val transformedOffset = layout.getOffsetForPosition(tapOffset)
                    val rawOffset = transformedEditorText.offsetMapping.transformedToOriginal(transformedOffset).coerceIn(0, editorValue.text.length)
                    editorValue = editorValue.copy(selection = TextRange(rawOffset))
                }
            }
            .onFocusChanged { isFocused = it.isFocused }
            .drawBehind {
                if (!isFocused || !editorValue.selection.collapsed) return@drawBehind
                val layout = textLayoutResult ?: return@drawBehind
                val transformedCursorOffset = transformedEditorText.offsetMapping.originalToTransformed(editorValue.selection.end)
                val cursorRect = layout.getCursorRect(transformedCursorOffset)
                val cursorTop = cursorRect.top + ((cursorRect.height - cursorHeightPx) / 2f).coerceAtLeast(0f)
                drawLine(color = EditingBlue, start = Offset(cursorRect.left, cursorTop), end = Offset(cursorRect.left, cursorTop + cursorHeightPx.coerceAtMost(cursorRect.height)), strokeWidth = 2f)
            },
        visualTransformation = visualTransformation,
        onTextLayout = { textLayoutResult = it }
    )
}

@Composable
fun TextAnswerQuestionContent(
    content: String,
    answerText: String,
    questionFontSize: Float,
    lineSpacingMultiplier: Float = 1.3f,
    letterSpacing: Float = 0f,
    showResult: Boolean,
    onAnswerChange: (String) -> Unit,
    stemImages: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    val displayContent = content
        .replace(BlankRegex, "")
        .replace(Regex("[ \\t]{2,}"), " ")
        .trim()

    RichText(
        text = displayContent.ifBlank { content },
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = questionFontSize.sp,
        fontFamily = LocalFontFamily.current,
        modifier = modifier.fillMaxWidth()
    )
    if (stemImages.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        StemImagesSection(
            imagePaths = stemImages,
            modifier = Modifier.fillMaxWidth()
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
    OutlinedTextField(
        value = answerText,
        onValueChange = onAnswerChange,
        enabled = !showResult,
        placeholder = {
            Text(
                text = stringResource(R.string.fill_answer_hint),
                fontSize = LocalFontSize.current,
                fontFamily = LocalFontFamily.current
            )
        },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3,
        maxLines = 8,
        textStyle = questionTextStyle(questionFontSize, lineSpacingMultiplier, letterSpacing)
    )
}

@Composable
private fun questionTextStyle(
    questionFontSize: Float,
    lineSpacingMultiplier: Float,
    letterSpacing: Float
): TextStyle = MaterialTheme.typography.titleMedium.copy(
    fontSize = questionFontSize.sp,
    lineHeight = (questionFontSize * lineSpacingMultiplier).sp,
    letterSpacing = letterSpacing.sp,
    fontFamily = LocalFontFamily.current,
    color = MaterialTheme.colorScheme.onSurface
)
