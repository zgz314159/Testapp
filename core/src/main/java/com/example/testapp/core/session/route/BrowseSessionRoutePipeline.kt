package com.example.testapp.core.session.route

import com.example.testapp.domain.session.QuestionSessionKind

/** 抽屉点题 / 搜索跳转：Practice 路由 vs 独立 Browse 会话 */
object BrowseSessionRoutePipeline {
    fun shouldUseBrowseSession(targetQuestionId: Int?): Boolean =
        targetQuestionId != null && targetQuestionId >= 0

    fun browseKind(
        quizId: String,
        targetQuestionId: Int,
    ): QuestionSessionKind.Browse = QuestionSessionKind.Browse(quizId = quizId, targetQuestionId = targetQuestionId)

    fun practiceQuestionRoute(
        encodedQuizId: String,
        targetQuestionId: Int?,
    ): String =
        if (shouldUseBrowseSession(targetQuestionId)) {
            "question/$encodedQuizId?targetQuestionId=$targetQuestionId"
        } else {
            "question/$encodedQuizId"
        }
}
