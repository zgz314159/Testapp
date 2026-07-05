package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.QuestionWithState
import com.example.testapp.domain.model.UnifiedQuestionState
import com.example.testapp.domain.model.UnifiedSessionState

/** performSaveProgress 请求载荷组装（纯逻辑） */
object PracticeProgressSaveRequestPipeline {

    data class SaveRequest(
        val mergedMap: Map<Int, UnifiedQuestionState>,
        val unifiedState: UnifiedSessionState,
        val fillSignature: String,
        val extras: Map<String, Any>,
        val logMapSize: Int,
        val logFixedOrderSize: Int,
    )

    fun build(
        state: PracticeSessionState,
        cumulativeQuestionStateMap: Map<Int, UnifiedQuestionState>,
        fillSignature: String,
        unifiedState: UnifiedSessionState,
    ): SaveRequest {
        val extras =
            PracticeProgressSavePayloadPipeline.buildExtras(
                cumulativeQuestionStateMap,
                state.questionsWithState,
            )
        val fixedOrder = PracticeProgressSavePayloadPipeline.fixedOrderFrom(state)
        return SaveRequest(
            mergedMap = cumulativeQuestionStateMap,
            unifiedState = unifiedState,
            fillSignature = fillSignature,
            extras = extras,
            logMapSize = cumulativeQuestionStateMap.size,
            logFixedOrderSize = fixedOrder.size,
        )
    }

    fun mergeMap(
        cumulative: MutableMap<Int, UnifiedQuestionState>,
        questionsWithState: List<QuestionWithState>,
    ): Map<Int, UnifiedQuestionState> =
        PracticeProgressSavePayloadPipeline.mergeQuestionStateMap(cumulative, questionsWithState)
}
