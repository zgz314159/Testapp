package com.example.testapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.presentation.screen.magnetic.MagneticRebuildScreen
import com.example.testapp.presentation.session.host.SessionHost
import com.example.testapp.presentation.session.magnetic.MagneticRebuildSession

@Composable
fun MagneticRebuildRoute(
    quizId: String,
    onBack: () -> Unit,
) {
    val kind = remember(quizId) { QuestionSessionKind.MagneticRebuild(quizId) }
    SessionHost(kind = kind) { session ->
        MagneticRebuildScreen(
            session = session as MagneticRebuildSession,
            onBack = onBack,
        )
    }
}
