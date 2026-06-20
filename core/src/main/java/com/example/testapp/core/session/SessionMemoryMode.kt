package com.example.testapp.core.session

import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.UnifiedQuestionState

data class MemoryRoundPlan(
    val questions: List<Question>,
    val wrongQuestionIds: Set<Int>
)

/**
 * 记忆模式引擎 — 统一 Practice/Exam 的记忆模式逻辑。
 */
interface SessionMemoryMode {
    fun shouldUseMemoryMode(enabled: Boolean, source: String): Boolean

    fun buildMemoryRoundPlan(
        sourceQuestions: List<Question>,
        seed: Long,
        batchSize: Int,
        randomEnabled: Boolean,
        persistentMap: Map<Int, UnifiedQuestionState>
    ): MemoryRoundPlan

    companion object {
        const val MEMORY_WRONG_MODE_RETRY_WRONG_BLANKS = 0
        const val MEMORY_WRONG_MODE_REDO_ALL_BLANKS = 1
        const val MEMORY_POOL_MODE_IN_OUT = 0
        const val MEMORY_POOL_MODE_ROUND = 1
    }
}
