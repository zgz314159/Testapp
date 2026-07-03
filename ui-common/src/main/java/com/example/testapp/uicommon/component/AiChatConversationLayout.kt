package com.example.testapp.uicommon.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import com.example.testapp.uicommon.model.AiChatMessage

@Composable
fun AiChatConversationLayout(
    messages: List<AiChatMessage>,
    isTyping: Boolean,
    typingLabel: String,
    errorMessage: String?,
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    sendEnabled: Boolean,
    sendContentDescription: String,
    inputPlaceholder: String,
    modifier: Modifier = Modifier,
    assistantFontSize: TextUnit = LocalFontSize.current,
    assistantFontFamily: FontFamily? = LocalFontFamily.current,
    assistantTextToolbar: androidx.compose.ui.platform.TextToolbar? = null,
    assistantContent: (@Composable (String) -> Unit)? = null,
    geminiStyle: Boolean = true
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AiChatPromptSheet(
                value = inputText,
                onValueChange = onInputChange,
                onSend = onSend,
                sendEnabled = sendEnabled,
                sendContentDescription = sendContentDescription,
                placeholder = inputPlaceholder
            )
        }
    ) { innerPadding ->
        AiChatMessageList(
            messages = messages,
            isTyping = isTyping,
            typingLabel = typingLabel,
            errorMessage = errorMessage,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            assistantFontSize = assistantFontSize,
            assistantFontFamily = assistantFontFamily,
            assistantTextToolbar = assistantTextToolbar,
            assistantContent = assistantContent,
            geminiStyle = geminiStyle
        )
    }
}
