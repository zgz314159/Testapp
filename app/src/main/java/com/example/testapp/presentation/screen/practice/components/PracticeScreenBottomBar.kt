package com.example.testapp.presentation.screen.practice.components

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import com.example.testapp.R
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.WrongQuestion
import com.example.testapp.presentation.screen.practice.PracticeAnswerCorrectnessPipeline
import com.example.testapp.presentation.screen.practice.PracticeAutoAdvanceController
import com.example.testapp.presentation.screen.practice.PracticeSubmitFlow
import com.example.testapp.presentation.screen.practice.PracticeSubmitRevealPipeline
import com.example.testapp.presentation.screen.practice.PracticeSubmitSideEffectsPipeline
import com.example.testapp.presentation.screen.practice.PracticeViewModel
import com.example.testapp.presentation.screen.practice.UnansweredNavResult
import com.example.testapp.presentation.screen.wrongbook.WrongBookViewModel
import com.example.testapp.uicommon.component.QuestionNavigationControls
import com.example.testapp.util.rememberSoundEffects
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
    viewModel: PracticeViewModel,
    wrongBookViewModel: WrongBookViewModel,
    autoAdvance: PracticeAutoAdvanceController,
    coroutineScope: CoroutineScope,
    postAnswerAdvance: suspend () -> Unit,
    onSubmit: (Boolean) -> Unit,
    onExitWithoutAnswer: () -> Unit,
    onRequestSubmitDialog: () -> Unit,
    onAnsweredThisSession: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val soundEffects = rememberSoundEffects()
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
                    correctIndices = correctIndices
                )
                PracticeSubmitRevealPipeline.revealImmediately(answeredIndex, viewModel::revealShowResult)
                autoAdvance.schedule(
                    coroutineScope,
                    answeredIndex,
                    delaySec = if (allCorrect) correctDelay else wrongDelay,
                    revealResultFirst = false,
                    showResult = viewModel::updateShowResult,
                    onAdvance = postAnswerAdvance,
                    advanceOnly = true
                )
                coroutineScope.launch {
                    focusManager.clearFocus(force = true)
                    PracticeSubmitSideEffectsPipeline.apply(
                        allCorrect = allCorrect,
                        soundEnabled = soundEnabled,
                        playCorrect = soundEffects::playCorrect,
                        playWrong = soundEffects::playWrong,
                        onSubmit = onSubmit,
                        onWrongAnswer = {
                            if (selectedOption.isNotEmpty() || textAnswer.isNotBlank()) {
                                wrongBookViewModel.addWrongQuestion(WrongQuestion(question, selectedOption))
                            }
                        }
                    )
                }
            }
        } else {
            null
        }
        val fullAnswerSkipEnabled = viewModel.isFullAnswerMode
        val canSkipPrevSource = fullAnswerSkipEnabled && viewModel.canSkipToUnansweredSource(forward = false)
        val canSkipNextSource = fullAnswerSkipEnabled && viewModel.canSkipToUnansweredSource(forward = true)
        QuestionNavigationControls(
            visible = true,
            onPrev = {
                autoAdvance.cancel()
                focusManager.clearFocus(force = true)
                when (viewModel.prevQuestionViaIcon()) {
                    UnansweredNavResult.AtFirstUnanswered -> {
                        Toast.makeText(context, unansweredNavAtFirstText, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            },
            onNext = {
                autoAdvance.cancel()
                focusManager.clearFocus(force = true)
                when (viewModel.nextQuestionViaIcon()) {
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
                    viewModel.prevQuestionViaIconDoubleClick()
                }
            } else {
                null
            },
            onNextDoubleClick = if (fullAnswerSkipEnabled) {
                {
                    autoAdvance.cancel()
                    focusManager.clearFocus(force = true)
                    viewModel.nextQuestionViaIconDoubleClick()
                }
            } else {
                null
            },
            onSubmit = submitManualAnswer,
            onSubmitDoubleClick = ::requestPracticeSubmitDialog,
            submitContentDescription = stringResource(R.string.submit_answer),
            enabledPrev = viewModel.canNavigateToPrevUnanswered() || canSkipPrevSource ||
                showResult || inAnsweredHistory,
            enabledNext = viewModel.canNavigateToNextUnanswered() || canSkipNextSource ||
                showResult || inAnsweredHistory
        )
    }
    if (isReviewMode) {
        QuestionNavigationControls(
            visible = true,
            onPrev = { viewModel.prevQuestion() },
            onNext = { viewModel.nextQuestion() },
            onSubmit = null,
            enabledPrev = viewModel.canReviewBrowseBack(),
            enabledNext = viewModel.canReviewBrowseForward()
        )
    }
}
