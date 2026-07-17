package com.example.testapp.uicommon.design

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * 顶栏 / 底栏锚定在屏幕上下缘。外层使用不裁切的 Box，确保软阴影能显示在护眼背景上。
 * 底栏不消费 IME 位移，键盘弹出时由 scroll 末尾 spacer 提供可滚空间。
 */
@Composable
fun QuestionSessionChromeLayout(
    scrollState: ScrollState,
    onScrollInProgress: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit,
    bottomBar: @Composable () -> Unit,
    scrollContent: @Composable ColumnScope.() -> Unit,
) {
    val insets = QuestionSessionChromeInsetsPipeline
    Box(modifier = modifier.fillMaxSize()) {
        QuestionSessionBodyScroll(
            scrollState = scrollState,
            onScrollInProgress = onScrollInProgress,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = insets.scrollTopInset(),
                    bottom = insets.scrollBottomInset(),
                ),
            content = scrollContent,
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .consumeWindowInsets(WindowInsets.ime),
        ) {
            topBar()
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .consumeWindowInsets(WindowInsets.ime),
        ) {
            bottomBar()
        }
    }
}
