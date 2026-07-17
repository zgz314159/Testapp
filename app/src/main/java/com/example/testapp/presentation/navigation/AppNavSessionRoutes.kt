package com.example.testapp.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.testapp.core.common.parseExamReviewTarget
import com.example.testapp.core.common.parsePracticeReviewTarget
import com.example.testapp.core.session.route.BrowseSessionRoutePipeline
import com.example.testapp.core.util.safeDecode
import com.example.testapp.presentation.screen.settings.SettingsViewModel

fun NavGraphBuilder.registerPracticeSessionRoutes(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel,
    sessionNavCallbacks: QuestionSessionNavCallbacks,
) {
    composable(
        route = "adaptive/{quizId}",
        arguments = listOf(navArgument("quizId") { type = NavType.StringType }),
    ) { backStackEntry ->
        val encoded = backStackEntry.arguments?.getString("quizId") ?: "default"
        val quizId = com.example.testapp.util.safeDecode(encoded)
        AdaptiveFadingPracticeRoute(
            quizId = quizId,
            settingsViewModel = settingsViewModel,
            onQuizEnd = { score, total, unanswered ->
                navController.navToResult(
                    prefix = "adaptive",
                    quizId = quizId,
                    score = score,
                    total = total,
                    unanswered = unanswered,
                    cumulativeCorrect = null,
                    cumulativeAnswered = null,
                )
            },
            onExitWithoutAnswer = { navController.popBackStack() },
            practiceNavCallbacks = sessionNavCallbacks,
        )
    }

    composable(
        route = "question/{quizId}?targetQuestionId={targetQuestionId}",
        arguments =
            listOf(
                navArgument("quizId") { type = NavType.StringType },
                navArgument("targetQuestionId") {
                    type = NavType.IntType
                    defaultValue = -1
                },
            ),
    ) { backStackEntry ->
        val encoded = backStackEntry.arguments?.getString("quizId") ?: "default"
        val quizId = com.example.testapp.util.safeDecode(encoded)
        val targetQuestionId = backStackEntry.arguments?.getInt("targetQuestionId") ?: -1
        if (BrowseSessionRoutePipeline.shouldUseBrowseSession(targetQuestionId.takeIf { it >= 0 })) {
            BrowsePracticeRoute(
                quizId = quizId,
                targetQuestionId = targetQuestionId,
                settingsViewModel = settingsViewModel,
                onExit = { navController.popBackStack() },
            )
        } else {
            PracticePracticeRoute(
                quizId = quizId,
                settingsViewModel = settingsViewModel,
                onQuizEnd = { score, total, unanswered, cumulativeCorrect, cumulativeAnswered, sessionProgressId ->
                    navController.navToResult(
                        prefix = "practice",
                        quizId = quizId,
                        score = score,
                        total = total,
                        unanswered = unanswered,
                        cumulativeCorrect = cumulativeCorrect,
                        cumulativeAnswered = cumulativeAnswered,
                        sessionProgressId = sessionProgressId,
                    )
                },
                onExitWithoutAnswer = { navController.popBackStack() },
                practiceNavCallbacks = sessionNavCallbacks,
            )
        }
    }

    composable(
        route = "question_edit/{quizId}/{questionId}",
        arguments =
            listOf(
                navArgument("quizId") { type = NavType.StringType },
                navArgument("questionId") { type = NavType.IntType },
            ),
    ) { backStackEntry ->
        val encoded = backStackEntry.arguments?.getString("quizId").orEmpty()
        val quizId = com.example.testapp.util.safeDecode(encoded)
        val questionId = backStackEntry.arguments?.getInt("questionId") ?: 0
        QuestionEditPracticeRoute(
            quizId = quizId,
            questionId = questionId,
            onDismiss = { navController.popBackStack() },
        )
    }

    composable(
        "practice_review/{progressId}",
        arguments = listOf(navArgument("progressId") { type = NavType.StringType }),
    ) { backStackEntry ->
        val progressId = safeDecode(backStackEntry.arguments?.getString("progressId").orEmpty())
        val target = parsePracticeReviewTarget(progressId)
        ReviewPracticeRoute(
            progressId = progressId,
            quizId = target.quizFileName,
            isWrongBookMode = target.isWrongBookMode,
            wrongBookFileName = target.quizFileName.takeIf { target.isWrongBookMode },
            isFavoriteMode = target.isFavoriteMode,
            favoriteFileName = target.quizFileName.takeIf { target.isFavoriteMode },
            settingsViewModel = settingsViewModel,
            onReviewBack = { navController.popBackStack() },
            onExitWithoutAnswer = { navController.popBackStack() },
            practiceNavCallbacks = sessionNavCallbacks,
        )
    }

    composable(
        "practice_wrongbook/{fileName}",
        arguments = listOf(navArgument("fileName") { type = NavType.StringType }),
    ) { backStackEntry ->
        val encoded = backStackEntry.arguments?.getString("fileName") ?: ""
        val name = com.example.testapp.util.safeDecode(encoded)
        PracticePracticeRoute(
            quizId = name,
            wrongBookFileName = name,
            settingsViewModel = settingsViewModel,
            onQuizEnd = { score, total, unanswered, cumulativeCorrect, cumulativeAnswered, sessionProgressId ->
                navController.navToResult(
                    prefix = "practice",
                    quizId = name,
                    score = score,
                    total = total,
                    unanswered = unanswered,
                    cumulativeCorrect = cumulativeCorrect,
                    cumulativeAnswered = cumulativeAnswered,
                    sessionProgressId = sessionProgressId,
                    popUpTo = "wrongbook",
                )
            },
            onExitWithoutAnswer = { navController.popBackStack() },
            practiceNavCallbacks = sessionNavCallbacks,
        )
    }

    composable(
        "practice_favorite/{fileName}",
        arguments = listOf(navArgument("fileName") { type = NavType.StringType }),
    ) { backStackEntry ->
        val encoded = backStackEntry.arguments?.getString("fileName") ?: ""
        val name = com.example.testapp.util.safeDecode(encoded)
        PracticePracticeRoute(
            quizId = name,
            favoriteFileName = name,
            settingsViewModel = settingsViewModel,
            onQuizEnd = { score, total, unanswered, cumulativeCorrect, cumulativeAnswered, sessionProgressId ->
                navController.navToResult(
                    prefix = "practice",
                    quizId = name,
                    score = score,
                    total = total,
                    unanswered = unanswered,
                    cumulativeCorrect = cumulativeCorrect,
                    cumulativeAnswered = cumulativeAnswered,
                    sessionProgressId = sessionProgressId,
                    popUpTo = "favorite",
                )
            },
            onExitWithoutAnswer = { navController.popBackStack() },
            practiceNavCallbacks = sessionNavCallbacks,
        )
    }
}

fun NavGraphBuilder.registerExamSessionRoutes(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel,
    sessionNavCallbacks: QuestionSessionNavCallbacks,
) {
    composable(
        "exam/{quizId}",
        arguments = listOf(navArgument("quizId") { type = NavType.StringType }),
    ) { backStackEntry ->
        val encoded = backStackEntry.arguments?.getString("quizId") ?: "default"
        val quizId = com.example.testapp.util.safeDecode(encoded)
        ExamExamRoute(
            quizId = quizId,
            settingsViewModel = settingsViewModel,
            onExamEnd = { score, total, unanswered, cc, ca, ec, sessionProgressId ->
                navController.navToResult(
                    prefix = "exam",
                    quizId = quizId,
                    score = score,
                    total = total,
                    unanswered = unanswered,
                    cumulativeCorrect = cc,
                    cumulativeAnswered = ca,
                    cumulativeExamCount = ec,
                    sessionProgressId = sessionProgressId,
                )
            },
            onExitWithoutAnswer = { navController.popBackStack() },
            examNavCallbacks = sessionNavCallbacks,
        )
    }

    composable(
        "exam_review/{progressId}",
        arguments = listOf(navArgument("progressId") { type = NavType.StringType }),
    ) { backStackEntry ->
        val progressId = safeDecode(backStackEntry.arguments?.getString("progressId").orEmpty())
        val target = parseExamReviewTarget(progressId)
        ExamExamRoute(
            quizId = target.quizFileName,
            wrongBookFileName = target.quizFileName.takeIf { target.isWrongBookMode },
            favoriteFileName = target.quizFileName.takeIf { target.isFavoriteMode },
            reviewProgressId = progressId,
            isReviewMode = true,
            settingsViewModel = settingsViewModel,
            onReviewBack = { navController.popBackStack() },
            onExamEnd = { _, _, _, _, _, _, _ -> },
            onExitWithoutAnswer = { navController.popBackStack() },
            examNavCallbacks = sessionNavCallbacks,
        )
    }

    composable(
        "exam_wrongbook/{fileName}",
        arguments = listOf(navArgument("fileName") { type = NavType.StringType }),
    ) { backStackEntry ->
        val encoded = backStackEntry.arguments?.getString("fileName") ?: ""
        val name = com.example.testapp.util.safeDecode(encoded)
        ExamExamRoute(
            quizId = name,
            wrongBookFileName = name,
            settingsViewModel = settingsViewModel,
            onExamEnd = { score, total, unanswered, cc, ca, ec, sessionProgressId ->
                navController.navToResult(
                    prefix = "exam",
                    quizId = name,
                    score = score,
                    total = total,
                    unanswered = unanswered,
                    cumulativeCorrect = cc,
                    cumulativeAnswered = ca,
                    cumulativeExamCount = ec,
                    sessionProgressId = sessionProgressId,
                    popUpTo = "wrongbook",
                )
            },
            onExitWithoutAnswer = { navController.popBackStack() },
            examNavCallbacks = sessionNavCallbacks,
        )
    }

    composable(
        "exam_favorite/{fileName}",
        arguments = listOf(navArgument("fileName") { type = NavType.StringType }),
    ) { backStackEntry ->
        val encoded = backStackEntry.arguments?.getString("fileName") ?: ""
        val name = com.example.testapp.util.safeDecode(encoded)
        ExamExamRoute(
            quizId = name,
            favoriteFileName = name,
            settingsViewModel = settingsViewModel,
            onExamEnd = { score, total, unanswered, cc, ca, ec, sessionProgressId ->
                navController.navToResult(
                    prefix = "exam",
                    quizId = name,
                    score = score,
                    total = total,
                    unanswered = unanswered,
                    cumulativeCorrect = cc,
                    cumulativeAnswered = ca,
                    cumulativeExamCount = ec,
                    sessionProgressId = sessionProgressId,
                    popUpTo = "favorite",
                )
            },
            onExitWithoutAnswer = { navController.popBackStack() },
            examNavCallbacks = sessionNavCallbacks,
        )
    }
}
