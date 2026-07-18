package com.example.testapp.presentation.screen.exam

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.core.common.LocalizedResult
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.session.SessionCommand
import com.example.testapp.feature.exam.R
import com.example.testapp.presentation.screen.ai.DeepSeekViewModel
import com.example.testapp.presentation.screen.ai.SparkViewModel
import com.example.testapp.presentation.screen.settings.SettingsViewModel
import com.example.testapp.presentation.screen.shared.rememberSessionAnalysisLoader
import com.example.testapp.presentation.session.exam.ExamScreenBindings
import com.example.testapp.presentation.viewmodel.BaiduQianfanViewModel
import com.example.testapp.uicommon.util.formatQuestionForAi

@Composable
fun ExamScreen(
    quizId: String,
    isWrongBookMode: Boolean = false,
    wrongBookFileName: String? = null,
    isFavoriteMode: Boolean = false,
    favoriteFileName: String? = null,
    isReviewMode: Boolean = false,
    reviewProgressId: String? = null,
    onReviewBack: () -> Unit = {},
    bindings: ExamScreenBindings,
    sessionHosted: Boolean = false,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    aiViewModel: DeepSeekViewModel = hiltViewModel(),
    sparkViewModel: SparkViewModel = hiltViewModel(),
    isQuestionFavorite: (Int) -> Boolean = { false },
    onToggleQuestionFavorite: (Question) -> Unit = {},
    onExamEnd:
    (score: Int, total: Int, unanswered: Int, cumulativeCorrect: Int?, cumulativeAnswered: Int?, cumulativeExamCount: Int?) -> Unit = { _, _, _, _, _, _ -> },
    onExitWithoutAnswer: () -> Unit = {},
    onViewDeepSeek: (String, Int, Int) -> Unit = { _, _, _ -> },
    onViewSpark: (String, Int, Int) -> Unit = { _, _, _ -> },
    onAskDeepSeek: (String, Int, Int) -> Unit = { _, _, _ -> },
    onAskSpark: (String, Int, Int) -> Unit = { _, _, _ -> },
    onViewBaidu: (String, Int, Int) -> Unit = { _, _, _ -> },
    onAskBaidu: (String, Int, Int) -> Unit = { _, _, _ -> },
    onViewExplanation: (String) -> Unit = {},
    onEditCorrectAnswer: (String, Int, Int) -> Unit = { _, _, _ -> },
    onEditNote: (String, Int, Int) -> Unit = { _, _, _ -> },
) {
    val context = LocalContext.current
    val baiduQianfanViewModel: BaiduQianfanViewModel = hiltViewModel()

    val examCount by settingsViewModel.examQuestionCount.collectAsState()
    val randomExam by settingsViewModel.randomExam.collectAsState()
    val fillQuestionGenerationMode by settingsViewModel.fillQuestionGenerationMode.collectAsState()
    val fillBlankCount by settingsViewModel.fillBlankCount.collectAsState()
    val fillFullAnswerRandomOrder by settingsViewModel.fillFullAnswerRandomOrder.collectAsState()
    val fillFullAnswerRequireCorrect by settingsViewModel.fillFullAnswerRequireCorrect.collectAsState()
    val fillAnswerScoreMin by settingsViewModel.fillAnswerScoreMin.collectAsState()
    val fillAnswerScoreMax by settingsViewModel.fillAnswerScoreMax.collectAsState()
    val fillAnswerTagFilter by settingsViewModel.fillAnswerTagFilter.collectAsState()
    val fillConfigVersion = listOf(
        fillQuestionGenerationMode.storageValue,
        fillBlankCount,
        fillFullAnswerRandomOrder,
        fillFullAnswerRequireCorrect,
        fillAnswerScoreMin,
        fillAnswerScoreMax,
        fillAnswerTagFilter,
    ).joinToString("|")
    val examMemoryMode by settingsViewModel.examMemoryMode.collectAsState()
    val examMemoryBatchSize by settingsViewModel.examMemoryBatchSize.collectAsState()
    val examMemoryWrongMode by settingsViewModel.examMemoryWrongMode.collectAsState()
    val examMemoryPoolMode by settingsViewModel.examMemoryPoolMode.collectAsState()
    val fontSize by settingsViewModel.fontSize.collectAsState()
    val examDelay by settingsViewModel.examDelay.collectAsState()

    LaunchedEffect(Unit) { settingsViewModel.loadFontSettings() }
    if (!isReviewMode && !sessionHosted) {
        LaunchedEffect(quizId) { bindings.resetLoadState() }
    }

    val analysisPair by aiViewModel.analysis.collectAsState()
    val sparkPair by sparkViewModel.analysis.collectAsState()
    val chatGptResult by baiduQianfanViewModel.analysisResult.collectAsState()
    val chatGptLoading by baiduQianfanViewModel.loading.collectAsState()
    val questions by bindings.questions.collectAsState()
    val currentIndex by bindings.currentIndex.collectAsState()
    val question = questions.getOrNull(currentIndex)

    val questionTextForAi = remember(question) { question?.let(::formatQuestionForAi).orEmpty() }
    val isFavorite = remember(question?.id) {
        question?.id?.let(isQuestionFavorite) ?: false
    }

    fun resolveLocalized(res: LocalizedResult?): String {
        return res?.let { r ->
            val resId = context.resources.getIdentifier(r.key, "string", context.packageName)
            if (resId != 0) {
                try {
                    context.getString(resId, *r.args.toTypedArray())
                } catch (e: Exception) {
                    if (r.args.isEmpty()) r.key else r.key + " " + r.args.joinToString(",")
                }
            } else {
                r.key
            }
        } ?: ""
    }

    val dispatchCommand = rememberExamCommandDispatcher(bindings)
    val sendCommand: (SessionCommand) -> Unit = remember(bindings) {
        { command -> dispatchCommand(command) }
    }

    ExamScreenContent(
        quizId = quizId,
        bindings = bindings,
        dispatchCommand = dispatchCommand,
        sessionHosted = sessionHosted,
        isReviewMode = isReviewMode,
        reviewProgressId = reviewProgressId,
        isWrongBookMode = isWrongBookMode,
        isFavoriteMode = isFavoriteMode,
        onReviewBack = onReviewBack,
        externalState = ExternalExamState(
            examCount = examCount,
            randomExam = randomExam,
            fillConfigVersion = fillConfigVersion,
            fillGenerationMode = fillQuestionGenerationMode,
            examMemoryMode = examMemoryMode,
            examMemoryBatchSize = examMemoryBatchSize,
            examMemoryWrongMode = examMemoryWrongMode,
            examMemoryPoolMode = examMemoryPoolMode,
            fontSize = fontSize,
            examDelay = examDelay.toLong(),
            isFavorite = isFavorite,
            onToggleFavorite = { question?.let(onToggleQuestionFavorite) },
            analysisPair = analysisPair,
            sparkPair = sparkPair,
            chatGptResult = chatGptResult,
            resolveLocalized = ::resolveLocalized,
        ),
        onExamEnd = onExamEnd,
        onExitWithoutAnswer = onExitWithoutAnswer,
        onViewDeepSeek = onViewDeepSeek,
        onViewSpark = onViewSpark,
        onAskDeepSeek = onAskDeepSeek,
        onAskSpark = onAskSpark,
        onViewBaidu = onViewBaidu,
        onAskBaidu = onAskBaidu,
        onViewExplanation = onViewExplanation,
        onEditCorrectAnswer = onEditCorrectAnswer,
        onEditNote = onEditNote,
    )

    val parsingText = stringResource(R.string.parsing)
    val showResultList by bindings.showResultList.collectAsState()
    ExamAISyncEffects(
        currentIndex = currentIndex,
        questionId = question?.id,
        questionStem = question?.content.orEmpty(),
        showResultForIndex = showResultList.getOrNull(currentIndex),
        pollingText = parsingText,
        analysisPair = analysisPair,
        sparkPair = sparkPair,
        chatGptResult = chatGptResult,
        aiViewModel = aiViewModel,
        sparkViewModel = sparkViewModel,
        baiduQianfanViewModel = baiduQianfanViewModel,
        dispatchCommand = sendCommand,
        loader = rememberSessionAnalysisLoader(),
    )
}
