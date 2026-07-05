package com.example.testapp.uicommon.design

import androidx.compose.ui.unit.Dp

/** 无状态：Chrome 布局 scroll 区 top/bottom 内边距。 */
object QuestionSessionChromeInsetsPipeline {

    fun scrollTopInset(): Dp = PracticeExamTopBarMetrics.barHeight

    fun scrollBottomInset(): Dp = QuestionSessionBottomNavMetrics.barHeight
}
