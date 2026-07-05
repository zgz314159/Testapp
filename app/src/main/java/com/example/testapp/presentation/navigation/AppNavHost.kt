package com.example.testapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.testapp.core.session.route.BrowseSessionRoutePipeline
import com.example.testapp.core.session.route.QuestionEditSessionRoutePipeline
import com.example.testapp.core.util.safeDecode
import com.example.testapp.presentation.navigation.ExplanationRoute
import com.example.testapp.presentation.navigation.HomeRoute
import com.example.testapp.presentation.screen.settings.FillSettingsScreen
import com.example.testapp.presentation.screen.settings.SettingsScreen

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    settingsViewModel: com.example.testapp.presentation.screen.settings.SettingsViewModel,
) {
    val sessionNavCallbacks = navController.rememberQuestionSessionNavCallbacks()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeRoute(
                onStartQuiz = { quizId ->
                    val encoded = java.net.URLEncoder.encode(quizId, "UTF-8")
                    navController.navigate("question/$encoded")
                },
                onBrowseQuestion = { fileName, questionId ->
                    val encoded = java.net.URLEncoder.encode(fileName, "UTF-8")
                    navController.navigate(
                        BrowseSessionRoutePipeline
                            .practiceQuestionRoute(encoded, questionId),
                    )
                },
                onEditQuestion = { fileName, questionId ->
                    val encoded = java.net.URLEncoder.encode(fileName, "UTF-8")
                    navController.navigate(
                        QuestionEditSessionRoutePipeline.route(encoded, questionId),
                    )
                },
                onStartExam = { quizId ->
                    val encoded = java.net.URLEncoder.encode(quizId, "UTF-8")
                    navController.navigate("exam/$encoded")
                },
                onStartWrongBookQuiz = { name ->
                    val encoded = java.net.URLEncoder.encode(name, "UTF-8")
                    navController.navigate("practice_wrongbook/$encoded")
                },
                onStartWrongBookExam = { name ->
                    val encoded = java.net.URLEncoder.encode(name, "UTF-8")
                    navController.navigate("exam_wrongbook/$encoded")
                },
                onStartFavoriteQuiz = { name ->
                    val encoded = java.net.URLEncoder.encode(name, "UTF-8")
                    navController.navigate("practice_favorite/$encoded")
                },
                onStartFavoriteExam = { name ->
                    val encoded = java.net.URLEncoder.encode(name, "UTF-8")
                    navController.navigate("exam_favorite/$encoded")
                },
                onSettings = { navController.navigateFromHome("settings") },
                onViewQuestionDetail = { quizId ->
                    val encoded = java.net.URLEncoder.encode(quizId, "UTF-8")
                    navController.navigate("question/$encoded?targetQuestionId=0")
                },
                onWrongBook = { fileName ->
                    if (fileName.isBlank()) {
                        navController.navigateFromHome("wrongbook")
                    } else {
                        val encoded = java.net.URLEncoder.encode(fileName, "UTF-8")
                        navController.navigateFromHome("wrongbook/$encoded")
                    }
                },
                onFavoriteBook = { fileName ->
                    if (fileName.isBlank()) {
                        navController.navigateFromHome("favorite")
                    } else {
                        val encoded = java.net.URLEncoder.encode(fileName, "UTF-8")
                        navController.navigateFromHome("favorite/$encoded")
                    }
                },
                onViewResult = { fileName ->
                    val encoded = java.net.URLEncoder.encode(fileName, "UTF-8")
                    navController.navigate("result/0/0/0/$encoded")
                },
                onHistory = { navController.navigateFromHome("history") },
                settingsViewModel = settingsViewModel,
            )
        }

        registerPracticeSessionRoutes(navController, settingsViewModel, sessionNavCallbacks)
        registerExamSessionRoutes(navController, settingsViewModel, sessionNavCallbacks)

        composable(
            "explanation/{text}",
            arguments = listOf(navArgument("text") { type = NavType.StringType }),
        ) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("text") ?: ""
            val text = com.example.testapp.util.safeDecode(encoded)
            ExplanationRoute(
                text = text,
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = "result/{score}/{total}/{unanswered}/{quizId}?cumulativeCorrect={cumulativeCorrect}&cumulativeAnswered={cumulativeAnswered}&cumulativeExamCount={cumulativeExamCount}&sessionProgressId={sessionProgressId}",
            arguments =
                listOf(
                    navArgument("quizId") { type = NavType.StringType },
                    navArgument("cumulativeCorrect") { type = NavType.IntType; defaultValue = -1 },
                    navArgument("cumulativeAnswered") { type = NavType.IntType; defaultValue = -1 },
                    navArgument("cumulativeExamCount") { type = NavType.IntType; defaultValue = -1 },
                    navArgument("sessionProgressId") { type = NavType.StringType; defaultValue = "" },
                ),
        ) { backStackEntry ->
            val score = backStackEntry.arguments?.getString("score")?.toIntOrNull() ?: 0
            val total = backStackEntry.arguments?.getString("total")?.toIntOrNull() ?: 0
            val unanswered = backStackEntry.arguments?.getString("unanswered")?.toIntOrNull() ?: 0
            val encodedQuiz = backStackEntry.arguments?.getString("quizId") ?: ""
            val cumulativeCorrect = backStackEntry.arguments?.getInt("cumulativeCorrect")?.takeIf { it != -1 }
            val cumulativeAnswered = backStackEntry.arguments?.getInt("cumulativeAnswered")?.takeIf { it != -1 }
            val cumulativeExamCount = backStackEntry.arguments?.getInt("cumulativeExamCount")?.takeIf { it != -1 }
            val encodedSessionProgressId = backStackEntry.arguments?.getString("sessionProgressId").orEmpty()
            val quizId = com.example.testapp.util.safeDecode(encodedQuiz)
            val sessionProgressId = encodedSessionProgressId.takeIf { it.isNotBlank() }?.let(::safeDecode)
            ResultRoute(
                score = score,
                total = total,
                unanswered = unanswered,
                quizId = quizId,
                cumulativeCorrect = cumulativeCorrect,
                cumulativeAnswered = cumulativeAnswered,
                cumulativeExamCount = cumulativeExamCount,
                onBackHome = { navController.popBackStack("home", false) },
                onViewDetail = {
                    val progressId = sessionProgressId ?: quizId
                    if (progressId.isBlank()) return@ResultRoute
                    val encoded = java.net.URLEncoder.encode(progressId, "UTF-8")
                    val route = if (quizId.startsWith("exam_")) "exam_review/$encoded" else "practice_review/$encoded"
                    navController.navigate(route)
                },
                onBack = { navController.popBackStack("home", false) },
            )
        }

        composable("wrongbook") {
            WrongBookRoute(
                onBack = { navController.popBackStack() },
                onOpenFile = { name ->
                    val encoded = java.net.URLEncoder.encode(name, "UTF-8")
                    navController.navigate("wrongbook/$encoded")
                },
                onStartWrongBookQuiz = { name ->
                    val encoded = java.net.URLEncoder.encode(name, "UTF-8")
                    navController.navigate("practice_wrongbook/$encoded")
                },
            )
        }
        composable(
            "wrongbook/{fileName}",
            arguments = listOf(navArgument("fileName") { type = NavType.StringType }),
        ) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("fileName") ?: ""
            val fileName = com.example.testapp.util.safeDecode(encoded)
            WrongBookRoute(
                fileName = fileName,
                onBack = { navController.popBackStack() },
                onOpenFile = { name ->
                    val enc = java.net.URLEncoder.encode(name, "UTF-8")
                    navController.navigate("wrongbook/$enc")
                },
                onStartWrongBookQuiz = { name ->
                    val enc = java.net.URLEncoder.encode(name, "UTF-8")
                    navController.navigate("practice_wrongbook/$enc")
                },
            )
        }

        composable("history") { HistoryRoute() }

        composable("settings") {
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateHome = { navController.popBackToHome() },
                onNavigateFillSettings = { navController.navigate("settings/fill") },
            )
        }
        composable("settings/fill") {
            FillSettingsScreen(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() },
            )
        }

        composable("favorite") {
            FavoriteRoute(
                onBack = { navController.popBackStack() },
                onOpenFile = { name ->
                    val encoded = java.net.URLEncoder.encode(name, "UTF-8")
                    navController.navigate("favorite/$encoded")
                },
                onStartFavoriteQuiz = { name ->
                    val encoded = java.net.URLEncoder.encode(name, "UTF-8")
                    navController.navigate("practice_favorite/$encoded")
                },
            )
        }
        composable(
            "favorite/{fileName}",
            arguments = listOf(navArgument("fileName") { type = NavType.StringType }),
        ) { backStackEntry ->
            val encodedFav = backStackEntry.arguments?.getString("fileName") ?: ""
            val fileName = com.example.testapp.util.safeDecode(encodedFav)
            FavoriteRoute(
                fileName = fileName,
                onBack = { navController.popBackStack() },
                onOpenFile = { name ->
                    val enc = java.net.URLEncoder.encode(name, "UTF-8")
                    navController.navigate("favorite/$enc")
                },
                onStartFavoriteQuiz = { name ->
                    val enc = java.net.URLEncoder.encode(name, "UTF-8")
                    navController.navigate("practice_favorite/$enc")
                },
            )
        }

        registerAiOverlayRoutes(navController)
    }
}
