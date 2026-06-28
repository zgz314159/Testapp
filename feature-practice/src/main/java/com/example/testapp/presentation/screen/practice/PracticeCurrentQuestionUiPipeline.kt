package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.PracticeSessionState

data class PracticeCurrentQuestionUi(
    val index: Int,
    val selectedOptions: List<Int>,
    val textAnswer: String,
    val showResult: Boolean,
    val analysis: String,
    val sparkAnalysis: String,
    val baiduAnalysis: String,
    val note: String
)

object PracticeCurrentQuestionUiPipeline {
    fun snapshot(state: PracticeSessionState): PracticeCurrentQuestionUi? {
        val index = state.currentIndex
        val qws = state.questionsWithState.getOrNull(index) ?: return null
        return PracticeCurrentQuestionUi(
            index = index,
            selectedOptions = qws.selectedOptions,
            textAnswer = qws.textAnswer,
            showResult = qws.showResult,
            analysis = qws.analysis,
            sparkAnalysis = qws.sparkAnalysis,
            baiduAnalysis = qws.baiduAnalysis,
            note = qws.note
        )
    }
}
