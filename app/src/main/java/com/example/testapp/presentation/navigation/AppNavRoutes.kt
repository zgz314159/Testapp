package com.example.testapp.presentation.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.testapp.presentation.screen.ai.BaiduAskScreen
import com.example.testapp.presentation.screen.ai.BaiduScreen
import com.example.testapp.data.network.deepseek.DeepSeekExamAnchorPipeline
import com.example.testapp.presentation.screen.ai.DeepSeekAskScreen
import com.example.testapp.presentation.screen.ai.DeepSeekScreen
import com.example.testapp.presentation.screen.exam.ExamViewModel
import com.example.testapp.presentation.screen.note.NoteScreen
import com.example.testapp.presentation.screen.practice.PracticeViewModel
import com.example.testapp.presentation.screen.ai.SparkAskScreen
import com.example.testapp.presentation.screen.ai.SparkScreen
import com.example.testapp.core.util.safeDecode
import com.example.testapp.core.util.safeEncode

// ---- Practice/Exam navigation callback helpers ----

/** Creates the 7 standard analysis/edit callbacks shared across all Practice/Exam route instances. */
internal fun NavHostController.createAnalysisCallbacks(): AnalysisCallbacks = AnalysisCallbacks(
    onViewDeepSeek = { text, id, index ->
        navToEncoded("deepseek_ask/$id/$index", text)
    },
    onViewSpark = { text, id, index ->
        navToEncoded("spark/$id/$index", text)
    },
    onAskDeepSeek = { text, id, index ->
        navToEncoded("deepseek_ask/$id/$index", text)
    },
    onAskSpark = { text, id, index ->
        navToEncoded("spark_ask/$id/$index", text)
    },
    onViewBaidu = { text, id, index ->
        navToEncoded("baidu/$id/$index", text)
    },
    onAskBaidu = { text, id, index ->
        navToEncoded("baidu_ask/$id/$index", text)
    },
    onEditNote = { text, id, index ->
        navToEncoded("note/$id/$index", text)
    }
)

private fun NavHostController.navToEncoded(prefix: String, text: String) {
    val encodedText = safeEncode(text)
    navigate("$prefix/$encodedText")
}

// ---- Analysis screen route builders ----
// DeepSeek, Spark, Baidu each have a "view" and "ask" variant (+ Note).
// All 8 routes share: route "name/{id}/{index}/{text}", decode text/id/index, resolve owners.

private val ANALYTIC_SCREEN_ARGUMENTS = listOf(
    navArgument("id") { type = NavType.IntType },
    navArgument("index") { type = NavType.IntType },
    navArgument("text") { type = NavType.StringType }
)

internal fun NavGraphBuilder.registerAnalysisRoutes(
    navController: NavHostController,
    resolveOwners: @Composable (NavBackStackEntry?) -> Pair<PracticeViewModel?, ExamViewModel?>,
    settingsViewModel: com.example.testapp.presentation.screen.settings.SettingsViewModel
) {
    registerScreenRoute("deepseek", navController, resolveOwners, settingsViewModel) { pvm, evm, params ->
        val examAnchor = remember(pvm, evm, params.index) {
            DeepSeekExamAnchorPipeline.fromQuestion(
                evm?.questions?.value?.getOrNull(params.index)
                    ?: pvm?.questions?.value?.getOrNull(params.index)
            )
        }
        DeepSeekAskScreen(
            text = params.text,
            questionId = params.id,
            index = params.index,
            navController = navController,
            examAnchor = examAnchor,
            onSave = { v -> evm?.updateAnalysis(params.index, v); pvm?.updateAnalysis(params.index, v) },
            settingsViewModel = settingsViewModel
        )
    }
    registerScreenRoute("deepseek_ask", navController, resolveOwners, settingsViewModel) { pvm, evm, params ->
        val examAnchor = remember(pvm, evm, params.index) {
            DeepSeekExamAnchorPipeline.fromQuestion(
                evm?.questions?.value?.getOrNull(params.index)
                    ?: pvm?.questions?.value?.getOrNull(params.index)
            )
        }
        DeepSeekAskScreen(
            text = params.text,
            questionId = params.id,
            index = params.index,
            navController = navController,
            examAnchor = examAnchor,
            onSave = { v -> evm?.updateAnalysis(params.index, v); pvm?.updateAnalysis(params.index, v) },
            settingsViewModel = settingsViewModel
        )
    }
    registerScreenRoute("spark", navController, resolveOwners, settingsViewModel) { pvm, evm, params ->
        SparkScreen(
            text = params.text, questionId = params.id, index = params.index, navController = navController,
            onSave = { v -> evm?.updateSparkAnalysis(params.index, v); pvm?.updateSparkAnalysis(params.index, v) },
            settingsViewModel = settingsViewModel
        )
    }
    registerScreenRoute("spark_ask", navController, resolveOwners, settingsViewModel) { pvm, evm, params ->
        SparkAskScreen(
            text = params.text, questionId = params.id, index = params.index, navController = navController,
            onSave = { v -> evm?.updateSparkAnalysis(params.index, v); pvm?.updateSparkAnalysis(params.index, v) },
            settingsViewModel = settingsViewModel
        )
    }
    registerScreenRoute("baidu", navController, resolveOwners, settingsViewModel) { pvm, evm, params ->
        BaiduScreen(
            text = params.text, questionId = params.id, index = params.index, navController = navController,
            onSave = { v -> evm?.updateBaiduAnalysis(params.index, v); pvm?.updateBaiduAnalysis(params.index, v) },
            settingsViewModel = settingsViewModel
        )
    }
    registerScreenRoute("baidu_ask", navController, resolveOwners, settingsViewModel) { pvm, evm, params ->
        BaiduAskScreen(
            text = params.text, questionId = params.id, index = params.index, navController = navController,
            onSave = { v -> evm?.updateBaiduAnalysis(params.index, v); pvm?.updateBaiduAnalysis(params.index, v) },
            settingsViewModel = settingsViewModel
        )
    }
    // Note screen is special — passes both VMs directly
    composable(
        "note/{id}/{index}/{text}",
        arguments = ANALYTIC_SCREEN_ARGUMENTS
    ) { backStackEntry ->
        val decoded = AnalyticsRouteParams.from(backStackEntry)
        val parentEntry = remember(backStackEntry) { navController.previousBackStackEntry }
        val (practiceViewModel, examViewModel) = resolveOwners(parentEntry)
        val scope = rememberCoroutineScope()
        Log.d("NoteTraceNav", "note route: parentRoute=${parentEntry?.destination?.route}, questionId=${decoded.id}, index=${decoded.index}, textLength=${decoded.text.length}, hasPracticeOwner=${practiceViewModel != null}, hasExamOwner=${examViewModel != null}")
        NoteScreen(
            text = decoded.text, questionId = decoded.id, index = decoded.index,
            navController = navController,
            onSave = { text ->
                scope.launch {
                    when {
                        examViewModel != null -> examViewModel.saveNoteAndWait(decoded.id, decoded.index, text)
                        practiceViewModel != null -> practiceViewModel.saveNoteAndWait(decoded.id, decoded.index, text)
                    }
                }
            },
            settingsViewModel = settingsViewModel,
            examViewModel = examViewModel,
            practiceViewModel = practiceViewModel
        )
    }
}

private fun NavGraphBuilder.registerScreenRoute(
    routeName: String,
    navController: NavHostController,
    resolveOwners: @Composable (NavBackStackEntry?) -> Pair<PracticeViewModel?, ExamViewModel?>,
    settingsViewModel: com.example.testapp.presentation.screen.settings.SettingsViewModel,
    content: @Composable (PracticeViewModel?, ExamViewModel?, AnalyticsRouteParams) -> Unit
) {
    composable(
        "$routeName/{id}/{index}/{text}",
        arguments = ANALYTIC_SCREEN_ARGUMENTS
    ) { backStackEntry ->
        val decoded = AnalyticsRouteParams.from(backStackEntry)
        val parentEntry = remember(backStackEntry) { navController.previousBackStackEntry }
        val (practiceViewModel, examViewModel) = resolveOwners(parentEntry)
        content(practiceViewModel, examViewModel, decoded)
    }
}

private data class AnalyticsRouteParams(val id: Int, val index: Int, val text: String) {
    companion object {
        fun from(entry: NavBackStackEntry): AnalyticsRouteParams {
            val encoded = entry.arguments?.getString("text") ?: ""
            return AnalyticsRouteParams(
                id = entry.arguments?.getInt("id") ?: 0,
                index = entry.arguments?.getInt("index") ?: 0,
                text = safeDecode(encoded)
            )
        }
    }
}

// Practice/Exam result navigation
internal fun NavHostController.navToResult(
    prefix: String, quizId: String, score: Int, total: Int, unanswered: Int,
    cumulativeCorrect: Int?, cumulativeAnswered: Int?, cumulativeExamCount: Int? = null,
    sessionProgressId: String? = null,
    popUpTo: String = "home"
) {
    val id = "${prefix}_$quizId"
    val e = java.net.URLEncoder.encode(id, "UTF-8")
    val extras = buildString {
        append("result/$score/$total/$unanswered/$e")
        append("?cumulativeCorrect=${cumulativeCorrect ?: -1}")
        append("&cumulativeAnswered=${cumulativeAnswered ?: -1}")
        if (cumulativeExamCount != null) append("&cumulativeExamCount=$cumulativeExamCount")
        if (!sessionProgressId.isNullOrBlank()) {
            append("&sessionProgressId=${java.net.URLEncoder.encode(sessionProgressId, "UTF-8")}")
        }
    }
    navigate(extras) { popUpTo(popUpTo) { inclusive = false } }
}

data class AnalysisCallbacks(
    val onViewDeepSeek: (String, Int, Int) -> Unit,
    val onViewSpark: (String, Int, Int) -> Unit,
    val onAskDeepSeek: (String, Int, Int) -> Unit,
    val onAskSpark: (String, Int, Int) -> Unit,
    val onViewBaidu: (String, Int, Int) -> Unit,
    val onAskBaidu: (String, Int, Int) -> Unit,
    val onEditNote: (String, Int, Int) -> Unit
)

