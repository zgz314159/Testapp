package com.example.testapp.presentation.screen.exam

import com.example.testapp.domain.model.ExamProgress
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.UnifiedQuestionState

/** 新轮次出题顺序：未答池优先，随机/顺序；再按题数截断 */
object ExamQuestionOrderPipeline {

    fun answeredQuestionIds(questionStateMap: Map<Int, UnifiedQuestionState>): Set<Int> =
        ExamSourceQuestionPipeline.answeredSourceIds(questionStateMap)

    fun orderForNewRound(
        originalQuestions: List<Question>,
        questionCount: Int,
        random: Boolean,
        answeredSourceIds: Set<Int>,
        seed: Long,
        lastRoundSourceIds: Set<Int> = emptySet()
    ): List<Question> {
        val unanswered = originalQuestions.filter { it.id !in answeredSourceIds }
        val answered = originalQuestions.filter { it.id in answeredSourceIds }
        val ordered = when {
            unanswered.isNotEmpty() -> {
                if (random) {
                    unanswered.shuffled(java.util.Random(seed)) +
                        answered.shuffled(java.util.Random(seed + 1000))
                } else {
                    unanswered + answered
                }
            }
            else -> recyclePool(originalQuestions, lastRoundSourceIds, random, seed)
        }
        return ExamQuestionCountPolicy.limitQuestions(ordered, questionCount)
    }

    private fun recyclePool(
        originalQuestions: List<Question>,
        lastRoundSourceIds: Set<Int>,
        random: Boolean,
        seed: Long
    ): List<Question> {
        val preferred = originalQuestions.filter { it.id !in lastRoundSourceIds }
        val pool = preferred.ifEmpty { originalQuestions }
        return if (random) pool.shuffled(java.util.Random(seed)) else pool
    }

    fun buildNewRoundProgress(
        prior: ExamProgress?,
        progressId: String,
        seed: Long,
        sessionId: String,
        questions: List<Question>
    ): ExamProgress = ExamProgress(
        id = progressId,
        currentIndex = 0,
        selectedOptions = emptyList(),
        showResultList = emptyList(),
        analysisList = emptyList(),
        sparkAnalysisList = emptyList(),
        baiduAnalysisList = emptyList(),
        noteList = emptyList(),
        finished = false,
        timestamp = seed,
        sessionId = sessionId,
        fixedQuestionOrder = questions.map { it.id },
        questionStateMap = prior?.questionStateMap.orEmpty()
    )
}
