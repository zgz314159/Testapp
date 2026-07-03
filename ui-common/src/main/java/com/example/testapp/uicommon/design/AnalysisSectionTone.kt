package com.example.testapp.uicommon.design

import androidx.compose.ui.graphics.Color

enum class AnalysisSectionTone {
    Explanation,
    Note,
    DeepSeek,
    Spark,
    Baidu
}

data class AnalysisSectionColors(
    val container: Color,
    val content: Color
)
