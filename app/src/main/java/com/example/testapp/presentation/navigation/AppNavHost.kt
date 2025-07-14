package com.example.testapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.presentation.screen.PracticeViewModel
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
import com.example.testapp.presentation.screen.DeepSeekScreen

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
                },
                onViewResult = {
                    navController.navigate("result/0/0/")
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
                    val id = "practice_${quizId}"
                    val e = java.net.URLEncoder.encode(id, "UTF-8")
                    navController.navigate("result/$score/$total/$e") {
                        popUpTo("home") { inclusive = false }
                    }
                },
                onExitWithoutAnswer = { navController.popBackStack() },
                onViewDeepSeek = { text, id, index ->
                    val encodedText = java.net.URLEncoder.encode(text, "UTF-8")
                    navController.navigate("deepseek/$id/$index/$encodedText")
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
                    val id = "exam_${quizId}"
                    val e = java.net.URLEncoder.encode(id, "UTF-8")
                    navController.navigate("result/$score/$total/$e") {
                        popUpTo("home") { inclusive = false }
                    }
                },
                onExitWithoutAnswer = { navController.popBackStack() },
                onViewDeepSeek = { text, id, index ->
                    val encodedText = java.net.URLEncoder.encode(text, "UTF-8")
                    navController.navigate("deepseek/$id/$index/$encodedText")
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
        composable(
            "result/{score}/{total}/{quizId}",
            arguments = listOf(navArgument("quizId") { type = NavType.StringType })
        ) { backStackEntry ->
            val score = backStackEntry.arguments?.getString("score")?.toIntOrNull() ?: 0
            val total = backStackEntry.arguments?.getString("total")?.toIntOrNull() ?: 0
            val encodedQuiz = backStackEntry.arguments?.getString("quizId") ?: ""
            val quizId = java.net.URLDecoder.decode(encodedQuiz, "UTF-8")
            ResultScreen(score, total, quizId,
                onBackHome = { navController.popBackStack("home", false) },
                onViewDetail = {
                    if (encodedQuiz.isNotBlank()) {
                        navController.navigate("exam/$encodedQuiz")
                    }
                },
                onBack = { navController.popBackStack("home", false) }
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
                    val id = "practice_${name}"
                    val e = java.net.URLEncoder.encode(id, "UTF-8")
                    navController.navigate("result/$score/$total/$e") {
                        popUpTo("wrongbook") { inclusive = false }
                    }
                },
                onExitWithoutAnswer = { navController.popBackStack() },
                onViewDeepSeek = { text, id, index ->
                    val encodedText = java.net.URLEncoder.encode(text, "UTF-8")
                    navController.navigate("deepseek/$id/$index/$encodedText")
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
                    val id = "practice_${name}"
                    val e = java.net.URLEncoder.encode(id, "UTF-8")
                    navController.navigate("result/$score/$total/$e") {
                        popUpTo("favorite") { inclusive = false }
                    }
                },
                onExitWithoutAnswer = { navController.popBackStack() },
                onViewDeepSeek = { text, id, index ->
                    val encodedText = java.net.URLEncoder.encode(text, "UTF-8")
                    navController.navigate("deepseek/$id/$index/$encodedText")
                }
            )
        }
        composable(
            "deepseek/{id}/{index}/{text}",
            arguments = listOf(
                navArgument("id") { type = NavType.IntType },
                navArgument("index") { type = NavType.IntType },
                navArgument("text") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("text") ?: ""
            val text = java.net.URLDecoder.decode(encoded, "UTF-8")
            val id = backStackEntry.arguments?.getInt("id") ?: 0
            val index = backStackEntry.arguments?.getInt("index") ?: 0
            val parentEntry = remember(backStackEntry) { navController.previousBackStackEntry }
            val practiceViewModel: PracticeViewModel = parentEntry?.let { hiltViewModel(it) } ?: hiltViewModel()
            DeepSeekScreen(
                text = text,
                questionId = id,
                index = index,
                navController = navController,
                practiceViewModel = practiceViewModel,
                settingsViewModel = settingsViewModel
            )
        }
        // TODO: 添加更多页面
    }
}
