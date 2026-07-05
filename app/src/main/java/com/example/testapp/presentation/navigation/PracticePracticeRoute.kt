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
fun PracticePracticeRoute(
    quizId: String,
    wrongBookFileName: String? = null,
    favoriteFileName: String? = null,
    settingsViewModel: SettingsViewModel,
    onQuizEnd: (Int, Int, Int, Int?, Int?, String) -> Unit,
    onExitWithoutAnswer: () -> Unit,
    practiceNavCallbacks: QuestionSessionNavCallbacks,
) {
    val kind =
        remember(quizId, wrongBookFileName, favoriteFileName) {
            QuestionSessionKind.Practice(
                quizId = quizId,
                wrongBookFileName = wrongBookFileName,
                favoriteFileName = favoriteFileName,
            )
        }
    SessionHost(kind = kind) { session ->
        val hosted = session as AbstractPracticeQuestionSession
        PracticeScreen(
            quizId = quizId,
            isWrongBookMode = wrongBookFileName != null,
            wrongBookFileName = wrongBookFileName,
            isFavoriteMode = favoriteFileName != null,
            favoriteFileName = favoriteFileName,
            bindings = hosted.bindings,
            sessionHosted = true,
            settingsViewModel = settingsViewModel,
            correctSoundResId = R.raw.correct,
            wrongSoundResId = R.raw.wrong,
            onQuizEnd = { score, total, unanswered, cumulativeCorrect, cumulativeAnswered ->
                onQuizEnd(
                    score,
                    total,
                    unanswered,
                    cumulativeCorrect,
                    cumulativeAnswered,
                    hosted.bindings.currentProgressId,
                )
            },
            onExitWithoutAnswer = onExitWithoutAnswer,
            onViewDeepSeek = practiceNavCallbacks.onViewDeepSeek,
            onViewSpark = practiceNavCallbacks.onViewSpark,
            onViewBaidu = practiceNavCallbacks.onViewBaidu,
            onAskDeepSeek = practiceNavCallbacks.onAskDeepSeek,
            onAskSpark = practiceNavCallbacks.onAskSpark,
            onAskBaidu = practiceNavCallbacks.onAskBaidu,
            onViewExplanation = practiceNavCallbacks.onViewExplanation,
            onEditNote = practiceNavCallbacks.onEditNote,
        )
    }
}

@Composable
fun ReviewPracticeRoute(
    progressId: String,
    quizId: String,
    isWrongBookMode: Boolean,
    wrongBookFileName: String?,
    isFavoriteMode: Boolean,
    favoriteFileName: String?,
    settingsViewModel: SettingsViewModel,
    onReviewBack: () -> Unit,
    onExitWithoutAnswer: () -> Unit,
    practiceNavCallbacks: QuestionSessionNavCallbacks,
) {
    val kind =
        remember(progressId) {
            QuestionSessionKind.Review(
                progressId = progressId,
                wrongBookFileName = wrongBookFileName,
                favoriteFileName = favoriteFileName,
            )
        }
    SessionHost(kind = kind) { session ->
        val hosted = session as AbstractPracticeQuestionSession
        PracticeScreen(
            quizId = quizId,
            isWrongBookMode = isWrongBookMode,
            wrongBookFileName = wrongBookFileName,
            isFavoriteMode = isFavoriteMode,
            favoriteFileName = favoriteFileName,
            isReviewMode = true,
            reviewProgressId = progressId,
            onReviewBack = onReviewBack,
            bindings = hosted.bindings,
            sessionHosted = true,
            settingsViewModel = settingsViewModel,
            correctSoundResId = R.raw.correct,
            wrongSoundResId = R.raw.wrong,
            onExitWithoutAnswer = onExitWithoutAnswer,
            onViewDeepSeek = practiceNavCallbacks.onViewDeepSeek,
            onViewSpark = practiceNavCallbacks.onViewSpark,
            onViewBaidu = practiceNavCallbacks.onViewBaidu,
            onAskDeepSeek = practiceNavCallbacks.onAskDeepSeek,
            onAskSpark = practiceNavCallbacks.onAskSpark,
            onAskBaidu = practiceNavCallbacks.onAskBaidu,
            onViewExplanation = practiceNavCallbacks.onViewExplanation,
            onEditNote = practiceNavCallbacks.onEditNote,
        )
    }
}
