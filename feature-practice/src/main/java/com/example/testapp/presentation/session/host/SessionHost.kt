package com.example.testapp.presentation.session.host

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.domain.session.QuestionSession
import com.example.testapp.domain.session.QuestionSessionKind
import kotlinx.coroutines.launch

/** 薄 SessionHost：enter / leave / 暴露 session 流（≤120 行） */
@Composable
fun SessionHost(
    kind: QuestionSessionKind,
    hostViewModel: QuestionSessionHostViewModel = hiltViewModel(),
    content: @Composable (QuestionSession) -> Unit,
) {
    val session by hostViewModel.session.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(kind) {
        hostViewModel.enter(kind)
    }

    DisposableEffect(kind) {
        onDispose {
            scope.launch { hostViewModel.leave() }
        }
    }

    val activeSession = session
    if (activeSession != null) {
        content(activeSession)
    }
}
