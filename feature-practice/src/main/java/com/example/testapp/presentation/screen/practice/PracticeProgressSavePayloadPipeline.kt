package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.QuestionWithState
import com.example.testapp.domain.model.UnifiedQuestionState

/** performSaveProgress 载荷与 map 合并（纯逻辑） */
object PracticeProgressSavePayloadPipeline {

    fun mergeQuestionStateMap(
        cumulative: MutableMap<Int, UnifiedQuestionState>,
        questionsWithState: List<QuestionWithState>,
    ): Map<Int, UnifiedQuestionState> =
        PracticeProgressMapPipeline.mergeInto(cumulative, questionsWithState)

    fun buildExtras(
        questionStateMap: Map<Int, UnifiedQuestionState>,
        questionsWithState: List<QuestionWithState>,
    ): Map<String, Any> = mapOf(
        "questionStateMap" to questionStateMap,
        "fixedQuestionOrder" to questionsWithState.map { it.question.id },
    )

    fun fixedOrderFrom(state: PracticeSessionState): List<Int> =
        state.questionsWithState.map { it.question.id }
}
