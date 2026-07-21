package com.example.testapp.presentation.screen.practice.navigation


/** Applies index changes and reopen side effects for practice navigation. */
internal class NavigationTargetNavigator(
    private val env: NavigationEnvironment
) {
    fun navigateToQuestion(index: Int, reopenWrongFullAnswerRetry: Boolean = false) {
        val currentState = env.sessionState.value
        if (index !in currentState.questionsWithState.indices) {
            return
        }

        val fromIndex = currentState.currentIndex
        val targetQuestion = currentState.questionsWithState[index]
        if (reopenWrongFullAnswerRetry && env.fullAnswerRequireCorrect() &&
            targetQuestion.showResult && targetQuestion.isCorrect != true
        ) {
            env.reopenQuestionForFullAnswerRetry(index)
            return
        }
        if (env.shouldReopenUnansweredReveal(targetQuestion)) {
            env.reopenQuestionForPendingRetry(index)
            return
        }
        if (index != currentState.currentIndex) {
            env.sessionState.value = currentState.copy(currentIndex = index)
            env.scheduleNavigationSave()
        }
    }
}
