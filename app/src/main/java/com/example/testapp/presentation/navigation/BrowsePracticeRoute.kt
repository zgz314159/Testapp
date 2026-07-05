package com.example.testapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.testapp.core.session.route.BrowseSessionRoutePipeline
import com.example.testapp.presentation.screen.settings.SettingsViewModel
import com.example.testapp.presentation.session.browse.BrowseSession
import com.example.testapp.presentation.session.browse.BrowseSessionScreen
import com.example.testapp.presentation.session.host.SessionHost

@Composable
fun BrowsePracticeRoute(
    quizId: String,
    targetQuestionId: Int,
    settingsViewModel: SettingsViewModel,
    onExit: () -> Unit,
) {
    val kind =
        remember(quizId, targetQuestionId) {
            BrowseSessionRoutePipeline.browseKind(quizId, targetQuestionId)
        }
    val fontSize by settingsViewModel.fontSize.collectAsState()

    SessionHost(kind = kind) { session ->
        BrowseSessionScreen(
            session = session as BrowseSession,
            questionFontSize = fontSize,
            lineSpacing = 1.3f,
            letterSpacing = 0f,
            onExit = onExit,
        )
    }
}
