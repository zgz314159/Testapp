package com.example.testapp.core.session.strategy.edit

import com.example.testapp.domain.session.QuestionSessionKind

/** 题库抽屉单题编辑 Strategy 绑定 */
object QuestionEditSessionStrategyBootstrap {
    fun kind(
        quizId: String,
        questionId: Int,
    ): QuestionSessionKind.QuestionEdit = QuestionSessionKind.QuestionEdit(quizId = quizId, questionId = questionId)
}
