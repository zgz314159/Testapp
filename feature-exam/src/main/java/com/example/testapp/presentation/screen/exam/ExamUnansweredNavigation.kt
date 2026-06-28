package com.example.testapp.presentation.screen.exam

import com.example.testapp.domain.model.QuestionWithState

enum class ExamUnansweredNavResult {
    Navigated,
    AtFirstUnanswered,
    AtLastUnanswered
}

/** 考试未答题导航池 — 随机模式全库 pending（除当前）；顺序模式按锚点方向 */
object ExamUnansweredNavigation {

    fun hasPrevUnanswered(
        anchorIndex: Int,
        questionsWithState: List<QuestionWithState>,
        isPending: (QuestionWithState) -> Boolean,
        randomExam: Boolean = false
    ): Boolean = pendingPoolForDirection(
        anchorIndex, questionsWithState, isPending, forward = false, randomExam
    ).isNotEmpty()

    fun hasNextUnanswered(
        anchorIndex: Int,
        questionsWithState: List<QuestionWithState>,
        isPending: (QuestionWithState) -> Boolean,
        randomExam: Boolean = false
    ): Boolean = if (randomExam) {
        otherPendingPool(anchorIndex, questionsWithState, isPending).isNotEmpty()
    } else {
        ExamSequentialNextPipeline.resolveNextIndex(anchorIndex, questionsWithState, isPending) != null
    }

    fun resolvePrevUnansweredIndex(
        anchorIndex: Int,
        questionsWithState: List<QuestionWithState>,
        isPending: (QuestionWithState) -> Boolean,
        randomExam: Boolean,
        random: kotlin.random.Random = kotlin.random.Random.Default
    ): Pair<ExamUnansweredNavResult, Int?> {
        val pool = pendingPoolForDirection(
            anchorIndex, questionsWithState, isPending, forward = false, randomExam
        )
        if (pool.isEmpty()) return ExamUnansweredNavResult.AtFirstUnanswered to null
        val target = if (randomExam) pool.random(random) else pool.maxOrNull()
        return ExamUnansweredNavResult.Navigated to target
    }

    fun resolveNextUnansweredIndex(
        anchorIndex: Int,
        questionsWithState: List<QuestionWithState>,
        isPending: (QuestionWithState) -> Boolean,
        randomExam: Boolean,
        random: kotlin.random.Random = kotlin.random.Random.Default
    ): Pair<ExamUnansweredNavResult, Int?> {
        if (!randomExam) {
            val target = ExamSequentialNextPipeline.resolveNextIndex(
                anchorIndex, questionsWithState, isPending
            )
            return if (target == null) {
                ExamUnansweredNavResult.AtLastUnanswered to null
            } else {
                ExamUnansweredNavResult.Navigated to target
            }
        }
        val pool = otherPendingPool(anchorIndex, questionsWithState, isPending)
        if (pool.isEmpty()) return ExamUnansweredNavResult.AtLastUnanswered to null
        return ExamUnansweredNavResult.Navigated to pool.random(random)
    }

    private fun pendingPoolForDirection(
        anchorIndex: Int,
        questionsWithState: List<QuestionWithState>,
        isPending: (QuestionWithState) -> Boolean,
        forward: Boolean,
        randomExam: Boolean
    ): List<Int> = if (randomExam) {
        otherPendingPool(anchorIndex, questionsWithState, isPending)
    } else if (forward) {
        nextUnansweredPool(anchorIndex, questionsWithState, isPending)
    } else {
        prevUnansweredPool(anchorIndex, questionsWithState, isPending)
    }

    private fun otherPendingPool(
        anchorIndex: Int,
        questionsWithState: List<QuestionWithState>,
        isPending: (QuestionWithState) -> Boolean
    ): List<Int> = questionsWithState.indices.filter { index ->
        index != anchorIndex && isPending(questionsWithState[index])
    }

    private fun prevUnansweredPool(
        anchorIndex: Int,
        questionsWithState: List<QuestionWithState>,
        isPending: (QuestionWithState) -> Boolean
    ): List<Int> = questionsWithState.indices.filter { index ->
        index < anchorIndex && isPending(questionsWithState[index])
    }

    private fun nextUnansweredPool(
        anchorIndex: Int,
        questionsWithState: List<QuestionWithState>,
        isPending: (QuestionWithState) -> Boolean
    ): List<Int> = questionsWithState.indices.filter { index ->
        index > anchorIndex && isPending(questionsWithState[index])
    }
}
