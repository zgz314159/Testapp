package com.example.testapp.uicommon.component

import com.example.testapp.core.util.extractDerivedFillQuestionRound
import com.example.testapp.core.util.extractSourceQuestionId
import com.example.testapp.domain.model.Question

/** 全答原子题库答题卡：词条号、出题号标签与按词条分组判定。 */
object AnswerCardDisplayInfoPipeline {

    fun build(
        sessionQuestions: List<Question>,
        sourceCatalog: List<Question>,
        fullAnswerMode: Boolean
    ): Map<Int, AnswerCardDisplayInfo> {
        val orderBySourceId = sourceCatalog
            .distinctBy { it.id }
            .mapIndexed { index, question -> question.id to (index + 1) }
            .toMap()
        val fallbackOrder = entryOrderInSession(sessionQuestions)
        return sessionQuestions.associate { question ->
            val sourceId = extractSourceQuestionId(question.id)
            val order = orderBySourceId[sourceId] ?: fallbackOrder[sourceId] ?: 1
            val round = extractDerivedFillQuestionRound(question.id)
            question.id to AnswerCardDisplayInfo(
                label = formatEntryRoundLabel(order, round, fullAnswerMode),
                order = order,
                round = round
            )
        }
    }

    fun useEntryGroupedLayout(
        displayInfo: Map<Int, AnswerCardDisplayInfo>,
        fullAnswerMode: Boolean
    ): Boolean = fullAnswerMode && displayInfo.values.any { it.round != null }

    fun formatEntryRoundLabel(order: Int, round: Int?, fullAnswerMode: Boolean): String =
        when {
            fullAnswerMode && round != null -> "${order}${AnswerCardEntryCompactLayout.circled(round)}"
            round != null -> "$order-$round"
            else -> order.toString()
        }

    private fun entryOrderInSession(questions: List<Question>): Map<Int, Int> {
        val result = linkedMapOf<Int, Int>()
        var order = 0
        questions.forEach { question ->
            val sourceId = extractSourceQuestionId(question.id)
            if (sourceId !in result) {
                order++
                result[sourceId] = order
            }
        }
        return result
    }
}
