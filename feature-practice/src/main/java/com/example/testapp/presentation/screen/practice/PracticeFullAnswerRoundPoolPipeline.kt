package com.example.testapp.presentation.screen.practice

import com.example.testapp.core.util.extractDerivedFillQuestionRound
import com.example.testapp.domain.model.Question

/** 全答：会话内同轮次号（跨词条）题池索引；用于出池守卫 / 全局轮次完成判定。 */
object PracticeFullAnswerRoundPoolPipeline {

    fun roundOf(questionId: Int): Int = extractDerivedFillQuestionRound(questionId) ?: 1

    fun indicesInRoundPool(questions: List<Question>, currentIndex: Int): List<Int> {
        val current = questions.getOrNull(currentIndex) ?: return emptyList()
        val round = roundOf(current.id)
        return questions.indices.filter { index -> roundOf(questions[index].id) == round }
    }
}
