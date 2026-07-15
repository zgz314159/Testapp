package com.example.testapp.presentation.screen.questionbank

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import com.example.testapp.presentation.screen.home.design.HomeDesignTokens

@Composable
fun rememberHighlightedQuestionBankText(
    text: String,
    query: String,
): AnnotatedString {
    return buildHighlightedText(
        text = text,
        query = query,
        highlightColor = HomeDesignTokens.primary,
        highlightBackground = HomeDesignTokens.primaryContainer.copy(alpha = 0.65f),
    )
}

fun buildHighlightedText(
    text: String,
    query: String,
    highlightColor: Color,
    highlightBackground: Color,
): AnnotatedString {
    val normalizedQuery = query.trim()
    if (text.isEmpty() || normalizedQuery.isEmpty()) {
        return AnnotatedString(text)
    }

    val lowerText = text.lowercase()
    val lowerQuery = normalizedQuery.lowercase()
    return buildAnnotatedString {
        var startIndex = 0
        while (startIndex < text.length) {
            val matchIndex = lowerText.indexOf(lowerQuery, startIndex)
            if (matchIndex < 0) {
                append(text.substring(startIndex))
                break
            }
            if (matchIndex > startIndex) {
                append(text.substring(startIndex, matchIndex))
            }
            val matchEnd = matchIndex + normalizedQuery.length
            pushStyle(
                SpanStyle(
                    color = highlightColor,
                    background = highlightBackground,
                    fontWeight = FontWeight.Bold,
                ),
            )
            append(text.substring(matchIndex, matchEnd.coerceAtMost(text.length)))
            pop()
            startIndex = matchEnd
        }
    }
}
