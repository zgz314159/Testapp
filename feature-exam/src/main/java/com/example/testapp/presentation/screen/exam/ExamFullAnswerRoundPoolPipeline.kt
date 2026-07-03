package com.example.testapp.presentation.screen.exam

import com.example.testapp.core.util.extractDerivedFillQuestionRound
import com.example.testapp.domain.model.Question

/** 全答模式：当前轮次（跨词条）题池索引 */
object ExamFullAnswerRoundPoolPipeline {

    fun roundOf(questionId: Int): Int = extractDerivedFillQuestionRound(questionId) ?: 1

    fun indicesInRoundPool(questions: List<Question>, currentIndex: Int): List<Int> {
        val current = questions.getOrNull(currentIndex) ?: return emptyList()
        val round = roundOf(current.id)
        return questions.indices.filter { index -> roundOf(questions[index].id) == round }
    }
}
