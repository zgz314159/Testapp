package com.example.testapp.presentation.session.practice

import com.example.testapp.presentation.screen.practice.UnansweredNavResult
import com.example.testapp.presentation.screen.practice.navigation.AnsweredHistoryBackwardResult
import com.example.testapp.presentation.screen.practice.navigation.AnsweredHistoryForwardResult

sealed class PracticeCommandOutcome {
    data class UnansweredNav(val result: UnansweredNavResult) : PracticeCommandOutcome()

    data class HistoryOlder(val result: AnsweredHistoryBackwardResult) : PracticeCommandOutcome()

    data class HistoryNewer(val result: AnsweredHistoryForwardResult) : PracticeCommandOutcome()
}
