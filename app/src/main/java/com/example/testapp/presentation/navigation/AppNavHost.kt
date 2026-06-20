package com.example.testapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.presentation.screen.practice.PracticeViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.testapp.presentation.screen.home.HomeScreen
import com.example.testapp.presentation.screen.question.QuestionScreen
import com.example.testapp.presentation.screen.result.ResultScreen
import com.example.testapp.presentation.screen.wrongbook.WrongBookScreen
import com.example.testapp.presentation.screen.history.HistoryScreen
import com.example.testapp.presentation.screen.settings.SettingsScreen
import com.example.testapp.presentation.screen.favorite.FavoriteScreen
import com.example.testapp.presentation.screen.practice.PracticeScreen
import com.example.testapp.presentation.screen.exam.ExamScreen
import com.example.testapp.presentation.screen.ai.DeepSeekScreen
import com.example.testapp.presentation.screen.ai.DeepSeekAskScreen
import com.example.testapp.presentation.screen.note.NoteScreen
import com.example.testapp.presentation.screen.ai.SparkScreen
import com.example.testapp.presentation.screen.ai.SparkAskScreen
import com.example.testapp.presentation.screen.ai.BaiduScreen
import com.example.testapp.presentation.screen.ai.BaiduAskScreen
import com.example.testapp.presentation.screen.exam.ExamViewModel
import com.example.testapp.presentation.screen.ai.ExplanationScreen

import com.example.testapp.core.util.safeDecode
import com.example.testapp.core.util.safeEncode

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController(), settingsViewModel: com.example.testapp.presentation.screen.settings.SettingsViewModel) {
    val globalPracticeViewModel: PracticeViewModel = hiltViewModel()
    val globalExamViewModel: ExamViewModel = hiltViewModel()

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
                onSettings = { navController.navigate("settings") },
                onViewQuestionDetail = { quizId ->
                    val encoded = java.net.URLEncoder.encode(quizId, "UTF-8")
                    navController.navigate("question_detail/$encoded")
                },
                onWrongBook = { fileName ->
                    if (fileName.isBlank()) { navController.navigate("wrongbook") }
                    else { val encoded = java.net.URLEncoder.encode(fileName, "UTF-8"); navController.navigate("wrongbook/$encoded") }
                },
                onFavoriteBook = { fileName ->
                    if (fileName.isBlank()) { navController.navigate("favorite") }
                    else { val encoded = java.net.URLEncoder.encode(fileName, "UTF-8"); navController.navigate("favorite/$encoded") }
                },
                onViewResult = { fileName ->
                    val encoded = java.net.URLEncoder.encode(fileName, "UTF-8"); navController.navigate("result/0/0/0/$encoded")
                },
                onHistory = { navController.navigate("history") },
                settingsViewModel = settingsViewModel
            )
        }
        composable("question/{quizId}", arguments = listOf(navArgument("quizId") { type = NavType.StringType })) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("quizId") ?: "default"
            val quizId = com.example.testapp.util.safeDecode(encoded)
            PracticeScreen(
                quizId = quizId, settingsViewModel = settingsViewModel, viewModel = globalPracticeViewModel,
                onQuizEnd = { score, total, unanswered, cumulativeCorrect, cumulativeAnswered ->
                    val id = "practice_${quizId}"
                    val e = java.net.URLEncoder.encode(id, "UTF-8")
                    navController.navigate("result/$score/$total/$unanswered/$e?cumulativeCorrect=${cumulativeCorrect ?: -1}&cumulativeAnswered=${cumulativeAnswered ?: -1}") {
                        popUpTo("home") { inclusive = false }
                    }
                },
                onExitWithoutAnswer = { navController.popBackStack() },
                onViewDeepSeek = { text, id, index -> val encodedText = safeEncode(text); navController.navigate("deepseek/$id/$index/$encodedText") },
                onViewSpark = { text, id, index -> val encodedText = safeEncode(text); navController.navigate("spark/$id/$index/$encodedText") },
                onViewBaidu = { text, id, index -> val encodedText = safeEncode(text); navController.navigate("baidu/$id/$index/$encodedText") },
                onAskDeepSeek = { text, id, index -> val encodedText = safeEncode(text); navController.navigate("deepseek_ask/$id/$index/$encodedText") },
                onAskSpark = { text, id, index -> val encodedText = safeEncode(text); navController.navigate("spark_ask/$id/$index/$encodedText") },
                onAskBaidu = { text, id, index -> val encodedText = safeEncode(text); navController.navigate("baidu_ask/$id/$index/$encodedText") },
                onViewExplanation = { text -> val encodedText = safeEncode(text); navController.navigate("explanation/$encodedText") },
                onEditNote = { text, id, index -> val encodedText = safeEncode(text); navController.navigate("note/$id/$index/$encodedText") }
            )
        }
        composable("explanation/{text}", arguments = listOf(navArgument("text") { type = NavType.StringType })) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("text") ?: ""
            val text = com.example.testapp.util.safeDecode(encoded)
            ExplanationScreen(text = text, navController = navController, settingsViewModel = settingsViewModel)
        }
        composable("exam/{quizId}", arguments = listOf(navArgument("quizId") { type = NavType.StringType })) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("quizId") ?: "default"
            val quizId = com.example.testapp.util.safeDecode(encoded)
            ExamScreen(
                quizId = quizId, settingsViewModel = settingsViewModel, viewModel = globalExamViewModel,
                onExamEnd = { score, total, unanswered, cumulativeCorrect, cumulativeAnswered, cumulativeExamCount ->
                    val id = "exam_${quizId}"
                    val e = java.net.URLEncoder.encode(id, "UTF-8")
                    navController.navigate("result/$score/$total/$unanswered/$e?cumulativeCorrect=${cumulativeCorrect ?: -1}&cumulativeAnswered=${cumulativeAnswered ?: -1}&cumulativeExamCount=${cumulativeExamCount ?: -1}") {
                        popUpTo("home") { inclusive = false }
                    }
                },
                onExitWithoutAnswer = { navController.popBackStack() },
                onViewDeepSeek = { text, id, index -> val encodedText = safeEncode(text); navController.navigate("deepseek/$id/$index/$encodedText") },
                onViewSpark = { text, id, index -> val encodedText = safeEncode(text); navController.navigate("spark/$id/$index/$encodedText") },
                onViewBaidu = { text, id, index -> val encodedText = safeEncode(text); navController.navigate("baidu/$id/$index/$encodedText") },
                onAskDeepSeek = { text, id, index -> val encodedText = safeEncode(text); navController.navigate("deepseek_ask/$id/$index/$encodedText") },
                onAskSpark = { text, id, index -> val encodedText = safeEncode(text); navController.navigate("spark_ask/$id/$index/$encodedText") },
                onAskBaidu = { text, id, index -> val encodedText = safeEncode(text); navController.navigate("baidu_ask/$id/$index/$encodedText") },
                onViewExplanation = { text -> val encodedText = safeEncode(text); navController.navigate("explanation/$encodedText") },
                onEditNote = { text, id, index -> val encodedText = safeEncode(text); navController.navigate("note/$id/$index/$encodedText") }
            )
        }
        composable("question_detail/{quizId}", arguments = listOf(navArgument("quizId") { type = NavType.StringType })) { backStackEntry ->
            val encodedDetail = backStackEntry.arguments?.getString("quizId") ?: "default"
            val quizId = com.example.testapp.util.safeDecode(encodedDetail)
            QuestionScreen(quizId = quizId)
        }
        composable("result/{score}/{total}/{unanswered}/{quizId}?cumulativeCorrect={cumulativeCorrect}&cumulativeAnswered={cumulativeAnswered}&cumulativeExamCount={cumulativeExamCount}",
            arguments = listOf(
                navArgument("quizId") { type = NavType.StringType },
                navArgument("cumulativeCorrect") { type = NavType.IntType; defaultValue = -1 },
                navArgument("cumulativeAnswered") { type = NavType.IntType; defaultValue = -1 },
                navArgument("cumulativeExamCount") { type = NavType.IntType; defaultValue = -1 }
            )
        ) { backStackEntry ->
            val score = backStackEntry.arguments?.getString("score")?.toIntOrNull() ?: 0
            val total = backStackEntry.arguments?.getString("total")?.toIntOrNull() ?: 0
            val unanswered = backStackEntry.arguments?.getString("unanswered")?.toIntOrNull() ?: 0
            val encodedQuiz = backStackEntry.arguments?.getString("quizId") ?: ""
            val cumulativeCorrect = backStackEntry.arguments?.getInt("cumulativeCorrect")?.takeIf { it != -1 }
            val cumulativeAnswered = backStackEntry.arguments?.getInt("cumulativeAnswered")?.takeIf { it != -1 }
            val cumulativeExamCount = backStackEntry.arguments?.getInt("cumulativeExamCount")?.takeIf { it != -1 }
            val quizId = com.example.testapp.util.safeDecode(encodedQuiz)
            ResultScreen(
                score = score, total = total, unanswered = unanswered, quizId = quizId,
                cumulativeCorrect = cumulativeCorrect, cumulativeAnswered = cumulativeAnswered, cumulativeExamCount = cumulativeExamCount,
                onBackHome = { navController.popBackStack("home", false) },
                onViewDetail = {
                    if (encodedQuiz.isNotBlank()) {
                        val decoded = com.example.testapp.util.safeDecode(encodedQuiz)
                        val original = when { decoded.startsWith("exam_") -> decoded.removePrefix("exam_"); decoded.startsWith("practice_") -> decoded.removePrefix("practice_"); else -> decoded }
                        val encodedOriginal = java.net.URLEncoder.encode(original, "UTF-8")
                        val route = if (decoded.startsWith("exam_")) "exam/$encodedOriginal" else "question/$encodedOriginal"
                        navController.navigate(route)
                    }
                },
                onBack = { navController.popBackStack("home", false) }
            )
        }
        composable("wrongbook") { WrongBookScreen(navController = navController) }
        composable("wrongbook/{fileName}") { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("fileName") ?: ""
            val fileName = com.example.testapp.util.safeDecode(encoded)
            WrongBookScreen(fileName = fileName, navController = navController)
        }
        composable("practice_wrongbook/{fileName}") { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("fileName") ?: ""
            val name = com.example.testapp.util.safeDecode(encoded)
            PracticeScreen(
                isWrongBookMode = true, wrongBookFileName = name, settingsViewModel = settingsViewModel,
                onQuizEnd = { score, total, unanswered, cumulativeCorrect, cumulativeAnswered ->
                    val id = "practice_${name}"; val e = java.net.URLEncoder.encode(id, "UTF-8")
                    navController.navigate("result/$score/$total/$unanswered/$e?cumulativeCorrect=${cumulativeCorrect ?: -1}&cumulativeAnswered=${cumulativeAnswered ?: -1}") { popUpTo("wrongbook") { inclusive = false } }
                },
                onExitWithoutAnswer = { navController.popBackStack() },
                onViewDeepSeek = { text, id, index -> val encodedText = safeEncode(text); navController.navigate("deepseek/$id/$index/$encodedText") },
                onViewSpark = { text, id, index -> val encodedText = safeEncode(text); navController.navigate("spark/$id/$index/$encodedText") },
                onViewBaidu = { text, id, index -> val encodedText = safeEncode(text); navController.navigate("baidu/$id/$index/$encodedText") },
                onAskDeepSeek = { text, id, index -> val encodedText = safeEncode(text); navController.navigate("deepseek_ask/$id/$index/$encodedText") },
                onAskSpark = { text, id, index -> val encodedText = safeEncode(text); navController.navigate("spark_ask/$id/$index/$encodedText") },
                onAskBaidu = { text, id, index -> val encodedText = safeEncode(text); navController.navigate("baidu_ask/$id/$index/$encodedText") },
                onViewExplanation = { text -> val encodedText = safeEncode(text); navController.navigate("explanation/$encodedText") },
                onEditNote = { text, id, index -> val encodedText = safeEncode(text); navController.navigate("note/$id/$index/$encodedText") }
            )
        }
        composable("exam_wrongbook/{fileName}") { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("fileName") ?: ""
            val name = com.example.testapp.util.safeDecode(encoded)
            ExamScreen(
                quizId = name, isWrongBookMode = true, wrongBookFileName = name, settingsViewModel = settingsViewModel,
                onExamEnd = { score, total, unanswered, cumulativeCorrect, cumulativeAnswered, cumulativeExamCount ->
                    val id = "exam_${name}"; val e = java.net.URLEncoder.encode(id, "UTF-8")
                    navController.navigate("result/$score/$total/$unanswered/$e?cumulativeCorrect=${cumulativeCorrect ?: -1}&cumulativeAnswered=${cumulativeAnswered ?: -1}&cumulativeExamCount=${cumulativeExamCount ?: -1}") { popUpTo("wrongbook") { inclusive = false } }
                },
                onExitWithoutAnswer = { navController.popBackStack() },
                onViewDeepSeek = { text, id, index -> val encodedText = safeEncode(text); navController.navigate("deepseek/$id/$index/$encodedText") },
                onViewSpark = { text, id, index -> val encodedText = safeEncode(text); navController.navigate("spark/$id/$index/$encodedText") },
                onViewBaidu = { text, id, index -> val encodedText = safeEncode(text); navController.navigate("baidu/$id/$index/$encodedText") },
                onAskDeepSeek = { text, id, index -> val encodedText = safeEncode(text); navController.navigate("deepseek_ask/$id/$index/$encodedText") },
                onAskSpark = { text, id, index -> val encodedText = safeEncode(text); navController.navigate("spark_ask/$id/$index/$encodedText") },
                onAskBaidu = { text, id, index -> val encodedText = safeEncode(text); navController.navigate("baidu_ask/$id/$index/$encodedText") },
                onViewExplanation = { text -> val encodedText = safeEncode(text); navController.navigate("explanation/$encodedText") }
            )
        }
        composable("history") { HistoryScreen() }
        composable("settings") { SettingsScreen(viewModel = settingsViewModel, onNavigateHome = { navController.popBackStack() }) }
        composable("favorite") { FavoriteScreen(navController = navController) }
        composable("favorite/{fileName}") { backStackEntry ->
            val encodedFav = backStackEntry.arguments?.getString("fileName") ?: ""
            val fileName = com.example.testapp.util.safeDecode(encodedFav)
            FavoriteScreen(fileName = fileName, navController = navController)
        }
        composable("practice_favorite/{fileName}") { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("fileName") ?: ""
            val name = com.example.testapp.util.safeDecode(encoded)
            PracticeScreen(
                isFavoriteMode = true, favoriteFileName = name, settingsViewModel = settingsViewModel,
                onQuizEnd = { score, total, unanswered, cumulativeCorrect, cumulativeAnswered ->
                    val id = "practice_${name}"; val e = java.net.URLEncoder.encode(id, "UTF-8")
                    navController.navigate("result/$score/$total/$unanswered/$e?cumulativeCorrect=${cumulativeCorrect ?: -1}&cumulativeAnswered=${cumulativeAnswered ?: -1}") { popUpTo("favorite") { inclusive = false } }
                },
                onExitWithoutAnswer = { navController.popBackStack() },
                onViewDeepSeek = { text, id, index -> val encodedText = safeEncode(text); navController.navigate("deepseek/$id/$index/$encodedText") },
                onViewSpark = { text, id, index -> val encodedText = safeEncode(text); navController.navigate("spark/$id/$index/$encodedText") },
                onViewBaidu = { text, id, index -> val encodedText = safeEncode(text); navController.navigate("baidu/$id/$index/$encodedText") },
                onAskDeepSeek = { text, id, index -> val encodedText = safeEncode(text); navController.navigate("deepseek_ask/$id/$index/$encodedText") },
                onAskSpark = { text, id, index -> val encodedText = safeEncode(text); navController.navigate("spark_ask/$id/$index/$encodedText") },
                onAskBaidu = { text, id, index -> val encodedText = safeEncode(text); navController.navigate("baidu_ask/$id/$index/$encodedText") },
                onViewExplanation = { text -> val encodedText = safeEncode(text); navController.navigate("explanation/$encodedText") },
                onEditNote = { text, id, index -> val encodedText = safeEncode(text); navController.navigate("note/$id/$index/$encodedText") }
            )
        }
        composable("exam_favorite/{fileName}") { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("fileName") ?: ""
            val name = com.example.testapp.util.safeDecode(encoded)
            ExamScreen(
                quizId = name, isFavoriteMode = true, favoriteFileName = name, settingsViewModel = settingsViewModel,
                onExamEnd = { score, total, unanswered, cumulativeCorrect, cumulativeAnswered, cumulativeExamCount ->
                    val id = "exam_${name}"; val e = java.net.URLEncoder.encode(id, "UTF-8")
                    navController.navigate("result/$score/$total/$unanswered/$e?cumulativeCorrect=${cumulativeCorrect ?: -1}&cumulativeAnswered=${cumulativeAnswered ?: -1}&cumulativeExamCount=${cumulativeExamCount ?: -1}") { popUpTo("favorite") { inclusive = false } }
                },
                onExitWithoutAnswer = { navController.popBackStack() },
                onViewDeepSeek = { text, id, index -> val encodedText = safeEncode(text); navController.navigate("deepseek/$id/$index/$encodedText") },
                onViewSpark = { text, id, index -> val encodedText = safeEncode(text); navController.navigate("spark/$id/$index/$encodedText") },
                onViewBaidu = { text, id, index -> val encodedText = safeEncode(text); navController.navigate("baidu/$id/$index/$encodedText") },
                onAskDeepSeek = { text, id, index -> val encodedText = safeEncode(text); navController.navigate("deepseek_ask/$id/$index/$encodedText") },
                onAskSpark = { text, id, index -> val encodedText = safeEncode(text); navController.navigate("spark_ask/$id/$index/$encodedText") },
                onAskBaidu = { text, id, index -> val encodedText = safeEncode(text); navController.navigate("baidu_ask/$id/$index/$encodedText") },
                onViewExplanation = { text -> val encodedText = safeEncode(text); navController.navigate("explanation/$encodedText") },
                onEditNote = { text, id, index -> val encodedText = safeEncode(text); navController.navigate("note/$id/$index/$encodedText") }
            )
        }
        composable("deepseek/{id}/{index}/{text}", arguments = listOf(navArgument("id") { type = NavType.IntType }, navArgument("index") { type = NavType.IntType }, navArgument("text") { type = NavType.StringType })) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("text") ?: ""; val text = com.example.testapp.util.safeDecode(encoded)
            val id = backStackEntry.arguments?.getInt("id") ?: 0; val index = backStackEntry.arguments?.getInt("index") ?: 0
            val parentEntry = remember(backStackEntry) { navController.previousBackStackEntry }
            val parentRoute = parentEntry?.destination?.route.orEmpty()
            val examViewModel: ExamViewModel? = if (parentRoute.startsWith("exam")) parentEntry?.let { hiltViewModel(it) } else null
            val practiceViewModel: PracticeViewModel? = if (!parentRoute.startsWith("exam")) parentEntry?.let { hiltViewModel(it) } else null
            DeepSeekScreen(text = text, questionId = id, index = index, navController = navController,
                onSave = { examViewModel?.updateAnalysis(index, it); practiceViewModel?.updateAnalysis(index, it) }, settingsViewModel = settingsViewModel)
        }
        composable("deepseek_ask/{id}/{index}/{text}", arguments = listOf(navArgument("id") { type = NavType.IntType }, navArgument("index") { type = NavType.IntType }, navArgument("text") { type = NavType.StringType })) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("text") ?: ""; val text = com.example.testapp.util.safeDecode(encoded)
            val id = backStackEntry.arguments?.getInt("id") ?: 0; val index = backStackEntry.arguments?.getInt("index") ?: 0
            val isExamMode = remember(backStackEntry) { navController.backQueue.any { entry -> entry.destination.route?.startsWith("exam") == true } }
            val examViewModel = if (isExamMode) globalExamViewModel else null; val practiceViewModel = if (!isExamMode) globalPracticeViewModel else null
            DeepSeekAskScreen(text = text, questionId = id, index = index, navController = navController,
                onSave = { val note = "【DeepSeek问答】\n$it"; examViewModel?.appendNoteSuspend(id, index, note); practiceViewModel?.appendNoteSuspend(id, index, note) }, settingsViewModel = settingsViewModel)
        }
        composable("spark_ask/{id}/{index}/{text}", arguments = listOf(navArgument("id") { type = NavType.IntType }, navArgument("index") { type = NavType.IntType }, navArgument("text") { type = NavType.StringType })) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("text") ?: ""; val text = com.example.testapp.util.safeDecode(encoded)
            val id = backStackEntry.arguments?.getInt("id") ?: 0; val index = backStackEntry.arguments?.getInt("index") ?: 0
            val isExamMode = remember(backStackEntry) { navController.backQueue.any { entry -> entry.destination.route?.startsWith("exam") == true } }
            val examViewModel = if (isExamMode) globalExamViewModel else null; val practiceViewModel = if (!isExamMode) globalPracticeViewModel else null
            SparkAskScreen(text = text, questionId = id, index = index, navController = navController,
                onSave = { val note = "【Spark问答】\n$it"; examViewModel?.appendNoteSuspend(id, index, note); practiceViewModel?.appendNoteSuspend(id, index, note) }, settingsViewModel = settingsViewModel)
        }
        composable("spark/{id}/{index}/{text}", arguments = listOf(navArgument("id") { type = NavType.IntType }, navArgument("index") { type = NavType.IntType }, navArgument("text") { type = NavType.StringType })) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("text") ?: ""; val text = com.example.testapp.util.safeDecode(encoded)
            val id = backStackEntry.arguments?.getInt("id") ?: 0; val index = backStackEntry.arguments?.getInt("index") ?: 0
            val parentEntry = remember(backStackEntry) { navController.previousBackStackEntry }; val parentRoute = parentEntry?.destination?.route.orEmpty()
            val examViewModel: ExamViewModel? = if (parentRoute.startsWith("exam")) parentEntry?.let { hiltViewModel(it) } else null
            val practiceViewModel: PracticeViewModel? = if (!parentRoute.startsWith("exam")) parentEntry?.let { hiltViewModel(it) } else null
            SparkScreen(text = text, questionId = id, index = index, navController = navController,
                onSave = { examViewModel?.updateSparkAnalysis(index, it); practiceViewModel?.updateSparkAnalysis(index, it) }, settingsViewModel = settingsViewModel)
        }
        composable("note/{id}/{index}/{text}", arguments = listOf(navArgument("id") { type = NavType.IntType }, navArgument("index") { type = NavType.IntType }, navArgument("text") { type = NavType.StringType })) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("text") ?: ""; val text = com.example.testapp.util.safeDecode(encoded)
            val id = backStackEntry.arguments?.getInt("id") ?: 0; val index = backStackEntry.arguments?.getInt("index") ?: 0
            val parentEntry = remember(backStackEntry) { navController.previousBackStackEntry }; val parentRoute = parentEntry?.destination?.route.orEmpty()
            val examViewModel = if (parentRoute.startsWith("exam")) globalExamViewModel else null; val practiceViewModel = if (!parentRoute.startsWith("exam")) globalPracticeViewModel else null
            NoteScreen(text = text, questionId = id, index = index, navController = navController,
                onSave = { examViewModel?.saveNote(id, index, it); practiceViewModel?.saveNote(id, index, it) }, settingsViewModel = settingsViewModel, examViewModel = examViewModel, practiceViewModel = practiceViewModel)
        }
        composable("baidu/{id}/{index}/{text}", arguments = listOf(navArgument("id") { type = NavType.IntType }, navArgument("index") { type = NavType.IntType }, navArgument("text") { type = NavType.StringType })) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("text") ?: ""; val text = com.example.testapp.util.safeDecode(encoded)
            val id = backStackEntry.arguments?.getInt("id") ?: 0; val index = backStackEntry.arguments?.getInt("index") ?: 0
            val parentEntry = remember(backStackEntry) { navController.previousBackStackEntry }; val parentRoute = parentEntry?.destination?.route.orEmpty()
            val examViewModel: ExamViewModel? = if (parentRoute.startsWith("exam")) parentEntry?.let { hiltViewModel(it) } else null
            val practiceViewModel: PracticeViewModel? = if (!parentRoute.startsWith("exam")) parentEntry?.let { hiltViewModel(it) } else null
            BaiduScreen(text = text, questionId = id, index = index, navController = navController,
                onSave = { examViewModel?.updateBaiduAnalysis(index, it); practiceViewModel?.updateBaiduAnalysis(index, it) }, settingsViewModel = settingsViewModel)
        }
        composable("baidu_ask/{id}/{index}/{text}", arguments = listOf(navArgument("id") { type = NavType.IntType }, navArgument("index") { type = NavType.IntType }, navArgument("text") { type = NavType.StringType })) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("text") ?: ""; val text = com.example.testapp.util.safeDecode(encoded)
            val id = backStackEntry.arguments?.getInt("id") ?: 0; val index = backStackEntry.arguments?.getInt("index") ?: 0
            val isExamMode = remember(backStackEntry) { navController.backQueue.any { entry -> entry.destination.route?.startsWith("exam") == true } }
            val examViewModel = if (isExamMode) globalExamViewModel else null; val practiceViewModel = if (!isExamMode) globalPracticeViewModel else null
            BaiduAskScreen(text = text, questionId = id, index = index, navController = navController,
                onSave = { val note = "【百度问答】\n$it"; examViewModel?.appendNoteSuspend(id, index, note); practiceViewModel?.appendNoteSuspend(id, index, note) }, settingsViewModel = settingsViewModel)
        }
    }
}