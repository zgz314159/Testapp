package com.example.testapp.core.session.strategy.navigation

import com.example.testapp.domain.model.QuestionWithState
import com.example.testapp.domain.review.AnsweredBrowseOrder

/** 随机练习导航历史栈（从 NavigationHistory 收编） */
object SessionRandomNavigationHistoryPipeline {
    fun shouldTrack(randomPracticeEnabled: Boolean): Boolean = randomPracticeEnabled

    fun shouldAppendOrigin(
        history: List<Int>,
        currentIndex: Int,
    ): Boolean = history.lastOrNull() != currentIndex

    fun appendedHistory(
        history: List<Int>,
        currentIndex: Int,
    ): List<Int> = history + currentIndex

    fun seedHistoryIndices(
        questionsWithState: List<QuestionWithState>,
        currentIndex: Int,
        isQuestionAnswered: (QuestionWithState) -> Boolean,
    ): List<Int> {
        val entries =
            questionsWithState.mapIndexedNotNull { index, qws ->
                if (index == currentIndex || !isQuestionAnswered(qws)) {
                    null
                } else {
                    index to qws.sessionAnswerTime
                }
            }
        return AnsweredBrowseOrder.sortIndicesByAnswerTimeDesc(entries)
    }
}
