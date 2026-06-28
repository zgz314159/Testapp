package com.example.testapp.presentation.screen.practice

import com.example.testapp.core.util.extractSourceQuestionId
import com.example.testapp.domain.model.Question

/**
 * 练习全答模式：双击上一题/下一题跳过当前词条各轮，跳到相邻词条。
 */
object PracticeFullAnswerNavigation {
    fun listSourceEntryIndices(questions: List<Question>): List<Int> {
        val indices = mutableListOf<Int>()
        val seen = mutableSetOf<Int>()
        questions.forEachIndexed { index, question ->
            val sourceId = extractSourceQuestionId(question.id)
            if (sourceId !in seen) {
                seen.add(sourceId)
                indices.add(index)
            }
        }
        return indices
    }

    fun resolveSkipToAdjacentSourceIndex(
        questions: List<Question>,
        currentIndex: Int,
        forward: Boolean,
        randomOrder: Boolean,
        random: kotlin.random.Random = kotlin.random.Random.Default
    ): Int? {
        if (questions.isEmpty()) return null
        val currentQuestion = questions.getOrNull(currentIndex) ?: return null
        val currentSourceId = extractSourceQuestionId(currentQuestion.id)
        if (currentSourceId == currentQuestion.id) return null

        val entries = listSourceEntryIndices(questions)
        if (entries.size <= 1) return null

        val currentEntryPos = entries.indexOfFirst {
            extractSourceQuestionId(questions[it].id) == currentSourceId
        }
        if (currentEntryPos < 0) return null

        if (randomOrder) {
            return entries
                .filter { extractSourceQuestionId(questions[it].id) != currentSourceId }
                .randomOrNull(random)
        }

        return if (forward) {
            entries.getOrNull(currentEntryPos + 1)
        } else {
            entries.getOrNull(currentEntryPos - 1)
        }
    }
}
