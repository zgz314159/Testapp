package com.example.testapp.presentation.screen.practice

/**
 * 练习题数策略：0 = 全部（不截断），>0 = 限制题数。
 */
object PracticeQuestionCountPolicy {
    fun limitQuestions(questions: List<com.example.testapp.domain.model.Question>, questionCount: Int) =
        if (questionCount > 0) {
            questions.take(questionCount.coerceAtMost(questions.size))
        } else {
            questions
        }

    fun canReuseSavedOrder(savedSourceCount: Int, questionCount: Int, fullPoolSize: Int): Boolean {
        if (savedSourceCount <= 0) return false
        return if (questionCount <= 0) {
            savedSourceCount >= fullPoolSize
        } else {
            savedSourceCount >= questionCount
        }
    }
}
