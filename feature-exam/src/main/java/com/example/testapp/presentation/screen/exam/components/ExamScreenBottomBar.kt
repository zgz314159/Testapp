package com.example.testapp.presentation.screen.exam.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import com.example.testapp.feature.exam.R
import com.example.testapp.presentation.screen.exam.ExamViewModel
import com.example.testapp.uicommon.component.QuestionNavigationControls

@Composable
fun ExamScreenBottomBar(
    isReviewMode: Boolean,
    showResult: Boolean,
    selectedOption: List<Int>,
    viewModel: ExamViewModel,
    onAnsweredThisSession: () -> Unit,
    onSubmit: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val fullAnswerSkipEnabled = viewModel.isFullAnswerMode && !isReviewMode
    val canSkipPrevSource = fullAnswerSkipEnabled && viewModel.canSkipToAdjacentSource(forward = false)
    val canSkipNextSource = fullAnswerSkipEnabled && viewModel.canSkipToAdjacentSource(forward = true)
    if (!isReviewMode) {
        QuestionNavigationControls(
            visible = true,
            onPrev = {
                focusManager.clearFocus(force = true)
                if (selectedOption.isNotEmpty()) onAnsweredThisSession()
                viewModel.prevQuestionViaIcon()
            },
            onNext = {
                focusManager.clearFocus(force = true)
                if (selectedOption.isNotEmpty()) onAnsweredThisSession()
                viewModel.nextQuestionViaIcon()
            },
            onPrevDoubleClick = if (fullAnswerSkipEnabled) {
                {
                    focusManager.clearFocus(force = true)
                    if (selectedOption.isNotEmpty()) onAnsweredThisSession()
                    viewModel.prevQuestionViaIconDoubleClick()
                }
            } else null,
            onNextDoubleClick = if (fullAnswerSkipEnabled) {
                {
                    focusManager.clearFocus(force = true)
                    if (selectedOption.isNotEmpty()) onAnsweredThisSession()
                    viewModel.nextQuestionViaIconDoubleClick()
                }
            } else null,
            onSubmit = if (!showResult) onSubmit else null,
            submitContentDescription = stringResource(R.string.submit_exam),
            enabledPrev = viewModel.canNavigateToPrevUnanswered() || canSkipPrevSource || showResult,
            enabledNext = viewModel.canNavigateToNextUnanswered() || canSkipNextSource || showResult
        )
    }
    if (isReviewMode) {
        QuestionNavigationControls(
            visible = true,
            onPrev = {
                focusManager.clearFocus(force = true)
                viewModel.prevQuestion()
            },
            onNext = {
                focusManager.clearFocus(force = true)
                viewModel.nextQuestion()
            },
            onSubmit = null,
            enabledPrev = viewModel.canReviewBrowseBack(),
            enabledNext = viewModel.canReviewBrowseForward()
        )
    }
}
