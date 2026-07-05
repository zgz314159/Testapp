package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.Question

/** 题库抽屉点题进入练习：保证指定题目出现在本轮会话中。 */
object PracticePinnedQuestionPipeline {
    fun ensurePinned(
        ordered: List<Question>,
        catalog: List<Question>,
        pinnedQuestionId: Int?,
        questionCount: Int,
    ): List<Question> {
        val pinnedId = pinnedQuestionId?.takeIf { it > 0 } ?: return ordered
        if (ordered.any { it.id == pinnedId }) return ordered
        val pinned = catalog.firstOrNull { it.id == pinnedId } ?: return ordered
        val base =
            if (questionCount > 0 && ordered.size >= questionCount) {
                ordered.dropLast(1)
            } else {
                ordered
            }
        return PracticeQuestionCountPolicy.limitQuestions(
            listOf(pinned) + base.filter { it.id != pinnedId },
            questionCount,
        )
    }

    fun indexInSession(
        questions: List<Question>,
        questionId: Int,
    ): Int? {
        val index = questions.indexOfFirst { it.id == questionId }
        return index.takeIf { it >= 0 }
    }
}
