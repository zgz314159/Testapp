package com.example.testapp.uicommon.design

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.ui.graphics.vector.ImageVector

fun iconForPracticeExamAiMenuAction(action: PracticeExamAiMenuAction): ImageVector =
    when (action) {
        PracticeExamAiMenuAction.DeepSeek -> Icons.Filled.Psychology
        PracticeExamAiMenuAction.SparkAsk -> Icons.Filled.Forum
    }
