package com.example.testapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.testapp.R
import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.presentation.screen.practice.PracticeScreen
import com.example.testapp.presentation.screen.settings.SettingsViewModel
import com.example.testapp.presentation.session.host.SessionHost
import com.example.testapp.presentation.session.practice.AbstractPracticeQuestionSession

@Composable
fun AdaptiveFadingPracticeRoute(
    quizId: String,
    settingsViewModel: SettingsViewModel,
    onQuizEnd: (score: Int, total: Int, unanswered: Int) -> Unit,
    onExitWithoutAnswer: () -> Unit,
    practiceNavCallbacks: QuestionSessionNavCallbacks,
) {
    val kind = remember(quizId) { QuestionSessionKind.AdaptiveFading(quizId) }
    SessionHost(kind = kind) { session ->
        val hosted = session as AbstractPracticeQuestionSession
        PracticeScreen(
            quizId = quizId,
            bindings = hosted.bindings,
            sessionHosted = true,
            persistentQuestionActionsEnabled = false,
            settingsViewModel = settingsViewModel,
            correctSoundResId = R.raw.correct,
            wrongSoundResId = R.raw.wrong,
            onQuizEnd = { score, total, unanswered, _, _ -> onQuizEnd(score, total, unanswered) },
            onExitWithoutAnswer = onExitWithoutAnswer,
            onViewDeepSeek = practiceNavCallbacks.onViewDeepSeek,
            onViewSpark = practiceNavCallbacks.onViewSpark,
            onViewBaidu = practiceNavCallbacks.onViewBaidu,
            onAskDeepSeek = practiceNavCallbacks.onAskDeepSeek,
            onAskSpark = practiceNavCallbacks.onAskSpark,
            onAskBaidu = practiceNavCallbacks.onAskBaidu,
            onViewExplanation = practiceNavCallbacks.onViewExplanation,
            onEditCorrectAnswer = practiceNavCallbacks.onEditCorrectAnswer,
            onEditNote = practiceNavCallbacks.onEditNote,
        )
    }
}
