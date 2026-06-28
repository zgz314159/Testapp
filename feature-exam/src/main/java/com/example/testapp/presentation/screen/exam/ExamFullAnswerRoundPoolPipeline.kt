package com.example.testapp.presentation.screen.exam

import com.example.testapp.core.util.extractDerivedFillQuestionRound
import com.example.testapp.core.util.extractSourceQuestionId
import com.example.testapp.domain.model.Question

/** 全答模式：当前词条 + 当前轮次 构成的题池索引 */
object ExamFullAnswerRoundPoolPipeline {

    fun roundOf(questionId: Int): Int = extractDerivedFillQuestionRound(questionId) ?: 1

    fun indicesInRoundPool(questions: List<Question>, currentIndex: Int): List<Int> {
        val current = questions.getOrNull(currentIndex) ?: return emptyList()
        val sourceId = extractSourceQuestionId(current.id)
        val round = roundOf(current.id)
        return questions.indices.filter { index ->
            val question = questions[index]
            extractSourceQuestionId(question.id) == sourceId && roundOf(question.id) == round
        }
    }
}
