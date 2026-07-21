package com.example.testapp.presentation.screen.practice.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.session.SessionCommand
import com.example.testapp.presentation.screen.practice.PracticeAutoAdvanceController
import com.example.testapp.presentation.screen.practice.PracticeOverlayAnchorHolder
import com.example.testapp.presentation.screen.practice.PracticeOverlayNavigationPipeline
import com.example.testapp.presentation.screen.practice.PracticeQuizInitReloadPipeline
import com.example.testapp.presentation.session.practice.PracticeScreenBindings
import com.example.testapp.uicommon.util.normalizeEditableFillAnswers

@Composable
fun PracticeScreenReviewInitEffect(
    isReviewMode: Boolean,
    reviewProgressId: String?,
    bindings: PracticeScreenBindings,
    sendCommand: (SessionCommand) -> Unit,
    sessionHosted: Boolean = false,
) {
    if (sessionHosted) return
    LaunchedEffect(isReviewMode, reviewProgressId) {
        if (isReviewMode && !reviewProgressId.isNullOrBlank()) {
            sendCommand(SessionCommand.EnterReviewSession(reviewProgressId))
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
    settingsReady: Boolean,
    bindings: PracticeScreenBindings,
    sendCommand: (SessionCommand) -> Unit,
    sessionHosted: Boolean = false,
) {
    if (sessionHosted) return
    LaunchedEffect(
        quizId,
        isWrongBookMode,
        wrongBookFileName,
        isFavoriteMode,
        favoriteFileName,
        fillConfigVersion,
        practiceCount,
        randomPractice,
        settingsReady,
    ) {
        if (isReviewMode || !settingsReady) return@LaunchedEffect

        val count = practiceCount
        val random = randomPractice


        val targetProgressId = when {
            isWrongBookMode && wrongBookFileName != null -> "practice_wrongbook_$wrongBookFileName"
            isFavoriteMode && favoriteFileName != null -> "practice_favorite_$favoriteFileName"
            else -> "practice_$quizId"
        }

        sendCommand(SessionCommand.SetRandomPractice(random))

        val initKey = PracticeQuizInitReloadPipeline.buildInitKey(fillConfigVersion, count, random)

        if (targetProgressId == bindings.currentProgressId && bindings.currentProgressId.isNotBlank()) {
            if (bindings.shouldReloadForQuizInit(initKey)) {
                sendCommand(SessionCommand.ReloadForFillConfig(count, initKey))
            }
            return@LaunchedEffect
        }

        if (isWrongBookMode && wrongBookFileName != null) {
            sendCommand(
                SessionCommand.SetProgressId(
                    id = targetProgressId,
                    questionsId = wrongBookFileName,
                    loadQuestions = false,
                    random = random,
                ),
            )
            sendCommand(SessionCommand.LoadWrongQuestions(wrongBookFileName))
        } else if (isFavoriteMode && favoriteFileName != null) {
            sendCommand(
                SessionCommand.SetProgressId(
                    id = targetProgressId,
                    questionsId = favoriteFileName,
                    loadQuestions = false,
                    random = random,
                ),
            )
            sendCommand(SessionCommand.LoadFavoriteQuestions(favoriteFileName))
        } else {
            sendCommand(
                SessionCommand.SetProgressId(
                    id = quizId,
                    questionsId = quizId,
                    questionCount = count,
                    random = random,
                ),
            )
        }
    }
}

@Composable
fun rememberPracticeOverlayNavigation(
    autoAdvance: PracticeAutoAdvanceController,
    currentIndex: Int,
    questionId: Int?,
    bindings: PracticeScreenBindings,
    sendCommand: (SessionCommand) -> Unit,
): (() -> Unit) -> Unit {
    val anchorHolder = remember { PracticeOverlayAnchorHolder() }
    val latestIndex by rememberUpdatedState(currentIndex)
    val latestQuestionId by rememberUpdatedState(questionId)

    PracticeScreenOverlayPinEffect(anchorHolder, currentIndex, sendCommand)
    PracticeScreenLifecycleEffect(
        autoAdvance = autoAdvance,
        anchorHolder = anchorHolder,
        currentIndex = latestIndex,
        sendCommand = sendCommand,
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
    sendCommand: (SessionCommand) -> Unit,
) {
    LaunchedEffect(currentIndex) {
        anchorHolder.shouldPin(currentIndex)?.let { pinned ->
            sendCommand(SessionCommand.GoToQuestion(pinned, "overlayPin"))
        }
    }
}

@Composable
fun PracticeScreenIndexWatchEffect(
    currentIndex: Int,
    questionId: Int?,
    showResult: Boolean,
) {
    LaunchedEffect(currentIndex, questionId, showResult) {
    }
}

@Composable
fun PracticeScreenLifecycleEffect(
    autoAdvance: PracticeAutoAdvanceController,
    anchorHolder: PracticeOverlayAnchorHolder,
    currentIndex: Int,
    sendCommand: (SessionCommand) -> Unit,
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
                            anchorHolder.openQuestionId,
                        )
                    } else {
                        null
                    }
                    autoAdvance.setScreenActive(false)
                }
                Lifecycle.Event.ON_RESUME -> {
                    val anchor = if (anchorHolder.isOverlayOpen && anchorHolder.openIndex >= 0) {
                        PracticeOverlayNavigationPipeline.capture(
                            anchorHolder.openIndex,
                            anchorHolder.openQuestionId,
                        )
                    } else {
                        null
                    }
                    autoAdvance.setScreenActive(true)
                    val pinnedIndex = anchorHolder.openIndex.takeIf {
                        anchorHolder.isOverlayOpen && it >= 0
                    }
                    val restore = PracticeOverlayNavigationPipeline.restoreIndex(
                        pinnedIndex?.let {
                            PracticeOverlayNavigationPipeline.capture(it, anchorHolder.openQuestionId)
                        },
                        latestIndex,
                    )
                    if (restore != null) {
                        sendCommand(SessionCommand.GoToQuestion(restore, "overlayRestore"))
                    } else {
                    }
                    anchorHolder.close()
                }
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
}

@Composable
fun PracticeScreenEditQuestionDraftEffect(
    editableQuestion: Question?,
    onDraft: (content: String, answer: String, parts: List<String>) -> Unit,
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
