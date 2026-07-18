package com.example.testapp.presentation.screen.home.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.ui.unit.dp

/**
 * 首页题库列表缓存 / 组合策略。
 *
 * - 题库+文件夹总数 ≤ [HomeQuestionBankEagerComposeLimit]：非 Lazy 一次组合，
 *   滚动阶段零 item 创建/销毁（日志证明 CacheWindow 无法在冷启动填满窗口）。
 * - 更大列表：Lazy + [HomeQuestionBankCacheWindow] 像素窗口预取。
 * 仅改变组合策略，不改变卡片视觉。
 */
internal object HomeQuestionBankCachePolicy {
    /** 与当前真机根目录约 35 item 同量级；超过后回退 Lazy。 */
    const val EagerComposeLimit = 48

    /** 首帧先组合约一屏，避免 20+ 张卡同时组合造成主页空白。 */
    const val InitialPaintCount = 8

    fun shouldEagerCompose(fileCount: Int, folderCount: Int): Boolean =
        fileCount + folderCount <= EagerComposeLimit
}

@OptIn(ExperimentalFoundationApi::class)
internal val HomeQuestionBankCacheWindow = LazyLayoutCacheWindow(
    ahead = 4_000.dp,
    behind = 4_000.dp,
)
