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
    val style = questionTextStyle(questionFontSize, lineSpacingMultiplier, letterSpacing)
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = content,
            style = style,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        BasicTextField(
            value = answerText,
            onValueChange = onAnswerChange,
            enabled = !showResult,
            textStyle = style,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth()
        )
        if (showResult) {
            Text(
                text = correctAnswer,
                color = MaterialTheme.colorScheme.primary,
                fontSize = (questionFontSize - 1f).coerceAtLeast(10f).sp,
                fontFamily = LocalFontFamily.current
            )
        }
    }
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
