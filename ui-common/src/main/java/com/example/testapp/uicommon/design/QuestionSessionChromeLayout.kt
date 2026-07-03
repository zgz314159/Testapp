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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * 顶栏 / 底栏锚定在屏幕上下缘（Box 层叠），中间 scroll 区填充。
 * 底栏不消费 IME 位移，键盘弹出时由 scroll 末尾 spacer 提供可滚空间。
 */
@Composable
fun QuestionSessionChromeLayout(
    scrollState: ScrollState,
    onScrollInProgress: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit,
    bottomBar: @Composable () -> Unit,
    scrollContent: @Composable ColumnScope.() -> Unit
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
                    bottom = insets.scrollBottomInset()
                ),
            content = scrollContent
        )
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .consumeWindowInsets(WindowInsets.ime),
            color = MaterialTheme.colorScheme.background
        ) {
            topBar()
        }
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .consumeWindowInsets(WindowInsets.ime),
            color = MaterialTheme.colorScheme.background
        ) {
            bottomBar()
        }
    }
}
