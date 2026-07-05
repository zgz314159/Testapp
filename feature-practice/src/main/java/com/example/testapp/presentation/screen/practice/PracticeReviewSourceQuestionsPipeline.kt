package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.Question

/** 错题 / 收藏复习模式题库过滤（纯逻辑） */
object PracticeReviewSourceQuestionsPipeline {

    fun filterBySource(
        sourceId: String,
        wrongBook: Boolean,
        favorite: Boolean,
        wrongBookQuestions: List<Question>,
        favoriteQuestions: List<Question>,
    ): List<Question>? {
        if (!wrongBook && !favorite) return null
        return when {
            wrongBook -> wrongBookQuestions.filter { it.fileName == sourceId }
            favorite -> favoriteQuestions.filter { it.fileName == sourceId }
            else -> null
        }
    }
}
