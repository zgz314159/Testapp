package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.QuestionWithState

/** clearProgress 后本地 session 状态重置（纯逻辑） */
object PracticeProgressLocalResetPipeline {

    fun resetQuestions(currentState: PracticeSessionState): PracticeSessionState =
        currentState.copy(
            currentIndex = 0,
            questionsWithState = currentState.questionsWithState.map(::clearQuestionAnswers),
            progressLoaded = false,
        )

    private fun clearQuestionAnswers(item: QuestionWithState): QuestionWithState =
        item.copy(
            selectedOptions = emptyList(),
            showResult = false,
            analysis = "",
            sparkAnalysis = "",
            baiduAnalysis = "",
            note = "",
        )
}
