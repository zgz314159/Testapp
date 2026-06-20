package com.example.testapp.presentation.screen.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.example.testapp.core.common.LocalizedResult
import com.example.testapp.domain.model.Question
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.component.LocalFontSize
import com.example.testapp.feature.practice.R
import com.example.testapp.uicommon.util.buildFillAnswerDisplayParts
import com.example.testapp.core.util.answerToOptionIndices
import com.example.testapp.core.util.resolveDisplayOptions
import com.example.testapp.uicommon.component.EditingBlue
import com.example.testapp.uicommon.component.RichText
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
import com.example.testapp.core.util.isFillAnswerCorrect
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue

private val CorrectGreen = Color(0xFF16A34A)
private val TextResponseBlankRegex = Regex("_{2,}|（\\s*）|\\(\\s*\\)|【\\s*】|\\[\\s*\\]")
private val RedundantBlankSeparatorRegex = Regex("[、,，;；:：]\\s*(?=(?:_{2,}|（\\s*）|\\(\\s*\\)|【\\s*】|\\[\\s*\\])|$)")

@Composable
fun PracticeExplanationBox(
    text: String,
    collapsed: Boolean,
    scrollStateProvider: () -> ScrollState,
    questionFontSize: Float,
    onToggle: () -> Unit
) {
    CollapsibleTextBox(
        text = text,
        collapsed = collapsed,
        scrollState = scrollStateProvider(),
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        questionFontSize = questionFontSize,
        onToggle = onToggle
    )
}

@Composable
fun PracticeNoteBox(
    note: String,
    collapsed: Boolean,
    scrollStateProvider: () -> ScrollState,
    questionFontSize: Float,
    onToggle: () -> Unit,
    onDoubleTap: () -> Unit,
    onLongPress: () -> Unit
) {
    CollapsibleTextBox(
        text = note,
        collapsed = collapsed,
        scrollState = scrollStateProvider(),
        backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
        questionFontSize = questionFontSize,
        onToggle = onToggle,
        onDoubleTap = onDoubleTap,
        onLongPress = onLongPress
    )
}

@Composable
fun ExamAnalysisSection(
    text: String,
    collapsed: Boolean,
    scrollState: ScrollState,
    backgroundColor: Color,
    onToggle: () -> Unit,
    onDoubleTap: () -> Unit,
    onLongPress: () -> Unit
) {
    CollapsibleTextBox(
        text = text,
        collapsed = collapsed,
        scrollState = scrollState,
        backgroundColor = backgroundColor,
        questionFontSize = LocalFontSize.current.value,
        onToggle = onToggle,
        onDoubleTap = onDoubleTap,
        onLongPress = onLongPress
    )
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun CollapsibleTextBox(
    text: String,
    collapsed: Boolean,
    scrollState: ScrollState,
    backgroundColor: Color,
    questionFontSize: Float,
    onToggle: () -> Unit,
    onDoubleTap: () -> Unit = onToggle,
    onLongPress: () -> Unit = onToggle
) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .combinedClickable(
                onClick = onToggle,
                onDoubleClick = onDoubleTap,
                onLongClick = onLongPress
            )
            .padding(8.dp)
            .then(if (collapsed) Modifier else Modifier.verticalScroll(scrollState)),
        fontSize = questionFontSize.sp,
        fontFamily = LocalFontFamily.current,
        maxLines = if (collapsed) 3 else Int.MAX_VALUE,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun PracticeConfirmDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    message: String,
    confirmLabel: String,
    dismissLabel: String,
    onConfirm: () -> Unit
) {
    if (!show) return
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
                onConfirm()
            }) { Text(confirmLabel) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(dismissLabel) }
        },
        text = { Text(message) }
    )
}

@Composable
fun PracticeChatGptDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    loading: Boolean,
    resultPair: Pair<Int, LocalizedResult>?,
    currentIndex: Int,
    onSaveToAnalysis: (String) -> Unit
) {
    if (!show) return
    val resultText = resultPair?.second?.toString().orEmpty()
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                if (resultPair?.first == currentIndex && resultText.isNotBlank()) {
                    onSaveToAnalysis(resultText)
                }
                onDismiss()
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        text = {
            Column {
                if (loading) {
                    CircularProgressIndicator()
                } else {
                    Text(resultText)
                }
            }
        }
    )
}

private val BLANK_REGEX = Regex("_{2,}|（\\s*）|\\(\\s*\\)|【\\s*】|\\[\\s*]")

@Composable
fun rememberQuestionTextStyle(
    questionFontSize: Float,
    lineSpacingMultiplier: Float,
    letterSpacing: Float = 0f,
    color: Color = MaterialTheme.colorScheme.onSurface
): TextStyle {
    return MaterialTheme.typography.titleMedium.copy(
        fontSize = questionFontSize.sp,
        lineHeight = (questionFontSize * lineSpacingMultiplier).sp,
        letterSpacing = letterSpacing.sp,
        fontFamily = LocalFontFamily.current,
        color = color,
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    )
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
    val matches = remember(content) { BLANK_REGEX.findAll(content).toList() }
    val questionTextStyle = rememberQuestionTextStyle(
        questionFontSize = questionFontSize,
        lineSpacingMultiplier = lineSpacingMultiplier,
        letterSpacing = letterSpacing
    )
    if (matches.isEmpty()) {
        Text(
            text = content,
            style = questionTextStyle,
            modifier = modifier.fillMaxWidth()
        )
        FillBlankAnswerField(
            answerText = answerText,
            correctAnswer = correctAnswer,
            showResult = showResult,
            onAnswerChange = onAnswerChange
        )
        return
    }

    val blankCount = matches.size
    val userParts = remember(answerText, blankCount) {
        decodeUserAnswers(answerText, blankCount)
    }
    val correctParts = remember(correctAnswer, blankCount) {
        decodeCorrectAnswers(correctAnswer, blankCount)
    }
    val errorColor = MaterialTheme.colorScheme.error

    if (showResult) {
        val resultAnnotatedText = remember(content, matches, userParts, correctParts, questionFontSize, errorColor) {
            buildResultQuestionAnnotatedString(
                content = content,
                matches = matches,
                userParts = userParts,
                correctParts = correctParts,
                questionFontSize = questionFontSize,
                errorColor = errorColor
            )
        }

        Text(
            text = resultAnnotatedText,
            style = questionTextStyle,
            modifier = modifier.fillMaxWidth()
        )
        return
    }

    InlineBlankSingleEditor(
        content = content,
        matches = matches,
        userParts = userParts,
        questionTextStyle = questionTextStyle,
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
    val editorSpec = remember(content, matches) {
        buildInlineBlankEditorSpec(content, matches)
    }
    val density = LocalDensity.current
    val externalRawText = remember(userParts, editorSpec.blankCount) {
        buildInlineEditorRawText(userParts, editorSpec.blankCount)
    }
    var editorValue by remember(content) {
        mutableStateOf(
            TextFieldValue(
                text = externalRawText,
                selection = defaultInlineEditorSelection(externalRawText, editorSpec.blankCount)
            )
        )
    }
    val visualTransformation = remember(editorSpec) {
        InlineBlankVisualTransformation(editorSpec)
    }
    val transformedEditorText = remember(editorValue.text, visualTransformation) {
        visualTransformation.filter(AnnotatedString(editorValue.text))
    }
    val cursorHeightPx = with(density) { questionTextStyle.fontSize.toPx() }
    var isFocused by remember { mutableStateOf(false) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(externalRawText, editorSpec.blankCount) {
        if (editorValue.text != externalRawText) {
            editorValue = TextFieldValue(
                text = externalRawText,
                selection = defaultInlineEditorSelection(externalRawText, editorSpec.blankCount)
            )
        }
    }

    BasicTextField(
        value = editorValue,
        onValueChange = { incomingValue ->
            val normalizedValue = normalizeInlineEditorValue(
                candidate = incomingValue,
                previous = editorValue,
                blankCount = editorSpec.blankCount
            )
            editorValue = normalizedValue
            onAnswerChange(
                encodeUserAnswers(
                    splitInlineEditorRawText(normalizedValue.text, editorSpec.blankCount)
                )
            )
        },
        textStyle = questionTextStyle,
        cursorBrush = SolidColor(Color.Transparent),
        modifier = modifier
            .focusRequester(focusRequester)
            .pointerInput(transformedEditorText) {
                detectTapGestures { tapOffset ->
                    focusRequester.requestFocus()
                    val layout = textLayoutResult ?: return@detectTapGestures
                    val transformedOffset = layout.getOffsetForPosition(tapOffset)
                    val rawOffset = transformedEditorText.offsetMapping
                        .transformedToOriginal(transformedOffset)
                        .coerceIn(0, editorValue.text.length)
                    editorValue = editorValue.copy(selection = TextRange(rawOffset))
                }
            }
            .onFocusChanged { isFocused = it.isFocused }
            .drawBehind {
                if (!isFocused || !editorValue.selection.collapsed) return@drawBehind

                val layout = textLayoutResult ?: return@drawBehind
                val transformedCursorOffset = transformedEditorText.offsetMapping.originalToTransformed(
                    editorValue.selection.end
                )
                val cursorRect = layout.getCursorRect(transformedCursorOffset)
                val cursorTop = cursorRect.top + ((cursorRect.height - cursorHeightPx) / 2f).coerceAtLeast(0f)
                drawLine(
                    color = EditingBlue,
                    start = androidx.compose.ui.geometry.Offset(cursorRect.left, cursorTop),
                    end = androidx.compose.ui.geometry.Offset(
                        cursorRect.left,
                        cursorTop + cursorHeightPx.coerceAtMost(cursorRect.height)
                    ),
                    strokeWidth = 2f
                )
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
    lineSpacingMultiplier: Float,
    letterSpacing: Float,
    showResult: Boolean,
    onAnswerChange: (String) -> Unit,
    stemImages: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    val displayContent = content
        .replace(RedundantBlankSeparatorRegex, "")
        .replace(TextResponseBlankRegex, "")
        .replace(Regex("[ \\t]{2,}"), " ")
        .replace(Regex("\\n{3,}"), "\n\n")
        .trim()

    RichText(
        text = displayContent.ifBlank { content },
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = questionFontSize.sp,
        fontFamily = LocalFontFamily.current,
        lineSpacingMultiplier = lineSpacingMultiplier,
        letterSpacing = letterSpacing,
        modifier = modifier.fillMaxWidth(),
        overflow = TextOverflow.Clip
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
                text = "在此输入答案",
                fontSize = LocalFontSize.current,
                fontFamily = LocalFontFamily.current
            )
        },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3,
        maxLines = 8,
        textStyle = rememberQuestionTextStyle(
            questionFontSize = questionFontSize,
            lineSpacingMultiplier = lineSpacingMultiplier,
            letterSpacing = letterSpacing
        )
    )
}

@Composable
fun StemContent(
    content: String,
    stemImages: List<String>,
    fontSize: TextUnit,
    fontFamily: FontFamily?,
    lineSpacingMultiplier: Float = 1.3f,
    letterSpacing: Float = 0f,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        RichText(
            text = content,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = fontSize,
            fontFamily = fontFamily,
            lineSpacingMultiplier = lineSpacingMultiplier,
            letterSpacing = letterSpacing
        )
        if (stemImages.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            StemImagesSection(
                imagePaths = stemImages,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ExamOptionsList(
    question: Question,
    questionFontSize: Float,
    lineSpacingMultiplier: Float,
    letterSpacing: Float = 0f,
    selectedOption: List<Int>,
    showResult: Boolean,
    onOptionClick: (Int) -> Unit
) {
    val displayOptions = resolveDisplayOptions(question)
    val correctIndices = answerToOptionIndices(question)
    val optionFontSize = (questionFontSize - 1.5f).coerceAtLeast(12f)

    displayOptions.forEachIndexed { idx, option ->
        val isSelected = selectedOption.contains(idx)
        val isCorrect = showResult && correctIndices.contains(idx)
        val isWrong = showResult && isSelected && !isCorrect

        val backgroundColor = when {
            isCorrect -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            isWrong -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
            isSelected -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
            else -> MaterialTheme.colorScheme.surface
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .background(backgroundColor)
                .clickable(enabled = !showResult) { onOptionClick(idx) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = null,
                    enabled = !showResult,
                    modifier = Modifier.scale(1.5f)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            RichText(
                text = option,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = optionFontSize.sp,
                fontFamily = LocalFontFamily.current,
                lineSpacingMultiplier = lineSpacingMultiplier,
                letterSpacing = letterSpacing
            )
        }
    }
}

@Composable
fun FillAnswerResultText(
    content: String,
    userAnswer: String,
    correctAnswer: String,
    questionFontSize: Float,
    allCorrect: Boolean,
    letterSpacing: Float
) {
    val style = TextStyle(
        fontSize = questionFontSize.sp,
        lineHeight = (questionFontSize * 1.28f).sp,
        letterSpacing = letterSpacing.sp,
        fontFamily = LocalFontFamily.current,
        color = if (allCorrect) CorrectGreen else MaterialTheme.colorScheme.error
    )

    if (allCorrect) {
        Text(text = stringResource(R.string.answer_correct), style = style)
        return
    }

    val parts = buildFillAnswerDisplayParts(content, correctAnswer, userAnswer)
    val annotated = buildAnnotatedString {
        append(stringResource(R.string.answer_wrong_format, ""))
        parts.forEachIndexed { index, part ->
            if (index > 0) append("；")
            withStyle(
                SpanStyle(
                    color = if (part.isCorrect) CorrectGreen else MaterialTheme.colorScheme.error
                )
            ) {
                append(part.label)
                append(part.value)
                part.appendedCorrectValue?.let { appendedCorrect ->
                    withStyle(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.error,
                            fontSize = (questionFontSize - 2f).coerceAtLeast(10f).sp
                        )
                    ) {
                        append("（$appendedCorrect）")
                    }
                }
            }
        }
    }

    Text(text = annotated, style = style)
}

@Composable
fun FillBlankAnswerField(
    answerText: String,
    correctAnswer: String,
    showResult: Boolean,
    onAnswerChange: (String) -> Unit
) {
    val isCorrect = showResult && isFillAnswerCorrect(answerText, correctAnswer)
    val displayValue = if (!showResult) {
        answerText
    } else if (isCorrect) {
        answerText
    } else {
        "（$correctAnswer）"
    }

    val bgColor = when {
        !showResult -> MaterialTheme.colorScheme.surface
        isCorrect -> Color(0xFFE4F6EA)
        else -> Color(0xFFFDE8E8)
    }

    OutlinedTextField(
        value = displayValue,
        onValueChange = { onAnswerChange(it) },
        enabled = !showResult,
        placeholder = {
            Text(
                text = "在此输入答案",
                fontSize = LocalFontSize.current,
                fontFamily = LocalFontFamily.current
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(vertical = 4.dp),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontSize = LocalFontSize.current,
            fontFamily = LocalFontFamily.current,
            lineHeight = (LocalFontSize.current.value * 1.2f).sp
        )
    )
}

