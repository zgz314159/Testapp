package com.example.testapp.domain.session

/** 会话类型标识；Registry 以 `kind::class` O(1) 查找 Creator */
sealed class QuestionSessionKind {
    data class Practice(
        val quizId: String,
        val wrongBookFileName: String? = null,
        val favoriteFileName: String? = null
    ) : QuestionSessionKind()

    data class AdaptiveFading(
        val quizId: String,
    ) : QuestionSessionKind()

    data class Browse(
        val quizId: String,
        val targetQuestionId: Int
    ) : QuestionSessionKind()

    data class Review(
        val progressId: String,
        val wrongBookFileName: String? = null,
        val favoriteFileName: String? = null
    ) : QuestionSessionKind()

    data class Exam(
        val quizId: String,
        val wrongBookFileName: String? = null,
        val favoriteFileName: String? = null,
        val reviewProgressId: String? = null
    ) : QuestionSessionKind()

    /** 题库抽屉内单题编辑（Browse 类策略 + 可改题） */
    data class QuestionEdit(
        val quizId: String,
        val questionId: Int
    ) : QuestionSessionKind()
}
