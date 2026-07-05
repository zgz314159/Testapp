package com.example.testapp.presentation.screen.exam.components

import android.widget.Toast
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import com.example.testapp.domain.session.SessionCommand
import com.example.testapp.presentation.screen.exam.ExamEdgeSwipePipeline
import com.example.testapp.presentation.screen.exam.ExamGestureNavigator
import com.example.testapp.presentation.screen.exam.ExamReviewSwipeOutcome
import com.example.testapp.presentation.session.exam.ExamCommandOutcome
import com.example.testapp.presentation.session.exam.ExamScreenBindings
import com.example.testapp.uicommon.design.QuestionSessionHistorySwipeDirection
import com.example.testapp.uicommon.design.QuestionSessionHistorySwipePipeline

fun Modifier.examScreenGesture(
    currentIndex: Int,
    gesture: ExamGestureNavigator,
    timer: com.example.testapp.presentation.screen.exam.ExamAutoAdvanceTimer,
    isReviewMode: Boolean,
    answeredThisSession: Boolean,
    bindings: ExamScreenBindings,
    dispatchCommand: (SessionCommand) -> ExamCommandOutcome?,
    clearFocus: () -> Unit,
    context: android.content.Context,
    atOldestText: String,
    atLatestText: String,
    onExitWithoutAnswer: () -> Unit,
    onPromptSubmit: () -> Unit,
): Modifier = pointerInput(currentIndex, gesture.containerWidth) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        gesture.dragStartX = down.position.x
        timer.cancel()
        var dragX = 0f
        var dragY = 0f
        do {
            val event = awaitPointerEvent()
            val change = event.changes.firstOrNull { it.id == down.id } ?: break
            if (change.pressed) {
                val delta = change.positionChange()
                dragX += delta.x
                dragY += delta.y
            }
        } while (event.changes.any { it.pressed })
        gesture.dragAmount = dragX
        val swipe = QuestionSessionHistorySwipePipeline.resolve(dragX, dragY)
        if (swipe == null) {
            gesture.resetDrag()
            return@awaitEachGesture
        }
        if (isReviewMode) {
            clearFocus()
            when (swipe) {
                QuestionSessionHistorySwipeDirection.Older -> {
                    when (
                        (dispatchCommand(SessionCommand.BrowseAnsweredHistoryOlder) as? ExamCommandOutcome.ReviewHistoryOlder)
                            ?.result
                    ) {
                        ExamReviewSwipeOutcome.AtOldest,
                        ExamReviewSwipeOutcome.NoHistory,
                        -> {
                            Toast.makeText(context, atOldestText, Toast.LENGTH_SHORT).show()
                        }
                        else -> Unit
                    }
                }
                QuestionSessionHistorySwipeDirection.Newer -> {
                    when (
                        (dispatchCommand(SessionCommand.BrowseAnsweredHistoryNewer) as? ExamCommandOutcome.ReviewHistoryNewer)
                            ?.result
                    ) {
                        ExamReviewSwipeOutcome.AtLatest -> {
                            Toast.makeText(context, atLatestText, Toast.LENGTH_SHORT).show()
                        }
                        else -> Unit
                    }
                }
            }
        } else if (
            (gesture.dragStartX < 20f && swipe == QuestionSessionHistorySwipeDirection.Older) ||
            (gesture.dragStartX > gesture.containerWidth - 20f && swipe == QuestionSessionHistorySwipeDirection.Newer)
        ) {
            clearFocus()
            when {
                !answeredThisSession -> onExitWithoutAnswer()
                else -> onPromptSubmit()
            }
        } else when (swipe) {
            QuestionSessionHistorySwipeDirection.Older -> {
                if (bindings.canGoPrevSequential()) {
                    clearFocus()
                    dispatchCommand(SessionCommand.NavPrevSequential)
                }
            }
            QuestionSessionHistorySwipeDirection.Newer -> {
                clearFocus()
                if (bindings.canGoNextSequential()) {
                    dispatchCommand(SessionCommand.NavNextSequential)
                } else {
                    when (
                        ExamEdgeSwipePipeline.resolveForwardSwipe(
                            answeredThisSession = answeredThisSession,
                            canNavigateNext = false,
                        )
                    ) {
                        ExamEdgeSwipePipeline.ForwardAction.ExitWithoutAnswer -> onExitWithoutAnswer()
                        ExamEdgeSwipePipeline.ForwardAction.PromptSubmit -> onPromptSubmit()
                        else -> Unit
                    }
                }
            }
        }
        gesture.resetDrag()
    }
}
