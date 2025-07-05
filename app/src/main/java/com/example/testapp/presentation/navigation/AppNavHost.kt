package com.example.testapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.testapp.presentation.screen.HomeScreen
import com.example.testapp.presentation.screen.QuestionScreen
import com.example.testapp.presentation.screen.ResultScreen
import com.example.testapp.presentation.screen.WrongBookScreen
import com.example.testapp.presentation.screen.HistoryScreen
import com.example.testapp.presentation.screen.SettingsScreen
import com.example.testapp.presentation.screen.FavoriteScreen
import com.example.testapp.presentation.screen.PracticeScreen
import com.example.testapp.presentation.screen.ExamScreen

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController(), settingsViewModel: com.example.testapp.presentation.screen.SettingsViewModel) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onStartQuiz = { quizId ->
                    val encoded = java.net.URLEncoder.encode(quizId, "UTF-8")
                    navController.navigate("question/$encoded")
                },
                onStartExam = { quizId ->
                    val encoded = java.net.URLEncoder.encode(quizId, "UTF-8")
                    navController.navigate("exam/$encoded")
                },
                onSettings = { navController.navigate("settings") },
                onViewQuestionDetail = { quizId ->
                    val encoded = java.net.URLEncoder.encode(quizId, "UTF-8")
                    navController.navigate("question_detail/$encoded")
                },
                onWrongBook = { fileName ->
                    if (fileName.isBlank()) {
                        navController.navigate("wrongbook")
                    } else {
                        val encoded = java.net.URLEncoder.encode(fileName, "UTF-8")
                        navController.navigate("wrongbook/$encoded")
                    }
                },
                onFavoriteBook = { fileName ->
                    if (fileName.isBlank()) {
                        navController.navigate("favorite")
                    } else {
                        val encoded = java.net.URLEncoder.encode(fileName, "UTF-8")
                        navController.navigate("favorite/$encoded")
                    }
                }
            )
        }
        composable(
            "question/{quizId}",
            arguments = listOf(navArgument("quizId") { type = NavType.StringType })
        ) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("quizId") ?: "default"
            val quizId = java.net.URLDecoder.decode(encoded, "UTF-8")
            PracticeScreen(
                quizId = quizId,
                settingsViewModel = settingsViewModel,
                onQuizEnd = { score, total ->
                    navController.navigate("result/$score/$total") {
                        popUpTo("home") { inclusive = false }
                    }
                }
            )
        }
        composable(
            "exam/{quizId}",
            arguments = listOf(navArgument("quizId") { type = NavType.StringType })
        ) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("quizId") ?: "default"
            val quizId = java.net.URLDecoder.decode(encoded, "UTF-8")
            ExamScreen(
                quizId = quizId,
                settingsViewModel = settingsViewModel,
                onExamEnd = { score, total ->
                    navController.navigate("result/$score/$total") {
                        popUpTo("home") { inclusive = false }
                    }
                }
            )
        }
        composable(
            "question_detail/{quizId}",
            arguments = listOf(navArgument("quizId") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedDetail = backStackEntry.arguments?.getString("quizId") ?: "default"
            val quizId = java.net.URLDecoder.decode(encodedDetail, "UTF-8")
            QuestionScreen(quizId = quizId)
        }
        composable("result/{score}/{total}") { backStackEntry ->
            val score = backStackEntry.arguments?.getString("score")?.toIntOrNull() ?: 0
            val total = backStackEntry.arguments?.getString("total")?.toIntOrNull() ?: 0
            ResultScreen(score, total,
                onBackHome = { navController.popBackStack("home", false) },
                onViewWrongBook = { navController.navigate("wrongbook") },
                onViewHistory = { navController.navigate("history") }
            )
        }
        composable("wrongbook") { WrongBookScreen(navController = navController) }
        composable("wrongbook/{fileName}") { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("fileName") ?: ""
            val fileName = java.net.URLDecoder.decode(encoded, "UTF-8")
            WrongBookScreen(fileName = fileName, navController = navController)
        }
        composable("practice_wrongbook/{fileName}") { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("fileName") ?: ""
            val name = java.net.URLDecoder.decode(encoded, "UTF-8")
            PracticeScreen(
                isWrongBookMode = true,
                wrongBookFileName = name,
                settingsViewModel = settingsViewModel,
                onQuizEnd = { score, total ->
                    navController.navigate("result/$score/$total") {
                        popUpTo("wrongbook") { inclusive = false }
                    }
                }
            )


        }

        composable("history") { HistoryScreen() }
        composable("settings") { SettingsScreen(viewModel = settingsViewModel) }
        composable("favorite") { FavoriteScreen(navController = navController) }
        composable("favorite/{fileName}") { backStackEntry ->
            val encodedFav = backStackEntry.arguments?.getString("fileName") ?: ""
            val fileName = java.net.URLDecoder.decode(encodedFav, "UTF-8")
            FavoriteScreen(fileName = fileName, navController = navController)
        }
        composable("practice_favorite/{fileName}") { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("fileName") ?: ""
            val name = java.net.URLDecoder.decode(encoded, "UTF-8")
            PracticeScreen(
                isFavoriteMode = true,
                favoriteFileName = name,
                settingsViewModel = settingsViewModel,
                onQuizEnd = { score, total ->
                    navController.navigate("result/$score/$total") {
                        popUpTo("favorite") { inclusive = false }
                    }
                }
            )
        }

        // TODO: 添加更多页面
    }
}
