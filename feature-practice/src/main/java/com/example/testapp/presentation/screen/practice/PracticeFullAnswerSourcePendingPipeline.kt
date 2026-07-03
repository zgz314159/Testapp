package com.example.testapp.presentation.screen.practice

import com.example.testapp.core.util.extractSourceQuestionId
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState

/** 当前词条（同源）是否仍有 pending 轮次槽。跨词条前须为 false。 */
object PracticeFullAnswerSourcePendingPipeline {

    fun indicesInSource(questions: List<Question>, currentIndex: Int): List<Int> {
        val current = questions.getOrNull(currentIndex) ?: return emptyList()
        val sourceId = extractSourceQuestionId(current.id)
        return questions.indices.filter { index ->
            extractSourceQuestionId(questions[index].id) == sourceId
        }
    }

    fun hasPendingInSource(
        questions: List<Question>,
        questionsWithState: List<QuestionWithState>,
        currentIndex: Int,
        fullAnswerRequireCorrect: Boolean
    ): Boolean = indicesInSource(questions, currentIndex).any { index ->
        PracticeFullAnswerRoundSlotPendingPipeline.isPendingInRoundSlot(
            questionsWithState[index],
            fullAnswerRequireCorrect
        )
    }
}
