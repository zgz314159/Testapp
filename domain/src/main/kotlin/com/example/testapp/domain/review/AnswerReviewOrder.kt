package com.example.testapp.domain.review

import com.example.testapp.domain.model.QuestionWithState

object AnswerReviewOrder {
    fun hasAnswerContent(item: QuestionWithState): Boolean =
        AnsweredBrowseOrder.hasAnswerContent(item)

    fun buildDisplayOrder(items: List<QuestionWithState>): List<Int> =
        AnsweredBrowseOrder.buildReviewDisplayOrder(items)
}
