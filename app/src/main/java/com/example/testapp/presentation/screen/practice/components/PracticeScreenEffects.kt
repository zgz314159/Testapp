package com.example.testapp.presentation.screen.practice.components



import android.util.Log

import androidx.compose.runtime.Composable

import androidx.compose.runtime.DisposableEffect

import androidx.compose.runtime.LaunchedEffect

import androidx.compose.runtime.getValue

import androidx.compose.runtime.mutableStateOf

import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope

import androidx.compose.runtime.rememberUpdatedState

import androidx.compose.runtime.setValue

import androidx.lifecycle.Lifecycle

import androidx.lifecycle.LifecycleEventObserver

import androidx.lifecycle.compose.LocalLifecycleOwner

import com.example.testapp.domain.QuestionTypes

import com.example.testapp.domain.model.Question

import com.example.testapp.presentation.screen.ai.DeepSeekViewModel

import com.example.testapp.presentation.screen.ai.SparkViewModel

import com.example.testapp.presentation.screen.practice.PracticeAutoAdvanceController

import com.example.testapp.presentation.screen.practice.PracticeQuizInitReloadPipeline

import com.example.testapp.presentation.screen.practice.PracticeOverlayAnchorHolder

import com.example.testapp.presentation.screen.practice.PracticeJumpDebugLog

import com.example.testapp.presentation.screen.practice.PracticeOverlayNavigationPipeline

import com.example.testapp.presentation.screen.practice.PracticeSessionAnalysisSyncPipeline

import com.example.testapp.presentation.screen.practice.PracticeViewModel

import com.example.testapp.presentation.screen.settings.SettingsViewModel

import com.example.testapp.presentation.viewmodel.BaiduQianfanViewModel

import com.example.testapp.uicommon.util.normalizeEditableFillAnswers

import kotlinx.coroutines.flow.first

import kotlinx.coroutines.launch



@Composable

fun PracticeScreenReviewInitEffect(

    isReviewMode: Boolean,

    reviewProgressId: String?,

    viewModel: PracticeViewModel

) {

    LaunchedEffect(isReviewMode, reviewProgressId) {

        if (isReviewMode && !reviewProgressId.isNullOrBlank()) {

            viewModel.enterReviewSession(reviewProgressId)

        }

    }

}



@Composable

fun PracticeScreenQuizInitEffect(

    quizId: String,

    isWrongBookMode: Boolean,

    wrongBookFileName: String?,

    isFavoriteMode: Boolean,

    favoriteFileName: String?,

    fillConfigVersion: String,

    practiceCount: Int,

    randomPractice: Boolean,

    isReviewMode: Boolean,

    settingsViewModel: SettingsViewModel,

    viewModel: PracticeViewModel

) {

    LaunchedEffect(

        quizId,

        isWrongBookMode,

        wrongBookFileName,

        isFavoriteMode,

        favoriteFileName,

        fillConfigVersion,

        practiceCount,

        randomPractice

    ) {

        if (isReviewMode) return@LaunchedEffect

        settingsViewModel.settingsReady.first { it }

        val count = practiceCount

        val random = randomPractice

        Log.d(

            "PracticeScreen",

            "[INIT] randomPractice=$random, isWrongBookMode=$isWrongBookMode, wrongBookFileName=$wrongBookFileName, isFavoriteMode=$isFavoriteMode, favoriteFileName=$favoriteFileName, quizId=$quizId, practiceCount=$count, fillConfig=$fillConfigVersion"

        )

        val targetProgressId = when {

            isWrongBookMode && wrongBookFileName != null -> "practice_wrongbook_${wrongBookFileName}"

            isFavoriteMode && favoriteFileName != null -> "practice_favorite_${favoriteFileName}"

            else -> "practice_${quizId}"

        }

        viewModel.setRandomPractice(random)

        val initKey = PracticeQuizInitReloadPipeline.buildInitKey(fillConfigVersion, count, random)

        if (targetProgressId == viewModel.currentProgressId && viewModel.currentProgressId.isNotBlank()) {

            if (viewModel.shouldReloadForQuizInit(initKey)) {
                viewModel.reloadForFillConfig(count, initKey)
            }

            return@LaunchedEffect

        }

        if (isWrongBookMode && wrongBookFileName != null) {

            viewModel.setProgressId(id = targetProgressId, questionsId = wrongBookFileName, loadQuestions = false, random = random)

            viewModel.loadWrongQuestions(wrongBookFileName)

        } else if (isFavoriteMode && favoriteFileName != null) {

            viewModel.setProgressId(id = targetProgressId, questionsId = favoriteFileName, loadQuestions = false, random = random)

            viewModel.loadFavoriteQuestions(favoriteFileName)

        } else {

            viewModel.setProgressId(id = quizId, questionsId = quizId, questionCount = count, random = random)

        }

    }

}



@Composable
fun rememberPracticeOverlayNavigation(
    autoAdvance: PracticeAutoAdvanceController,
    currentIndex: Int,
    questionId: Int?,
    viewModel: PracticeViewModel
): (() -> Unit) -> Unit {
    val anchorHolder = remember { PracticeOverlayAnchorHolder() }
    val latestIndex by rememberUpdatedState(currentIndex)
    val latestQuestionId by rememberUpdatedState(questionId)

    PracticeScreenOverlayPinEffect(anchorHolder, currentIndex, viewModel)
    PracticeScreenLifecycleEffect(
        autoAdvance = autoAdvance,
        anchorHolder = anchorHolder,
        currentIndex = latestIndex,
        viewModel = viewModel
    )

    return { block ->
        autoAdvance.setScreenActive(false)
        latestQuestionId?.let { id -> anchorHolder.open(latestIndex, id) }
        block()
    }
}

@Composable
fun PracticeScreenOverlayPinEffect(
    anchorHolder: PracticeOverlayAnchorHolder,
    currentIndex: Int,
    viewModel: PracticeViewModel
) {
    LaunchedEffect(currentIndex) {
        anchorHolder.shouldPin(currentIndex)?.let { pinned ->
            PracticeJumpDebugLog.overlayPinRevert(currentIndex, pinned)
            viewModel.goToQuestion(pinned, "overlayPin")
        }
    }
}

@Composable
fun PracticeScreenIndexWatchEffect(
    currentIndex: Int,
    questionId: Int?,
    showResult: Boolean
) {
    LaunchedEffect(currentIndex, questionId, showResult) {
        PracticeJumpDebugLog.indexChanged(currentIndex, questionId, showResult)
    }
}

@Composable
fun PracticeScreenLifecycleEffect(
    autoAdvance: PracticeAutoAdvanceController,
    anchorHolder: PracticeOverlayAnchorHolder,
    currentIndex: Int,
    viewModel: PracticeViewModel
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val latestIndex by rememberUpdatedState(currentIndex)

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    val anchor = if (anchorHolder.isOverlayOpen && anchorHolder.openIndex >= 0) {
                        PracticeOverlayNavigationPipeline.capture(
                            anchorHolder.openIndex,
                            anchorHolder.openQuestionId
                        )
                    } else {
                        null
                    }
                    PracticeJumpDebugLog.lifecycle("ON_PAUSE", anchor, latestIndex)
                    autoAdvance.setScreenActive(false)
                }
                Lifecycle.Event.ON_RESUME -> {
                    val anchor = if (anchorHolder.isOverlayOpen && anchorHolder.openIndex >= 0) {
                        PracticeOverlayNavigationPipeline.capture(
                            anchorHolder.openIndex,
                            anchorHolder.openQuestionId
                        )
                    } else {
                        null
                    }
                    PracticeJumpDebugLog.lifecycle("ON_RESUME", anchor, latestIndex)
                    autoAdvance.setScreenActive(true)
                    val pinnedIndex = anchorHolder.openIndex.takeIf {
                        anchorHolder.isOverlayOpen && it >= 0
                    }
                    val restore = PracticeOverlayNavigationPipeline.restoreIndex(
                        pinnedIndex?.let {
                            PracticeOverlayNavigationPipeline.capture(it, anchorHolder.openQuestionId)
                        },
                        latestIndex
                    )
                    if (restore != null) {
                        PracticeJumpDebugLog.overlayRestore(latestIndex, restore)
                        viewModel.goToQuestion(restore, "overlayRestore")
                    } else {
                        PracticeJumpDebugLog.overlayNoRestore(latestIndex, anchor)
                    }
                    anchorHolder.close()
                }
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
}



@Composable

fun PracticeScreenAnalysisSyncEffects(
    question: Question?,
    currentIndex: Int,
    showResult: Boolean,
    resultDisplayReady: Boolean,
    parsingText: String,
    analysisPair: Pair<Int, String>?,
    sparkPair: Pair<Int, String>?,
    baiduPair: Pair<Int, String>?,
    aiViewModel: DeepSeekViewModel,
    sparkViewModel: SparkViewModel,
    baiduQianfanViewModel: BaiduQianfanViewModel,
    viewModel: PracticeViewModel
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val scope = rememberCoroutineScope()
    val parsingKeyword = parsingText.removeSuffix("...")

    suspend fun syncStored() {
        val q = question ?: return
        PracticeSessionAnalysisSyncPipeline.syncStoredForQuestion(
            questionId = q.id,
            questionStem = q.content,
            index = currentIndex,
            aiViewModel = aiViewModel,
            sparkViewModel = sparkViewModel,
            baiduViewModel = baiduQianfanViewModel,
            viewModel = viewModel
        )
    }

    LaunchedEffect(currentIndex) {
        aiViewModel.clear()
        sparkViewModel.clear()
        baiduQianfanViewModel.clearResult()
    }
    LaunchedEffect(question?.id) {
        syncStored()
    }
    LaunchedEffect(currentIndex, question?.id, showResult) {
        if (showResult) syncStored()
    }
    LaunchedEffect(currentIndex, question?.id, resultDisplayReady) {
        if (resultDisplayReady) syncStored()
    }
    DisposableEffect(lifecycle, currentIndex, question?.id) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                scope.launch { syncStored() }
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(analysisPair) {

        val p = analysisPair

        if (p != null && p.second != parsingText && !p.second.startsWith(parsingKeyword)) {

            viewModel.updateAnalysis(p.first, p.second)

        }

    }

    LaunchedEffect(sparkPair) {

        val p = sparkPair

        if (p != null && p.second != parsingText && !p.second.startsWith(parsingKeyword)) {

            viewModel.updateSparkAnalysis(p.first, p.second)

        }

    }

    LaunchedEffect(baiduPair) {

        val p = baiduPair

        if (p != null && p.second != parsingText && !p.second.startsWith(parsingKeyword)) {

            viewModel.updateBaiduAnalysis(p.first, p.second)

        }

    }

}



@Composable

fun PracticeScreenEditQuestionDraftEffect(

    editableQuestion: Question?,

    onDraft: (content: String, answer: String, parts: List<String>) -> Unit

) {

    LaunchedEffect(editableQuestion?.id, editableQuestion?.content, editableQuestion?.answer) {

        val q = editableQuestion

        val content = q?.content.orEmpty()

        val answer = q?.answer.orEmpty()

        val parts = if (q?.let { QuestionTypes.isInlineBlank(it.type) } == true) {

            normalizeEditableFillAnswers(content, answer)

        } else {

            listOf(answer)

        }

        onDraft(content, answer, parts)

    }

}



@Composable

fun rememberPracticeAnsweredThisSession(progressLoaded: Boolean): Pair<Boolean, (Boolean) -> Unit> {

    var answeredThisSession by mutableStateOf(false)

    LaunchedEffect(progressLoaded) { if (progressLoaded) answeredThisSession = false }

    return answeredThisSession to { answeredThisSession = it }

}


