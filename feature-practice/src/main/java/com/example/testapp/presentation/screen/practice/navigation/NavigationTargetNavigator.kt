package com.example.testapp.presentation.screen.practice.navigation

import com.example.testapp.presentation.screen.practice.PracticeFullAnswerIconNavDebugLog

import com.example.testapp.presentation.screen.practice.PracticeJumpDebugLog

/** Applies index changes and reopen side effects for practice navigation. */
internal class NavigationTargetNavigator(
    private val env: NavigationEnvironment
) {
    fun navigateToQuestion(index: Int, reopenWrongFullAnswerRetry: Boolean = false) {
        val currentState = env.sessionState.value
        if (index !in currentState.questionsWithState.indices) {
            PracticeFullAnswerIconNavDebugLog.branch(
                forward = true,
                step = "navigateTo",
                detail = "reject outOfRange target=$index size=${currentState.questionsWithState.size}"
            )
            return
        }

        val fromIndex = currentState.currentIndex
        val targetQuestion = currentState.questionsWithState[index]
        if (reopenWrongFullAnswerRetry && env.fullAnswerRequireCorrect() &&
            targetQuestion.showResult && targetQuestion.isCorrect != true
        ) {
            PracticeFullAnswerIconNavDebugLog.navigateTo(
                forward = index > fromIndex,
                fromIndex = fromIndex,
                toIndex = index,
                reason = "reopenWrongFullAnswerRetry"
            )
            env.reopenQuestionForFullAnswerRetry(index)
            return
        }
        if (env.shouldReopenUnansweredReveal(targetQuestion)) {
            PracticeFullAnswerIconNavDebugLog.navigateTo(
                forward = index > fromIndex,
                fromIndex = fromIndex,
                toIndex = index,
                reason = "reopenUnansweredReveal"
            )
            env.reopenQuestionForPendingRetry(index)
            return
        }
        if (index != currentState.currentIndex) {
            PracticeJumpDebugLog.vmGoToQuestion(fromIndex, index, "NavigationTargetNavigator.setCurrentIndex")
            PracticeFullAnswerIconNavDebugLog.navigateTo(
                forward = index > fromIndex,
                fromIndex = fromIndex,
                toIndex = index,
                reason = "setCurrentIndex"
            )
            env.sessionState.value = currentState.copy(currentIndex = index)
            env.scheduleNavigationSave()
        }
    }
}
