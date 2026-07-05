package com.example.testapp.presentation.navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.testapp.presentation.screen.ai.BaiduAskScreen
import com.example.testapp.presentation.screen.ai.BaiduScreen
import com.example.testapp.presentation.screen.ai.DeepSeekAskScreen
import com.example.testapp.presentation.screen.ai.SparkAskScreen
import com.example.testapp.presentation.screen.ai.SparkScreen
import com.example.testapp.presentation.screen.note.NoteScreen

private val aiOverlayIdIndexTextArgs =
    listOf(
        navArgument("id") { type = NavType.IntType },
        navArgument("index") { type = NavType.IntType },
        navArgument("text") { type = NavType.StringType },
    )

fun NavGraphBuilder.registerAiOverlayRoutes(
    navController: NavHostController,
) {
    val onBack: () -> Unit = { navController.popBackStack(); Unit }

    composable("deepseek/{id}/{index}/{text}", arguments = aiOverlayIdIndexTextArgs) { entry ->
        val encoded = entry.arguments?.getString("text") ?: ""
        val text = com.example.testapp.util.safeDecode(encoded)
        val id = entry.arguments?.getInt("id") ?: 0
        val index = entry.arguments?.getInt("index") ?: 0
        val sessions = rememberAiOverlayParentSessions(navController, entry)
        val examAnchor = remember(sessions, index) {
            deepSeekExamAnchorFrom(sessions.examBindings, sessions.practiceBindings, index)
        }
        DeepSeekAskScreen(
            text = text,
            questionId = id,
            index = index,
            examAnchor = examAnchor,
            onBack = onBack,
            onSave = {
                com.example.testapp.presentation.screen.practice.PracticeJumpDebugLog.analysisSave(
                    routeIndex = index,
                    practiceCurrentIndex = sessions.practiceBindings?.currentIndex?.value,
                    questionId = id,
                )
                AppNavAiWritebackPipeline.updateAnalysis(sessions, index, it)
            },
        )
    }

    composable("deepseek_ask/{id}/{index}/{text}", arguments = aiOverlayIdIndexTextArgs) { entry ->
        val encoded = entry.arguments?.getString("text") ?: ""
        val text = com.example.testapp.util.safeDecode(encoded)
        val id = entry.arguments?.getInt("id") ?: 0
        val index = entry.arguments?.getInt("index") ?: 0
        val sessions = rememberAiOverlayParentSessions(navController, entry)
        val examAnchor = remember(sessions, index) {
            deepSeekExamAnchorFrom(sessions.examBindings, sessions.practiceBindings, index)
        }
        DeepSeekAskScreen(
            text = text,
            questionId = id,
            index = index,
            examAnchor = examAnchor,
            onBack = onBack,
            onSave = {
                com.example.testapp.presentation.screen.practice.PracticeJumpDebugLog.analysisSave(
                    routeIndex = index,
                    practiceCurrentIndex = sessions.practiceBindings?.currentIndex?.value,
                    questionId = id,
                )
                AppNavAiWritebackPipeline.updateAnalysis(sessions, index, it)
            },
        )
    }

    composable("spark_ask/{id}/{index}/{text}", arguments = aiOverlayIdIndexTextArgs) { entry ->
        val encoded = entry.arguments?.getString("text") ?: ""
        val text = com.example.testapp.util.safeDecode(encoded)
        val id = entry.arguments?.getInt("id") ?: 0
        val index = entry.arguments?.getInt("index") ?: 0
        val sessions = rememberAiOverlayParentSessions(navController, entry)
        SparkAskScreen(
            text = text,
            questionId = id,
            index = index,
            onBack = onBack,
            onSave = {
                val note = "【Spark问答】\n$it"
                AppNavAiWritebackPipeline.appendNote(sessions, id, index, note)
            },
        )
    }

    composable("spark/{id}/{index}/{text}", arguments = aiOverlayIdIndexTextArgs) { entry ->
        val encoded = entry.arguments?.getString("text") ?: ""
        val text = com.example.testapp.util.safeDecode(encoded)
        val id = entry.arguments?.getInt("id") ?: 0
        val index = entry.arguments?.getInt("index") ?: 0
        val sessions = rememberAiOverlayParentSessions(navController, entry)
        SparkScreen(
            text = text,
            questionId = id,
            index = index,
            onBack = onBack,
            onOpenAsk = { enc -> navController.navigateAiAsk("spark_ask", id, index, enc) },
            onSave = { AppNavAiWritebackPipeline.updateSparkAnalysis(sessions, index, it) },
        )
    }

    composable("note/{id}/{index}/{text}", arguments = aiOverlayIdIndexTextArgs) { entry ->
        val encoded = entry.arguments?.getString("text") ?: ""
        val text = com.example.testapp.util.safeDecode(encoded)
        val id = entry.arguments?.getInt("id") ?: 0
        val index = entry.arguments?.getInt("index") ?: 0
        val sessions = rememberAiOverlayParentSessions(navController, entry)
        val examNotes by (sessions.examBindings?.noteList?.collectAsState(initial = emptyList())
            ?: remember { androidx.compose.runtime.mutableStateOf(emptyList()) })
        val practiceNotes by (sessions.practiceBindings?.noteList?.collectAsState(initial = emptyList())
            ?: remember { androidx.compose.runtime.mutableStateOf(emptyList()) })
        val currentNote = when {
            sessions.examBindings != null && index < examNotes.size -> examNotes[index]
            sessions.practiceBindings != null && index < practiceNotes.size -> practiceNotes[index]
            else -> ""
        }
        NoteScreen(
            text = text,
            questionId = id,
            index = index,
            currentNote = currentNote,
            onBack = onBack,
            onOpenDeepSeekAsk = { enc -> navController.navigateAiAsk("deepseek_ask", id, index, enc) },
            onSave = { AppNavAiWritebackPipeline.saveNote(sessions, id, index, it) },
        )
    }

    composable("baidu/{id}/{index}/{text}", arguments = aiOverlayIdIndexTextArgs) { entry ->
        val encoded = entry.arguments?.getString("text") ?: ""
        val text = com.example.testapp.util.safeDecode(encoded)
        val id = entry.arguments?.getInt("id") ?: 0
        val index = entry.arguments?.getInt("index") ?: 0
        val sessions = rememberAiOverlayParentSessions(navController, entry)
        BaiduScreen(
            text = text,
            questionId = id,
            index = index,
            onBack = onBack,
            onOpenAsk = { enc -> navController.navigateAiAsk("baidu_ask", id, index, enc) },
            onSave = { AppNavAiWritebackPipeline.updateBaiduAnalysis(sessions, index, it) },
        )
    }

    composable("baidu_ask/{id}/{index}/{text}", arguments = aiOverlayIdIndexTextArgs) { entry ->
        val encoded = entry.arguments?.getString("text") ?: ""
        val text = com.example.testapp.util.safeDecode(encoded)
        val id = entry.arguments?.getInt("id") ?: 0
        val index = entry.arguments?.getInt("index") ?: 0
        val sessions = rememberAiOverlayParentSessions(navController, entry)
        BaiduAskScreen(
            text = text,
            questionId = id,
            index = index,
            onBack = onBack,
            onSave = {
                val note = "【百度问答】\n$it"
                AppNavAiWritebackPipeline.appendNote(sessions, id, index, note)
            },
        )
    }
}
