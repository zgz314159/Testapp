package com.example.testapp.presentation.screen.library

import androidx.compose.runtime.Composable
import com.example.testapp.presentation.screen.home.HomeStartQuizSheet

/** 错题库/收藏库：与主页同款开始练习/考试 sheet（不含自适应渐隐）。 */
@Composable
fun LibraryStartQuizSheet(
    visible: Boolean,
    pendingFileName: String,
    hasProgress: Boolean,
    onDismiss: () -> Unit,
    onStartPractice: (String) -> Unit,
    onStartExam: (String) -> Unit,
    onRestart: (String) -> Unit,
) {
    HomeStartQuizSheet(
        visible = visible,
        pendingFileName = pendingFileName,
        hasProgress = hasProgress,
        onDismiss = onDismiss,
        onStartQuiz = { name ->
            onDismiss()
            onStartPractice(name)
        },
        onStartAdaptive = { onDismiss() },
        onStartExam = { name ->
            onDismiss()
            onStartExam(name)
        },
        onRestart = onRestart,
        showAdaptiveOption = false,
    )
}
