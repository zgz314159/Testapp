package com.example.testapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.presentation.screen.home.HomeScreen
import com.example.testapp.presentation.screen.settings.SettingsViewModel

/** `:app` 薄路由 — 注入仅留 app 的 VM 绑定，UI 在 `:feature-practice`。 */
@Composable
fun HomeRoute(
    onStartQuiz: (quizId: String) -> Unit = {},
    onStartAdaptive: (quizId: String) -> Unit = {},
    onBrowseQuestion: (fileName: String, questionId: Int) -> Unit = { fileName, _ -> onStartQuiz(fileName) },
    onEditQuestion: (fileName: String, questionId: Int) -> Unit = { _, _ -> },
    onStartExam: (quizId: String) -> Unit = {},
    onSettings: () -> Unit = {},
    onViewQuestionDetail: (quizId: String) -> Unit = {},
    onWrongBook: (fileName: String) -> Unit = {},
    onFavoriteBook: (fileName: String) -> Unit = {},
    onHistory: () -> Unit = {},
    settingsViewModel: SettingsViewModel,
) {
    HomeScreen(
        viewModel = hiltViewModel(),
        folderViewModel = hiltViewModel(),
        dragViewModel = hiltViewModel(),
        drawerViewModel = hiltViewModel(),
        settingsViewModel = settingsViewModel,
        onStartQuiz = onStartQuiz,
        onStartAdaptive = onStartAdaptive,
        onBrowseQuestion = onBrowseQuestion,
        onEditQuestion = onEditQuestion,
        onStartExam = onStartExam,
        onSettings = onSettings,
        onViewQuestionDetail = onViewQuestionDetail,
        onWrongBook = onWrongBook,
        onFavoriteBook = onFavoriteBook,
        onHistory = onHistory,
    )
}
