package com.example.testapp.presentation.screen.practice

import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.core.common.LocalizedResult
import com.example.testapp.core.util.FavoriteSessionPipeline
import com.example.testapp.domain.model.WrongQuestion
import com.example.testapp.domain.session.SessionCommand
import com.example.testapp.feature.practice.R
import com.example.testapp.presentation.screen.ai.DeepSeekViewModel
import com.example.testapp.presentation.screen.ai.SparkViewModel
import com.example.testapp.presentation.screen.favorite.FavoriteViewModel
import com.example.testapp.presentation.screen.settings.SettingsViewModel
import com.example.testapp.presentation.screen.shared.rememberSessionAnalysisLoader
import com.example.testapp.presentation.screen.wrongbook.WrongBookViewModel
import com.example.testapp.presentation.session.practice.PracticeScreenBindings
import com.example.testapp.presentation.viewmodel.BaiduQianfanViewModel
import com.example.testapp.uicommon.util.rememberSoundEffects

@Composable
fun PracticeScreen(
    quizId: String = "default",
    isWrongBookMode: Boolean = false,
    wrongBookFileName: String? = null,
    isFavoriteMode: Boolean = false,
    favoriteFileName: String? = null,
    isReviewMode: Boolean = false,
    reviewProgressId: String? = null,
    onReviewBack: () -> Unit = {},
    bindings: PracticeScreenBindings,
    sessionHosted: Boolean = false,
    persistentQuestionActionsEnabled: Boolean = true,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    aiViewModel: DeepSeekViewModel = hiltViewModel(),
    sparkViewModel: SparkViewModel = hiltViewModel(),
    baiduQianfanViewModel: BaiduQianfanViewModel = hiltViewModel(),
    @RawRes correctSoundResId: Int = 0,
    @RawRes wrongSoundResId: Int = 0,
    onQuizEnd: (score: Int, total: Int, unanswered: Int, cumulativeCorrect: Int?, cumulativeAnswered: Int?) -> Unit = { _, _, _, _, _ -> },
    onSubmit: (Boolean) -> Unit = {},
    onExitWithoutAnswer: () -> Unit = {},
    onViewDeepSeek: (String, Int, Int) -> Unit = { _, _, _ -> },
    onViewSpark: (String, Int, Int) -> Unit = { _, _, _ -> },
    onViewBaidu: (String, Int, Int) -> Unit = { _, _, _ -> },
    onAskDeepSeek: (String, Int, Int) -> Unit = { _, _, _ -> },
    onAskSpark: (String, Int, Int) -> Unit = { _, _, _ -> },
    onAskBaidu: (String, Int, Int) -> Unit = { _, _, _ -> },
    onViewExplanation: (String) -> Unit = {},
    onEditNote: (String, Int, Int) -> Unit = { _, _, _ -> },
) {
    val randomPractice by settingsViewModel.randomPractice.collectAsState()
    val practiceCount by settingsViewModel.practiceQuestionCount.collectAsState()
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
    val settingsReady by settingsViewModel.settingsReady.collectAsState()
    val fontSize by settingsViewModel.fontSize.collectAsState()
    val correctDelay by settingsViewModel.correctDelay.collectAsState()
    val wrongDelay by settingsViewModel.wrongDelay.collectAsState()
    val soundEnabled by settingsViewModel.soundEnabled.collectAsState()
    val soundEffects = rememberSoundEffects(correctSoundResId, wrongSoundResId)

    val wrongBookViewModel: WrongBookViewModel = hiltViewModel()
    val favoriteViewModel: FavoriteViewModel = hiltViewModel()
    if (persistentQuestionActionsEnabled) {
        LaunchedEffect(Unit) { favoriteViewModel.ensureFullListLoaded() }
    }

    val questions by bindings.questions.collectAsState()
    val currentIndex by bindings.currentIndex.collectAsState()
    val question = questions.getOrNull(currentIndex)
    val analysisPair by aiViewModel.analysis.collectAsState()
    val sparkPair by sparkViewModel.analysis.collectAsState()
    val baiduPair by baiduQianfanViewModel.analysisResult.collectAsState()
    val favoriteQuestions by favoriteViewModel.favoriteQuestions.collectAsState()
    val isFavorite = remember(question?.id, favoriteQuestions, persistentQuestionActionsEnabled) {
        persistentQuestionActionsEnabled &&
            (question?.id?.let { FavoriteSessionPipeline.isFavorite(it, favoriteQuestions) } ?: false)
    }
    val showResultList by bindings.showResultList.collectAsState()
    val showResult = showResultList.getOrNull(currentIndex) == true
    val resultDisplayReady = rememberPracticeResultDisplayReady(showResult, currentIndex)
    val parsingText = stringResource(R.string.parsing)
    val chatGptResult by baiduQianfanViewModel.analysisResult.collectAsState()
    val chatGptLoading by baiduQianfanViewModel.loading.collectAsState()

    val dispatchCommand = rememberPracticeCommandDispatcher(bindings)
    val sendCommand: (SessionCommand) -> Unit = remember(bindings) {
        { command -> dispatchPracticeCommand(bindings, command) }
    }

    if (persistentQuestionActionsEnabled) {
        PracticeAISyncEffects(
            question = question,
            currentIndex = currentIndex,
            showResult = showResult,
            resultDisplayReady = resultDisplayReady,
            parsingText = parsingText,
            analysisPair = analysisPair,
            sparkPair = sparkPair,
            baiduPair = baiduPair,
            aiViewModel = aiViewModel,
            sparkViewModel = sparkViewModel,
            baiduQianfanViewModel = baiduQianfanViewModel,
            dispatchCommand = sendCommand,
            loader = rememberSessionAnalysisLoader(),
        )
    }

    PracticeScreenContent(
        quizId = quizId,
        isWrongBookMode = isWrongBookMode,
        wrongBookFileName = wrongBookFileName,
        isFavoriteMode = isFavoriteMode,
        favoriteFileName = favoriteFileName,
        isReviewMode = isReviewMode,
        reviewProgressId = reviewProgressId,
        onReviewBack = onReviewBack,
        bindings = bindings,
        dispatchCommand = dispatchCommand,
        sessionHosted = sessionHosted,
        persistentQuestionActionsEnabled = persistentQuestionActionsEnabled,
        externalState = ExternalPracticeState(
            randomPractice = randomPractice,
            practiceCount = practiceCount,
            fillConfigVersion = fillConfigVersion,
            fontSize = fontSize,
            correctDelay = correctDelay,
            wrongDelay = wrongDelay,
            soundEnabled = soundEnabled,
            settingsReady = settingsReady,
            isFavorite = isFavorite,
            onToggleFavorite = {
                if (persistentQuestionActionsEnabled) question?.let { q ->
                    if (isFavorite) favoriteViewModel.removeFavorite(q.id) else favoriteViewModel.addFavorite(q)
                }
            },
            analysisPair = analysisPair,
            sparkPair = sparkPair,
            baiduPair = baiduPair,
            playCorrect = soundEffects::playCorrect,
            playWrong = soundEffects::playWrong,
            onWrongAnswer = { q, options ->
                if (persistentQuestionActionsEnabled) {
                    wrongBookViewModel.addWrongQuestion(WrongQuestion(q, options))
                }
            },
            onClearDeepSeek = { aiViewModel.clear() },
            onClearSpark = { sparkViewModel.clear() },
            onClearBaidu = { baiduQianfanViewModel.clearResult() },
            chatGptLoading = chatGptLoading,
            chatGptResult = chatGptResult?.let {
                Pair(it.first, LocalizedResult(it.second ?: "", emptyList()))
            },
        ),
        onQuizEnd = onQuizEnd,
        onSubmit = onSubmit,
        onExitWithoutAnswer = onExitWithoutAnswer,
        onViewDeepSeek = onViewDeepSeek,
        onViewSpark = onViewSpark,
        onViewBaidu = onViewBaidu,
        onAskDeepSeek = onAskDeepSeek,
        onAskSpark = onAskSpark,
        onAskBaidu = onAskBaidu,
        onViewExplanation = onViewExplanation,
        onEditNote = onEditNote,
    )
}
