package com.example.testapp.presentation.screen.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
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
import com.example.testapp.uicommon.design.answerFeedbackColors
import com.example.testapp.uicommon.design.inlineBlankEditColors

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
    val matches = remember(content) { PracticeBlankRegex.findAll(content).toList() }
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
    val feedbackColors = answerFeedbackColors()
    val errorColor = feedbackColors.incorrectText
    val correctColor = feedbackColors.correctText

    if (showResult) {
        val resultAnnotatedText = remember(content, matches, userParts, correctParts, questionFontSize, correctColor, errorColor) {
            buildResultQuestionAnnotatedString(
                content = content,
                matches = matches,
                userParts = userParts,
                correctParts = correctParts,
                questionFontSize = questionFontSize,
                correctColor = correctColor,
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
    val editColors = inlineBlankEditColors()
    val visualTransformation = remember(editorSpec, editColors.blankText) {
        InlineBlankVisualTransformation(editorSpec, editColors.blankText)
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
                    color = editColors.cursor,
                    start = Offset(cursorRect.left, cursorTop),
                    end = Offset(
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
