package com.example.testapp.presentation.screen.practice

import com.example.testapp.core.util.extractSourceQuestionId
import com.example.testapp.domain.model.Question

/** 全答底栏单击 step1：当前词条 + 当前轮次号题池（不跨词条）。 */
object PracticeFullAnswerSourceRoundPoolPipeline {

    fun indicesInPool(questions: List<Question>, currentIndex: Int): List<Int> {
        val current = questions.getOrNull(currentIndex) ?: return emptyList()
        val round = PracticeFullAnswerRoundPoolPipeline.roundOf(current.id)
        val sourceId = extractSourceQuestionId(current.id)
        return questions.indices.filter { index ->
            extractSourceQuestionId(questions[index].id) == sourceId &&
                PracticeFullAnswerRoundPoolPipeline.roundOf(questions[index].id) == round
        }
    }
}
