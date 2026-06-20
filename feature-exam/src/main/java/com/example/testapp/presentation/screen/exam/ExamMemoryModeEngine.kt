package com.example.testapp.presentation.screen.exam

import com.example.testapp.domain.model.UnifiedQuestionState
import com.example.testapp.domain.model.Question
import javax.inject.Inject
import javax.inject.Singleton

data class MemoryRoundPlan(
    val questions: List<Question>,
    val wrongQuestionIds: Set<Int>
)

@Singleton
class ExamMemoryModeEngine @Inject constructor() {
    companion object {
        const val MEMORY_WRONG_MODE_RETRY_WRONG_BLANKS = 0
        const val MEMORY_WRONG_MODE_REDO_ALL_BLANKS = 1
        const val MEMORY_POOL_MODE_IN_OUT = 0
        const val MEMORY_POOL_MODE_ROUND = 1
    }

    fun shouldUseMemoryMode(enabled: Boolean, source: String): Boolean {
        if (!enabled) return false
        return !source.lowercase().let { it.startsWith("wrongbook_") || it.startsWith("favorite_") }
    }

    fun buildMemoryRoundPlan(
        sourceQuestions: List<Question>, seed: Long, batchSize: Int,
        randomEnabled: Boolean, persistentMap: Map<Int, UnifiedQuestionState>,
        answerRules: ExamAnswerRules
    ): MemoryRoundPlan {
        val tc = batchSize.coerceIn(1, sourceQuestions.size.coerceAtLeast(1))
        val wrong = sourceQuestions.filter { q ->
            persistentMap[q.id]?.let { answerRules.isQuestionAnswered(it) && !answerRules.isQuestionCorrect(q, it) } == true
        }
        val unseen = sourceQuestions.filter { q ->
            persistentMap[q.id]?.let { !answerRules.isQuestionAnswered(it) } ?: true
        }
        if (wrong.isEmpty() && unseen.isEmpty()) return MemoryRoundPlan(emptyList(), emptySet())
        val rnd = java.util.Random(seed)
        val sWrong = if (randomEnabled) wrong.shuffled(rnd) else wrong
        val sUnseen = if (randomEnabled) unseen.shuffled(rnd) else unseen
        val cWrong = sWrong.take(tc); val cUnseen = sUnseen.take((tc - cWrong.size).coerceAtLeast(0))
        return MemoryRoundPlan((cWrong + cUnseen).distinctBy { it.id }, cWrong.map { it.id }.toSet())
    }

    fun effectiveCurrentMemoryRoundQuestionIds(
        questions: List<Question>, memoryActive: Boolean, poolMode: Int, currentIds: Set<Int>
    ): Set<Int> {
        if (!memoryActive || poolMode != MEMORY_POOL_MODE_ROUND) return currentIds
        if (currentIds.isNotEmpty()) return currentIds
        return questions.map { it.id }.toSet()
    }
}
