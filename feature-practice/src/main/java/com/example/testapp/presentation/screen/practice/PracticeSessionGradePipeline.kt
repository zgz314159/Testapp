package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.QuestionWithState

/** 交卷前批量批改：对有输入但未 showResult 的题 reveal。 */
object PracticeSessionGradePipeline {

    fun indicesPendingReveal(questionsWithState: List<QuestionWithState>): List<Int> =
        questionsWithState.indices.filter { index ->
            val qws = questionsWithState[index]
            PracticeFullAnswerRoundSlotPendingPipeline.hasInputContent(qws) && !qws.showResult
        }
}
