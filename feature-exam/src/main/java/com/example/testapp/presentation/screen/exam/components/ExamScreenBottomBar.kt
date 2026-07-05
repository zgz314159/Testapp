package com.example.testapp.presentation.screen.exam.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import com.example.testapp.domain.session.SessionCommand
import com.example.testapp.feature.exam.R
import com.example.testapp.presentation.session.exam.ExamScreenBindings
import com.example.testapp.uicommon.component.QuestionNavigationControls

@Composable
fun ExamScreenBottomBar(
    isReviewMode: Boolean,
    showResult: Boolean,
    selectedOption: List<Int>,
    bindings: ExamScreenBindings,
    dispatchCommand: (SessionCommand) -> Unit,
    onAnsweredThisSession: () -> Unit,
    onSubmit: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val fullAnswerSkipEnabled = bindings.isFullAnswerMode && !isReviewMode
    val canSkipPrevSource = fullAnswerSkipEnabled && bindings.canSkipToAdjacentSource(forward = false)
    val canSkipNextSource = fullAnswerSkipEnabled && bindings.canSkipToAdjacentSource(forward = true)
    if (!isReviewMode) {
        QuestionNavigationControls(
            visible = true,
            onPrev = {
                focusManager.clearFocus(force = true)
                if (selectedOption.isNotEmpty()) onAnsweredThisSession()
                dispatchCommand(SessionCommand.NavPrevIcon)
            },
            onNext = {
                focusManager.clearFocus(force = true)
                if (selectedOption.isNotEmpty()) onAnsweredThisSession()
                dispatchCommand(SessionCommand.NavNextIcon)
            },
            onPrevDoubleClick = if (fullAnswerSkipEnabled) {
                {
                    focusManager.clearFocus(force = true)
                    if (selectedOption.isNotEmpty()) onAnsweredThisSession()
                    dispatchCommand(SessionCommand.NavPrevIconDoubleClick)
                }
            } else {
                null
            },
            onNextDoubleClick = if (fullAnswerSkipEnabled) {
                {
                    focusManager.clearFocus(force = true)
                    if (selectedOption.isNotEmpty()) onAnsweredThisSession()
                    dispatchCommand(SessionCommand.NavNextIconDoubleClick)
                }
            } else {
                null
            },
            onSubmit = if (!showResult) onSubmit else null,
            submitContentDescription = stringResource(R.string.submit_exam),
            enabledPrev = bindings.canNavigateToPrevUnanswered() || canSkipPrevSource || showResult,
            enabledNext = bindings.canNavigateToNextUnanswered() || canSkipNextSource || showResult,
        )
    }
    if (isReviewMode) {
        QuestionNavigationControls(
            visible = true,
            onPrev = {
                focusManager.clearFocus(force = true)
                dispatchCommand(SessionCommand.PrevQuestion)
            },
            onNext = {
                focusManager.clearFocus(force = true)
                dispatchCommand(SessionCommand.NextQuestion)
            },
            onSubmit = null,
            enabledPrev = bindings.canReviewBrowseBack(),
            enabledNext = bindings.canReviewBrowseForward(),
        )
    }
}
