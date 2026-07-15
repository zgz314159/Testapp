package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.Question
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** loadQuestionsForCurrentSource 题目顺序解析（复用 saved order 或新轮排序） */
object PracticeProgressLoadOrderPipeline {

    suspend fun resolveOrderedSourceQuestions(
        originalQuestions: List<Question>,
        context: PracticeProgressLoadRoundContext,
        randomPracticeEnabled: Boolean,
        questionCount: Int,
        newSessionStartTime: Long,
    ): List<Question> {
        if (context.canReuseSavedOrder) {
            val questionsMap = originalQuestions.associateBy { it.id }
            return context.savedSourceOrder.mapNotNull { questionsMap[it] }
        }
        return withContext(Dispatchers.Default) {
            val lastRoundSourceIds = if (context.startNewRound) {
                PracticeSourceQuestionPipeline.lastRoundSourceIds(
                    context.existingProgress?.fixedQuestionOrder.orEmpty(),
                )
            } else {
                emptySet()
            }
            val answeredSourceIds = PracticeQuestionOrderPipeline.answeredQuestionIds(
                context.existingProgress?.questionStateMap.orEmpty(),
            )
            PracticeQuestionOrderPipeline.orderForNewRound(
                originalQuestions = originalQuestions,
                questionCount = questionCount,
                random = randomPracticeEnabled,
                answeredSourceIds = answeredSourceIds,
                seed = newSessionStartTime,
                lastRoundSourceIds = lastRoundSourceIds,
            )
        }
    }
}
