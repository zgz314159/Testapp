package com.example.testapp.uicommon.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.example.testapp.uicommon.design.AiChatPromptDesignTokens
import com.example.testapp.uicommon.design.AppSpacing
import com.example.testapp.uicommon.model.AiChatMessageRole

@Composable
fun AiChatBubble(
    role: AiChatMessageRole,
    content: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    assistantFontSize: TextUnit = LocalFontSize.current,
    assistantFontFamily: FontFamily? = LocalFontFamily.current,
    assistantContent: (@Composable (String) -> Unit)? = null,
    onAssistantContentChange: ((String) -> Unit)? = null,
    geminiStyle: Boolean = true,
) {
    val scheme = MaterialTheme.colorScheme
    val tokens = AiChatPromptDesignTokens
    val useBubble = com.example.testapp.uicommon.design.AiChatBubbleLayoutPipeline
        .assistantUsesBubble(role, geminiStyle) || isError
    val maxWidthFraction = com.example.testapp.uicommon.design.AiChatBubbleLayoutPipeline
        .userMaxWidthFraction(geminiStyle)

    if (!useBubble && role == AiChatMessageRole.Assistant) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = AppSpacing.xs),
            shape = RoundedCornerShape(tokens.assistantCardCornerRadius),
            color = tokens.cardWhite,
            tonalElevation = 2.dp,
            shadowElevation = tokens.bubbleElevation,
        ) {
            Box(modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.md)) {
                AssistantMessageBody(
                    content = content,
                    contentColor = scheme.onSurface,
                    assistantFontSize = assistantFontSize,
                    assistantFontFamily = assistantFontFamily,
                    assistantContent = assistantContent,
                    onContentChange = onAssistantContentChange,
                )
            }
        }
        return
    }

    val colors = if (geminiStyle && role == AiChatMessageRole.User && !isError) {
        com.example.testapp.uicommon.design.AiChatBubbleColors(
            container = tokens.userBubble,
            content = tokens.userBubbleContent,
        )
    } else {
        com.example.testapp.uicommon.design.AiChatBubbleColorPipeline.resolve(
            role = role,
            primary = scheme.primary,
            primaryContainer = scheme.primaryContainer,
            onPrimary = scheme.onPrimary,
            onPrimaryContainer = scheme.onPrimaryContainer,
            surfaceVariant = if (geminiStyle && role == AiChatMessageRole.User) {
                scheme.surfaceContainerHigh
            } else {
                scheme.surfaceVariant
            },
            onSurface = scheme.onSurface,
            isError = isError,
            errorContainer = scheme.errorContainer,
            onErrorContainer = scheme.onErrorContainer,
        )
    }
    val horizontalArrangement = when (role) {
        AiChatMessageRole.User -> Arrangement.End
        AiChatMessageRole.Assistant -> Arrangement.Start
    }
    val bubbleShape = when {
        geminiStyle && role == AiChatMessageRole.User ->
            RoundedCornerShape(tokens.userBubbleCornerRadius)
        role == AiChatMessageRole.User -> RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 4.dp,
            bottomEnd = 16.dp,
            bottomStart = 16.dp,
        )
        else -> RoundedCornerShape(
            topStart = 4.dp,
            topEnd = 16.dp,
            bottomEnd = 16.dp,
            bottomStart = 16.dp,
        )
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(maxWidthFraction),
            shape = bubbleShape,
            color = colors.container,
            tonalElevation = 2.dp,
            shadowElevation = tokens.bubbleElevation,
        ) {
            Box(modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm)) {
                when {
                    role == AiChatMessageRole.Assistant && assistantContent != null ->
                        assistantContent(content)
                    role == AiChatMessageRole.Assistant ->
                        AssistantMessageBody(
                            content = content,
                            contentColor = colors.content,
                            assistantFontSize = assistantFontSize,
                            assistantFontFamily = assistantFontFamily,
                            assistantContent = assistantContent,
                            onContentChange = onAssistantContentChange,
                        )
                    else ->
                        SelectionContainer {
                            Text(
                                text = content,
                                color = colors.content,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = assistantFontSize,
                                    fontFamily = assistantFontFamily,
                                ),
                            )
                        }
                }
            }
        }
    }
}

@Composable
private fun AssistantMessageBody(
    content: String,
    contentColor: Color,
    assistantFontSize: TextUnit,
    assistantFontFamily: FontFamily?,
    assistantContent: (@Composable (String) -> Unit)?,
    onContentChange: ((String) -> Unit)?,
) {
    when {
        assistantContent != null -> assistantContent(content)
        onContentChange != null ->
            SelectionContainer {
                BasicTextField(
                    value = content,
                    onValueChange = onContentChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(
                        color = contentColor,
                        fontSize = assistantFontSize,
                        fontFamily = assistantFontFamily,
                    ),
                    cursorBrush = SolidColor(contentColor),
                )
            }
        else ->
            SelectionContainer {
                RichText(
                    text = content,
                    color = contentColor,
                    fontSize = assistantFontSize,
                    fontFamily = assistantFontFamily,
                )
            }
    }
}
