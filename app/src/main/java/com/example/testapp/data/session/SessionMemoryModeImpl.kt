package com.example.testapp.data.session

import com.example.testapp.core.session.MemoryRoundPlan
import com.example.testapp.core.session.SessionMemoryMode
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.UnifiedQuestionState
import java.util.Random
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionMemoryModeImpl @Inject constructor() : SessionMemoryMode {

    override fun shouldUseMemoryMode(enabled: Boolean, source: String): Boolean {
        if (!enabled) return false
        return !source.lowercase().let { it.startsWith("wrongbook_") || it.startsWith("favorite_") }
    }

    override fun buildMemoryRoundPlan(
        sourceQuestions: List<Question>,
        seed: Long,
        batchSize: Int,
        randomEnabled: Boolean,
        persistentMap: Map<Int, UnifiedQuestionState>
    ): MemoryRoundPlan {
        val tc = batchSize.coerceIn(1, sourceQuestions.size.coerceAtLeast(1))
        val wrong = sourceQuestions.filter { q ->
            persistentMap[q.id]?.let { state ->
                state.showResult && state.selectedOptions.isNotEmpty() &&
                    state.selectedOptions.sorted() != emptyList<Int>()
            } == true
        }
        val unseen = sourceQuestions.filter { q ->
            persistentMap[q.id]?.let { !it.showResult && it.selectedOptions.isEmpty() && it.textAnswer.isBlank() } ?: true
        }
        if (wrong.isEmpty() && unseen.isEmpty()) return MemoryRoundPlan(emptyList(), emptySet())
        val rnd = Random(seed)
        val sWrong = if (randomEnabled) wrong.shuffled(rnd) else wrong
        val sUnseen = if (randomEnabled) unseen.shuffled(rnd) else unseen
        val cWrong = sWrong.take(tc)
        val cUnseen = sUnseen.take((tc - cWrong.size).coerceAtLeast(0))
        return MemoryRoundPlan(
            (cWrong + cUnseen).distinctBy { it.id },
            cWrong.map { it.id }.toSet()
        )
    }
}
