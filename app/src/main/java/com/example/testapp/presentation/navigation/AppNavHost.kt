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

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onStartQuiz = { quizId -> navController.navigate("question/$quizId") },
                onSettings = { navController.navigate("settings") },
                onViewQuestionDetail = { quizId -> navController.navigate("question_detail/$quizId") }
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
        composable("wrongbook") { WrongBookScreen() }

        composable("history") { HistoryScreen() }
        composable("settings") { SettingsScreen() }
        composable("favorite") { FavoriteScreen(navController = navController) }
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
