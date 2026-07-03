package com.example.testapp.presentation.screen.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.component.LocalFontSize
import com.example.testapp.uicommon.component.RichText
import com.example.testapp.uicommon.component.StemImagesSection

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
        .replace(PracticeRedundantBlankSeparatorRegex, "")
        .replace(PracticeTextResponseBlankRegex, "")
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
