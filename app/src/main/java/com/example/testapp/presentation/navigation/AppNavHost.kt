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
import com.example.testapp.presentation.screen.WrongBookPracticeScreen
import com.example.testapp.presentation.screen.FavoriteScreen
import com.example.testapp.presentation.screen.PracticeScreen

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController(), settingsViewModel: com.example.testapp.presentation.screen.SettingsViewModel) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onStartQuiz = { quizId -> navController.navigate("question/$quizId") },
                onSettings = { navController.navigate("settings") },
                onViewQuestionDetail = { quizId -> navController.navigate("question_detail/$quizId") },
                onWrongBook = { fileName -> navController.navigate("wrongbook/$fileName") },
                onFavoriteBook = { fileName -> navController.navigate("favorite/$fileName") }
            )
        }
        composable(
            "question/{quizId}",
            arguments = listOf(navArgument("quizId") { type = NavType.StringType })
        ) { backStackEntry ->
            val quizId = backStackEntry.arguments?.getString("quizId") ?: "default"
            PracticeScreen(quizId = quizId,
                onQuizEnd = { score, total ->
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
            val quizId = backStackEntry.arguments?.getString("quizId") ?: "default"
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
        composable("wrongbook/{fileName}") { backStackEntry ->
            val fileName = backStackEntry.arguments?.getString("fileName") ?: ""
            WrongBookScreen(fileName = fileName)
        }
        composable("wrongbook_practice") { WrongBookPracticeScreen() }
        composable("history") { HistoryScreen() }
        composable("settings") { SettingsScreen(viewModel = settingsViewModel) }
        composable("favorite/{fileName}") { backStackEntry ->
            val fileName = backStackEntry.arguments?.getString("fileName") ?: ""
            FavoriteScreen(fileName = fileName, navController = navController)
        }
        composable("question_fav") {
            PracticeScreen(
                quizId = "favorite",
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
