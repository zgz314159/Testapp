package com.example.testapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.presentation.screen.exam.ExamScreen
import com.example.testapp.presentation.screen.settings.SettingsViewModel
import com.example.testapp.presentation.session.exam.AbstractExamQuestionSession
import com.example.testapp.presentation.session.host.SessionHost

@Composable
fun ExamExamRoute(
    quizId: String,
    wrongBookFileName: String? = null,
    favoriteFileName: String? = null,
    reviewProgressId: String? = null,
    isReviewMode: Boolean = false,
    settingsViewModel: SettingsViewModel,
    onReviewBack: () -> Unit = {},
    onExamEnd: (Int, Int, Int, Int?, Int?, Int?, String) -> Unit,
    onExitWithoutAnswer: () -> Unit,
    examNavCallbacks: QuestionSessionNavCallbacks,
) {
    val kind =
        remember(quizId, wrongBookFileName, favoriteFileName, reviewProgressId) {
            QuestionSessionKind.Exam(
                quizId = quizId,
                wrongBookFileName = wrongBookFileName,
                favoriteFileName = favoriteFileName,
                reviewProgressId = reviewProgressId,
            )
        }
    SessionHost(kind = kind) { session ->
        val hosted = session as AbstractExamQuestionSession
        val (isQuestionFavorite, onToggleQuestionFavorite) = rememberExamFavoriteBindings()
        ExamScreen(
            quizId = quizId,
            isWrongBookMode = wrongBookFileName != null,
            wrongBookFileName = wrongBookFileName,
            isFavoriteMode = favoriteFileName != null,
            favoriteFileName = favoriteFileName,
            isReviewMode = isReviewMode,
            reviewProgressId = reviewProgressId,
            onReviewBack = onReviewBack,
            bindings = hosted.bindings,
            sessionHosted = true,
            settingsViewModel = settingsViewModel,
            isQuestionFavorite = isQuestionFavorite,
            onToggleQuestionFavorite = onToggleQuestionFavorite,
            onExamEnd = { score, total, unanswered, cc, ca, ec ->
                onExamEnd(score, total, unanswered, cc, ca, ec, hosted.bindings.currentProgressId)
            },
            onExitWithoutAnswer = onExitWithoutAnswer,
            onViewDeepSeek = examNavCallbacks.onViewDeepSeek,
            onViewSpark = examNavCallbacks.onViewSpark,
            onViewBaidu = examNavCallbacks.onViewBaidu,
            onAskDeepSeek = examNavCallbacks.onAskDeepSeek,
            onAskSpark = examNavCallbacks.onAskSpark,
            onAskBaidu = examNavCallbacks.onAskBaidu,
            onViewExplanation = examNavCallbacks.onViewExplanation,
            onEditCorrectAnswer = examNavCallbacks.onEditCorrectAnswer,
            onEditNote = examNavCallbacks.onEditNote,
        )
    }
}
