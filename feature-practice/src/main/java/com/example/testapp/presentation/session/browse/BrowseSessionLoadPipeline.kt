package com.example.testapp.presentation.session.browse

import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState
import com.example.testapp.presentation.screen.practice.PracticePinnedQuestionPipeline
import com.example.testapp.presentation.screen.practice.PracticeQuestionCountPolicy
import com.example.testapp.presentation.screen.practice.PracticeQuestionOrderPipeline

/** 抽屉浏览：加载题库、纳入点选题、不恢复进度 */
object BrowseSessionLoadPipeline {
    data class Result(
        val questionsWithState: List<QuestionWithState>,
        val startIndex: Int,
    )

    fun load(
        catalog: List<Question>,
        targetQuestionId: Int,
        questionCount: Int,
        random: Boolean,
        sessionStartTime: Long,
    ): Result {
        if (catalog.isEmpty()) {
            return Result(emptyList(), 0)
        }
        val distinctCatalog = catalog.distinctBy { it.id }
        val ordered =
            if (random && questionCount > 0) {
                PracticeQuestionOrderPipeline.orderForNewRound(
                    originalQuestions = distinctCatalog,
                    questionCount = questionCount,
                    random = true,
                    answeredSourceIds = emptySet(),
                    seed = sessionStartTime,
                    lastRoundSourceIds = emptySet(),
                )
            } else {
                PracticeQuestionCountPolicy.limitQuestions(distinctCatalog, questionCount)
            }
        val pinId = targetQuestionId.takeIf { it > 0 }
        val sessionQuestions =
            PracticePinnedQuestionPipeline.ensurePinned(
                ordered = ordered,
                catalog = distinctCatalog,
                pinnedQuestionId = pinId,
                questionCount = questionCount,
            )
        val startIndex =
            if (pinId != null) {
                PracticePinnedQuestionPipeline.indexInSession(
                    questions = sessionQuestions,
                    questionId = pinId,
                ) ?: 0
            } else {
                0
            }
        return Result(
            questionsWithState = sessionQuestions.map { QuestionWithState(question = it) },
            startIndex = startIndex,
        )
    }
}
