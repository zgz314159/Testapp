package com.example.testapp.domain.review

import com.example.testapp.domain.model.QuestionWithState

data class ReviewPresentation(
    val questionsWithState: List<QuestionWithState>,
    val displayOrder: List<Int>
)

object SessionReviewPresentation {
    fun prepare(items: List<QuestionWithState>): ReviewPresentation {
        if (items.isEmpty()) {
            return ReviewPresentation(emptyList(), emptyList())
        }
        val displayOrder = AnsweredBrowseOrder.buildReviewDisplayOrder(items)
        val questionsWithState = items.map { item ->
            if (AnsweredBrowseOrder.hasAnswerContent(item)) item.copy(showResult = true) else item
        }
        return ReviewPresentation(questionsWithState, displayOrder)
    }
}
