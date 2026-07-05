package com.example.testapp.presentation.screen.practice.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.component.RichText
import com.example.testapp.uicommon.design.AnalysisSectionTone
import com.example.testapp.uicommon.design.AppSpacing
import com.example.testapp.uicommon.design.analysisSectionColors

@Composable
fun PracticeRichAnalysisSection(
    text: String?,
    tone: AnalysisSectionTone,
    label: String,
    fontSize: Float = 16f,
    lineHeight: Float = 1.3f,
    letterSpacing: Float = 0f,
    onDoubleTap: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null,
    onInteraction: (() -> Unit)? = null,
) {
    if (text.isNullOrBlank()) return

    val colors = analysisSectionColors(tone)
    var collapsed by remember(text) { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.xs)
            .background(colors.container)
            .padding(AppSpacing.sm)
            .pointerInput(text) {
                detectTapGestures(
                    onTap = {
                        onInteraction?.invoke()
                        collapsed = !collapsed
                    },
                    onDoubleTap = {
                        onInteraction?.invoke()
                        onDoubleTap?.invoke()
                    },
                    onLongPress = {
                        onInteraction?.invoke()
                        onLongPress?.invoke()
                    },
                )
            },
    ) {
        if (collapsed) {
            RichText(
                text = "$label$text",
                color = colors.content,
                fontSize = fontSize.sp,
                fontFamily = LocalFontFamily.current,
                lineSpacingMultiplier = lineHeight,
                letterSpacing = letterSpacing,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Text(
                text = label,
                color = colors.content,
                fontSize = fontSize.sp,
                fontFamily = LocalFontFamily.current,
                lineHeight = (fontSize * lineHeight).sp,
                letterSpacing = letterSpacing.sp,
                modifier = Modifier.fillMaxWidth(),
            )
            RichText(
                text = text,
                color = colors.content,
                fontSize = fontSize.sp,
                fontFamily = LocalFontFamily.current,
                lineSpacingMultiplier = lineHeight,
                letterSpacing = letterSpacing,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                IconButton(onClick = {
                    onInteraction?.invoke()
                    collapsed = true
                }) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowUp,
                        contentDescription = "折叠",
                        tint = colors.content,
                    )
                }
            }
        }
    }
}
