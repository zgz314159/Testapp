package com.example.testapp.presentation.session.practice

import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.presentation.screen.practice.PracticeQuestionRetryPipeline
import com.example.testapp.presentation.screen.practice.PracticeSessionGradeSnapshot
import com.example.testapp.presentation.screen.practice.PracticeStateUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/** 练习提交 / 揭示 / 错题重试 */
internal class PracticeSessionGradeDelegate(
    private val stateUpdater: PracticeStateUpdater,
    private val sessionState: MutableStateFlow<PracticeSessionState>,
    private val scope: CoroutineScope,
    private val onAnsweredSnapshot: (Int) -> Unit,
    private val saveProgress: () -> Unit,
) {
    fun updateShowResult(
        index: Int,
        value: Boolean,
    ) {
        stateUpdater.updateShowResult(index, value)
        if (value) onAnsweredSnapshot(index)
    }

    fun revealShowResult(index: Int) {
        stateUpdater.revealShowResult(index)
        scope.launch {
            onAnsweredSnapshot(index)
            saveProgress()
        }
    }

    suspend fun gradeSessionOnSubmit(): PracticeSessionGradeSnapshot {
        val revealed = stateUpdater.revealAllInputAnswers()
        revealed.forEach(onAnsweredSnapshot)
        val state = sessionState.value
        return PracticeSessionGradeSnapshot(
            sessionCorrectCount = state.sessionCorrectCount,
            sessionAnsweredCount = state.sessionAnsweredCount,
            answeredCount = state.answeredCount,
        )
    }

    fun retryCurrentQuestion(index: Int) {
        val state = sessionState.value
        if (index !in state.questionsWithState.indices) return
        onAnsweredSnapshot(index)
        sessionState.value =
            state.copy(
                currentIndex = index,
                questionsWithState =
                    state.questionsWithState.mapIndexed { idx, qws ->
                        if (idx == index) PracticeQuestionRetryPipeline.reopenCurrent(qws) else qws
                    },
            )
        saveProgress()
    }

    fun retryWrongBlanks(index: Int) {
        val state = sessionState.value
        if (index !in state.questionsWithState.indices) return
        onAnsweredSnapshot(index)
        sessionState.value =
            state.copy(
                currentIndex = index,
                questionsWithState =
                    state.questionsWithState.mapIndexed { idx, qws ->
                        if (idx == index) PracticeQuestionRetryPipeline.reopenWrongBlanks(qws) else qws
                    },
            )
        saveProgress()
    }
}
