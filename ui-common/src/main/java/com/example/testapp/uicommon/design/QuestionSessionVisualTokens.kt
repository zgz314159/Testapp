package com.example.testapp.uicommon.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val questionSessionFloatingLight = Color(0xFFFFFCF7)

@Composable
fun questionSessionFloatingContainerColor(): Color =
    if (isSystemInDarkTheme()) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        questionSessionFloatingLight
    }
