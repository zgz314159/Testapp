package com.example.testapp.presentation.session.host

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.domain.session.QuestionSession
import com.example.testapp.domain.session.QuestionSessionKind

/**
 * 薄 SessionHost：enter / 暴露 session 流（≤120 行）。
 *
 * 离开子路由（DeepSeek 全屏等）时 Compose 会 dispose 本 Host，但 NavBackStackEntry 上的
 * [QuestionSessionHostViewModel] 仍存活；不得在 onDispose 里 leave，否则返回后 re-enter 会重建会话并回到第 1 题。
 * 销毁仅在 ViewModel.onCleared（练习/考试路由真正出栈）时进行。
 */
@Composable
fun SessionHost(
    kind: QuestionSessionKind,
    hostViewModel: QuestionSessionHostViewModel = hiltViewModel(),
    content: @Composable (QuestionSession) -> Unit,
) {
    val session by hostViewModel.session.collectAsState()

    LaunchedEffect(kind) {
        hostViewModel.enter(kind)
    }

    val activeSession = session
    if (activeSession != null) {
        content(activeSession)
    }
}
