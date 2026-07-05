package com.example.testapp.presentation.screen.practice.components

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import com.example.testapp.feature.practice.R
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.session.SessionCommand
import com.example.testapp.presentation.screen.practice.PracticeAnswerCorrectnessPipeline
import com.example.testapp.presentation.screen.practice.PracticeAutoAdvanceController
import com.example.testapp.presentation.screen.practice.PracticeSubmitFlow
import com.example.testapp.presentation.screen.practice.PracticeSubmitSideEffectsPipeline
import com.example.testapp.presentation.screen.practice.UnansweredNavResult
import com.example.testapp.presentation.session.practice.PracticeCommandOutcome
import com.example.testapp.presentation.session.practice.PracticeScreenBindings
import com.example.testapp.uicommon.component.QuestionNavigationControls
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun PracticeScreenBottomBar(
    isReviewMode: Boolean,
    showResult: Boolean,
    inAnsweredHistory: Boolean,
    answeredThisSession: Boolean,
    hasSessionInput: Boolean,
    currentIndex: Int,
    question: Question,
    textAnswer: String,
    selectedOption: List<Int>,
    resolvedFillAnswer: String,
    correctIndices: List<Int>,
    correctDelay: Int,
    wrongDelay: Int,
    soundEnabled: Boolean,
    bindings: PracticeScreenBindings,
    dispatchCommand: (SessionCommand) -> PracticeCommandOutcome?,
    sendCommand: (SessionCommand) -> Unit,
    playCorrect: () -> Unit,
    playWrong: () -> Unit,
    onWrongAnswer: (Question, List<Int>) -> Unit,
    autoAdvance: PracticeAutoAdvanceController,
    coroutineScope: CoroutineScope,
    postAnswerAdvance: suspend () -> Unit,
    onSubmit: (Boolean) -> Unit,
    onExitWithoutAnswer: () -> Unit,
    onRequestSubmitDialog: () -> Unit,
    onAnsweredThisSession: () -> Unit,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val unansweredNavAtFirstText = stringResource(R.string.unanswered_nav_at_first)
    val unansweredNavAtLastText = stringResource(R.string.unanswered_nav_at_last)

    if (!isReviewMode) {
        fun requestPracticeSubmitDialog() {
            autoAdvance.cancel()
            focusManager.clearFocus(force = true)
            when (PracticeSubmitFlow.resolve(answeredThisSession, hasSessionInput)) {
                PracticeSubmitFlow.Action.ExitWithoutAnswer -> onExitWithoutAnswer()
                PracticeSubmitFlow.Action.ShowSubmitDialog -> onRequestSubmitDialog()
            }
        }
        val submitManualAnswer: (() -> Unit)? = if (!showResult && !inAnsweredHistory) {
            {
                autoAdvance.cancel()
                onAnsweredThisSession()
                val answeredIndex = currentIndex
                val allCorrect = PracticeAnswerCorrectnessPipeline.isAllCorrect(
                    question = question,
                    textAnswer = textAnswer,
                    selectedOptions = selectedOption,
                    resolvedFillAnswer = resolvedFillAnswer,
                    correctIndices = correctIndices,
                )
                sendCommand(SessionCommand.RevealAnswer(answeredIndex))
                autoAdvance.schedule(
                    coroutineScope,
                    answeredIndex,
                    delaySec = if (allCorrect) correctDelay else wrongDelay,
                    revealResultFirst = false,
                    showResult = { index, value -> sendCommand(SessionCommand.SetShowResult(index, value)) },
                    onAdvance = postAnswerAdvance,
                    advanceOnly = true,
                )
                coroutineScope.launch {
                    focusManager.clearFocus(force = true)
                    PracticeSubmitSideEffectsPipeline.apply(
                        allCorrect = allCorrect,
                        soundEnabled = soundEnabled,
                        playCorrect = playCorrect,
                        playWrong = playWrong,
                        onSubmit = onSubmit,
                        onWrongAnswer = {
                            if (selectedOption.isNotEmpty() || textAnswer.isNotBlank()) {
                                onWrongAnswer(question, selectedOption)
                            }
                        },
                    )
                }
            }
        } else {
            null
        }
        val fullAnswerSkipEnabled = bindings.isFullAnswerMode
        val canSkipPrevSource = fullAnswerSkipEnabled && bindings.canSkipToUnansweredSource(forward = false)
        val canSkipNextSource = fullAnswerSkipEnabled && bindings.canSkipToUnansweredSource(forward = true)
        QuestionNavigationControls(
            visible = true,
            onPrev = {
                autoAdvance.cancel()
                focusManager.clearFocus(force = true)
                when (
                    (dispatchCommand(SessionCommand.NavPrevIcon) as? PracticeCommandOutcome.UnansweredNav)?.result
                ) {
                    UnansweredNavResult.AtFirstUnanswered -> {
                        Toast.makeText(context, unansweredNavAtFirstText, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            },
            onNext = {
                autoAdvance.cancel()
                focusManager.clearFocus(force = true)
                when (
                    (dispatchCommand(SessionCommand.NavNextIcon) as? PracticeCommandOutcome.UnansweredNav)?.result
                ) {
                    UnansweredNavResult.AtLastUnanswered -> {
                        Toast.makeText(context, unansweredNavAtLastText, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            },
            onPrevDoubleClick = if (fullAnswerSkipEnabled) {
                {
                    autoAdvance.cancel()
                    focusManager.clearFocus(force = true)
                    dispatchCommand(SessionCommand.NavPrevIconDoubleClick)
                }
            } else {
                null
            },
            onNextDoubleClick = if (fullAnswerSkipEnabled) {
                {
                    autoAdvance.cancel()
                    focusManager.clearFocus(force = true)
                    dispatchCommand(SessionCommand.NavNextIconDoubleClick)
                }
            } else {
                null
            },
            onSubmit = submitManualAnswer,
            onSubmitDoubleClick = ::requestPracticeSubmitDialog,
            submitContentDescription = stringResource(R.string.submit_answer),
            enabledPrev = bindings.canNavigateToPrevUnanswered() || canSkipPrevSource ||
                showResult || inAnsweredHistory,
            enabledNext = bindings.canNavigateToNextUnanswered() || canSkipNextSource ||
                showResult || inAnsweredHistory,
        )
    }
    if (isReviewMode) {
        QuestionNavigationControls(
            visible = true,
            onPrev = { dispatchCommand(SessionCommand.PrevQuestion) },
            onNext = { dispatchCommand(SessionCommand.NextQuestion) },
            onSubmit = null,
            enabledPrev = bindings.canReviewBrowseBack(),
            enabledNext = bindings.canReviewBrowseForward(),
        )
    }
}
