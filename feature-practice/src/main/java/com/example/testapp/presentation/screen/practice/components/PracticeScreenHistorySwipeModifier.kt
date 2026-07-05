package com.example.testapp.presentation.screen.practice.components

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import com.example.testapp.domain.session.SessionCommand
import com.example.testapp.presentation.screen.practice.navigation.AnsweredHistoryBackwardResult
import com.example.testapp.presentation.screen.practice.navigation.AnsweredHistoryForwardResult
import com.example.testapp.presentation.session.practice.PracticeCommandOutcome
import com.example.testapp.uicommon.design.QuestionSessionHistorySwipeDirection
import com.example.testapp.uicommon.design.QuestionSessionHistorySwipePipeline

fun Modifier.practiceHistorySwipe(
    currentIndex: Int,
    showResult: Boolean,
    inAnsweredHistory: Boolean,
    dispatchCommand: (SessionCommand) -> PracticeCommandOutcome?,
    autoAdvanceCancel: () -> Unit,
    clearFocus: () -> Unit,
    context: android.content.Context,
    atOldestText: String,
    atLatestText: String,
): Modifier = pointerInput(currentIndex) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
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
        when (QuestionSessionHistorySwipePipeline.resolve(dragX, dragY)) {
            QuestionSessionHistorySwipeDirection.Older -> {
                autoAdvanceCancel()
                clearFocus()
                val result =
                    (dispatchCommand(SessionCommand.BrowseAnsweredHistoryOlder) as? PracticeCommandOutcome.HistoryOlder)
                        ?.result
                Log.d(
                    "PracticeHistorySwipe",
                    "UI.swipeRight | idx=$currentIndex | showResult=$showResult | result=$result",
                )
                when (result) {
                    AnsweredHistoryBackwardResult.AtOldestAnswered,
                    AnsweredHistoryBackwardResult.NoMoreHistory,
                    -> {
                        if (showResult || inAnsweredHistory) {
                            Toast.makeText(context, atOldestText, Toast.LENGTH_SHORT).show()
                        }
                    }
                    else -> Unit
                }
            }
            QuestionSessionHistorySwipeDirection.Newer -> {
                autoAdvanceCancel()
                clearFocus()
                val result =
                    (dispatchCommand(SessionCommand.BrowseAnsweredHistoryNewer) as? PracticeCommandOutcome.HistoryNewer)
                        ?.result
                Log.d(
                    "PracticeHistorySwipe",
                    "UI.swipeLeft | idx=$currentIndex | showResult=$showResult | inHistory=$inAnsweredHistory | result=$result",
                )
                when (result) {
                    AnsweredHistoryForwardResult.AtLatestAnswered -> {
                        Toast.makeText(context, atLatestText, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
            null -> Unit
        }
    }
}
