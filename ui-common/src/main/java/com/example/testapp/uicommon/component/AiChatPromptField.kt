package com.example.testapp.uicommon.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.example.testapp.uicommon.design.AiChatPromptDesignTokens

@Composable
fun AiChatPromptField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 6,
    enabled: Boolean = true
) {
    val tokens = AiChatPromptDesignTokens
    val shape = RoundedCornerShape(tokens.fieldCornerRadius)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = tokens.fieldMinHeight),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = tokens.fieldInnerHorizontalPadding,
                    vertical = tokens.fieldInnerVerticalPadding
                ),
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = MaterialTheme.typography.bodyLarge.fontSize
            ),
            maxLines = maxLines,
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    inner()
                }
            }
        )
    }
}
