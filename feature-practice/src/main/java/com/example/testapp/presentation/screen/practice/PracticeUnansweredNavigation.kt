package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.QuestionWithState

enum class UnansweredNavResult {
    Navigated,
    AtFirstUnanswered,
    AtLastUnanswered
}

object PracticeUnansweredNavigation {

    fun hasPrevUnanswered(
        anchorIndex: Int,
        questionsWithState: List<QuestionWithState>,
        isPending: (QuestionWithState) -> Boolean,
        randomPractice: Boolean = false
    ): Boolean = pendingPoolForDirection(
        anchorIndex, questionsWithState, isPending, forward = false, randomPractice
    ).isNotEmpty()

    fun hasNextUnanswered(
        anchorIndex: Int,
        questionsWithState: List<QuestionWithState>,
        isPending: (QuestionWithState) -> Boolean,
        randomPractice: Boolean = false
    ): Boolean = pendingPoolForDirection(
        anchorIndex, questionsWithState, isPending, forward = true, randomPractice
    ).isNotEmpty()

    fun resolvePrevUnansweredIndex(
        anchorIndex: Int,
        questionsWithState: List<QuestionWithState>,
        isPending: (QuestionWithState) -> Boolean,
        randomPractice: Boolean,
        random: kotlin.random.Random = kotlin.random.Random.Default
    ): Pair<UnansweredNavResult, Int?> {
        val pool = pendingPoolForDirection(
            anchorIndex, questionsWithState, isPending, forward = false, randomPractice
        )
        if (pool.isEmpty()) return UnansweredNavResult.AtFirstUnanswered to null
        val target = if (randomPractice) {
            pool.random(random)
        } else {
            pool.maxOrNull()
        }
        return UnansweredNavResult.Navigated to target
    }

    fun resolveNextUnansweredIndex(
        anchorIndex: Int,
        questionsWithState: List<QuestionWithState>,
        isPending: (QuestionWithState) -> Boolean,
        randomPractice: Boolean,
        random: kotlin.random.Random = kotlin.random.Random.Default
    ): Pair<UnansweredNavResult, Int?> {
        val pool = pendingPoolForDirection(
            anchorIndex, questionsWithState, isPending, forward = true, randomPractice
        )
        if (pool.isEmpty()) return UnansweredNavResult.AtLastUnanswered to null
        val target = if (randomPractice) {
            pool.random(random)
        } else {
            pool.minOrNull()
        }
        return UnansweredNavResult.Navigated to target
    }

    private fun pendingPoolForDirection(
        anchorIndex: Int,
        questionsWithState: List<QuestionWithState>,
        isPending: (QuestionWithState) -> Boolean,
        forward: Boolean,
        randomPractice: Boolean
    ): List<Int> = if (randomPractice) {
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
