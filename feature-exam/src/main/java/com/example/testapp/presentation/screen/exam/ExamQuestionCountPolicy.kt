package com.example.testapp.presentation.screen.exam

import com.example.testapp.domain.model.Question

/** 考试题数策略：0 = 全部，>0 = 限制题数 */
object ExamQuestionCountPolicy {

    fun limitQuestions(questions: List<Question>, questionCount: Int): List<Question> =
        if (questionCount > 0) {
            questions.take(questionCount.coerceAtMost(questions.size))
        } else {
            questions
        }

    fun expectedCount(questionCount: Int, fullPoolSize: Int): Int =
        if (questionCount > 0) questionCount.coerceAtMost(fullPoolSize) else fullPoolSize

    fun canReuseSavedOrder(savedSourceCount: Int, questionCount: Int, fullPoolSize: Int): Boolean {
        if (savedSourceCount <= 0) return false
        return if (questionCount <= 0) {
            savedSourceCount >= fullPoolSize
        } else {
            savedSourceCount >= questionCount
        }
    }
}
