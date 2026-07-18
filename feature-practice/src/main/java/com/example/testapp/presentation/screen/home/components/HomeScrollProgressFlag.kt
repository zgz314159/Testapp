package com.example.testapp.presentation.screen.home.components

/**
 * 滚动中标志：仅供手势门闩读取，禁止在 composition 中订阅。
 * 见 Android 文档 defer state reads — 在 composition 读 isScrollInProgress
 * 会在每次 scroll_start 重组整棵列表树。
 */
internal class HomeScrollProgressFlag {
    @Volatile
    var value: Boolean = false
}
