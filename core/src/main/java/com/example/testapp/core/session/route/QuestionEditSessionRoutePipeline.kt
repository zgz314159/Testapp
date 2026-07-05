package com.example.testapp.core.session.route

/** 抽屉长按改题 → 独立 QuestionEdit 路由 */
object QuestionEditSessionRoutePipeline {
    fun route(
        encodedQuizId: String,
        questionId: Int,
    ): String = "question_edit/$encodedQuizId/$questionId"
}
