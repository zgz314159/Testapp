package com.example.testapp.uicommon.design

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun ReadingCollapsibleSection(
    containerColor: Color,
    contentColor: Color,
    resetKey: Any?,
    modifier: Modifier = Modifier,
    onInteraction: () -> Unit = {},
    onDoubleTap: (() -> Unit)? = null,
    collapsedContent: @Composable () -> Unit,
    expandedContent: @Composable () -> Unit
) {
    var collapsed by remember(resetKey) { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.xs)
            .background(containerColor)
            .padding(AppSpacing.sm)
            .pointerInput(resetKey, onDoubleTap) {
                detectTapGestures(
                    onTap = {
                        onInteraction()
                        collapsed = !collapsed
                    },
                    onDoubleTap = onDoubleTap?.let { action ->
                        {
                            onInteraction()
                            action()
                        }
                    },
                )
            }
    ) {
        if (collapsed) {
            collapsedContent()
        } else {
            expandedContent()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(onClick = {
                    onInteraction()
                    collapsed = true
                }) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowUp,
                        contentDescription = "折叠",
                        tint = contentColor
                    )
                }
            }
        }
    }
}
