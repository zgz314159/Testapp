package com.example.testapp.core.session.strategy.navigation

import com.example.testapp.core.util.extractSourceQuestionId
import com.example.testapp.domain.model.Question

/** 全答模式词条池索引（从 PracticeFullAnswerIconNavigation 收编） */
object SessionFullAnswerSourcePoolPipeline {
    fun sourcePoolIndices(
        questions: List<Question>,
        anchorIndex: Int,
    ): Set<Int> {
        val current = questions.getOrNull(anchorIndex) ?: return emptySet()
        val sourceId = extractSourceQuestionId(current.id)
        return questions.indices
            .filter { extractSourceQuestionId(questions[it].id) == sourceId }
            .toSet()
    }
}
