package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.QuestionWithState
import kotlin.random.Random

/** 非原子全答：底栏箭头在题库未作答题间跳转（顺序环绕 / 随机）。 */
object PracticeFullAnswerIconUnansweredPipeline {

    fun resolveNextIndex(
        anchorIndex: Int,
        questionsWithState: List<QuestionWithState>,
        isPending: (QuestionWithState) -> Boolean,
        randomOrder: Boolean,
        random: Random = Random.Default
    ): Pair<UnansweredNavResult, Int?> {
        val pool = pendingPool(anchorIndex, questionsWithState, isPending, forward = true, randomOrder)
        if (pool.isEmpty()) return UnansweredNavResult.AtLastUnanswered to null
        val target = if (randomOrder) pool.random(random) else pool.minOrNull()
        return UnansweredNavResult.Navigated to target
    }

    fun resolvePrevIndex(
        anchorIndex: Int,
        questionsWithState: List<QuestionWithState>,
        isPending: (QuestionWithState) -> Boolean,
        randomOrder: Boolean,
        random: Random = Random.Default
    ): Pair<UnansweredNavResult, Int?> {
        val pool = pendingPool(anchorIndex, questionsWithState, isPending, forward = false, randomOrder)
        if (pool.isEmpty()) return UnansweredNavResult.AtFirstUnanswered to null
        val target = if (randomOrder) pool.random(random) else pool.maxOrNull()
        return UnansweredNavResult.Navigated to target
    }

    fun hasNext(
        anchorIndex: Int,
        questionsWithState: List<QuestionWithState>,
        isPending: (QuestionWithState) -> Boolean,
        randomOrder: Boolean
    ): Boolean = pendingPool(anchorIndex, questionsWithState, isPending, forward = true, randomOrder).isNotEmpty()

    fun hasPrev(
        anchorIndex: Int,
        questionsWithState: List<QuestionWithState>,
        isPending: (QuestionWithState) -> Boolean,
        randomOrder: Boolean
    ): Boolean = pendingPool(anchorIndex, questionsWithState, isPending, forward = false, randomOrder).isNotEmpty()

    private fun pendingPool(
        anchorIndex: Int,
        questionsWithState: List<QuestionWithState>,
        isPending: (QuestionWithState) -> Boolean,
        forward: Boolean,
        randomOrder: Boolean
    ): List<Int> {
        if (randomOrder) {
            return questionsWithState.indices.filter { index ->
                index != anchorIndex && isPending(questionsWithState[index])
            }
        }
        val directional = questionsWithState.indices.filter { index ->
            if (forward) index > anchorIndex else index < anchorIndex
        }.filter { index -> isPending(questionsWithState[index]) }
        if (directional.isNotEmpty()) return directional
        return questionsWithState.indices.filter { index ->
            index != anchorIndex && isPending(questionsWithState[index])
        }
    }
}
