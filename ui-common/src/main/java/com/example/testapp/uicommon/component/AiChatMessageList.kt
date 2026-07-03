package com.example.testapp.uicommon.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import com.example.testapp.uicommon.design.AiChatScrollTargetPipeline
import com.example.testapp.uicommon.design.AppSpacing
import com.example.testapp.uicommon.model.AiChatMessage
import com.example.testapp.uicommon.model.AiChatMessageRole

@Composable
fun AiChatMessageList(
    messages: List<AiChatMessage>,
    isTyping: Boolean,
    typingLabel: String,
    errorMessage: String?,
    modifier: Modifier = Modifier,
    assistantFontSize: TextUnit = LocalFontSize.current,
    assistantFontFamily: FontFamily? = LocalFontFamily.current,
    assistantTextToolbar: androidx.compose.ui.platform.TextToolbar? = null,
    assistantContent: (@Composable (String) -> Unit)? = null,
    geminiStyle: Boolean = true
) {
    val listState = rememberLazyListState()
    val showError = !errorMessage.isNullOrBlank()
    LaunchedEffect(messages.size, isTyping, errorMessage) {
        val target = AiChatScrollTargetPipeline.lastIndex(
            messageCount = messages.size,
            showTyping = isTyping,
            showError = showError
        )
        if (target >= 0) {
            listState.animateScrollToItem(target)
        }
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(
            start = AppSpacing.md,
            end = AppSpacing.md,
            top = AppSpacing.sm,
            bottom = AppSpacing.md
        ),
        verticalArrangement = Arrangement.spacedBy(
            if (geminiStyle) AppSpacing.md else AppSpacing.sm
        )
    ) {
        itemsIndexed(messages, key = { index, item -> "$index-${item.role}-${item.content.hashCode()}" }) { _, message ->
            val bubbleModifier = Modifier.fillMaxWidth()
            if (message.role == AiChatMessageRole.Assistant && assistantTextToolbar != null) {
                CompositionLocalProvider(LocalTextToolbar provides assistantTextToolbar) {
                    AiChatBubble(
                        role = message.role,
                        content = message.content,
                        modifier = bubbleModifier,
                        assistantFontSize = assistantFontSize,
                        assistantFontFamily = assistantFontFamily,
                        assistantContent = assistantContent,
                        geminiStyle = geminiStyle
                    )
                }
            } else {
                AiChatBubble(
                    role = message.role,
                    content = message.content,
                    modifier = bubbleModifier,
                    assistantFontSize = assistantFontSize,
                    assistantFontFamily = assistantFontFamily,
                    assistantContent = assistantContent,
                    geminiStyle = geminiStyle
                )
            }
        }
        if (isTyping) {
            item(key = "typing") {
                AiChatTypingBubble(label = typingLabel, geminiStyle = geminiStyle)
            }
        }
        if (showError) {
            item(key = "error") {
                AiChatBubble(
                    role = AiChatMessageRole.Assistant,
                    content = errorMessage.orEmpty(),
                    isError = true,
                    assistantFontSize = assistantFontSize,
                    assistantFontFamily = assistantFontFamily,
                    geminiStyle = geminiStyle
                )
            }
        }
    }
}
