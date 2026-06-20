package com.example.testapp.presentation.screen.exam

import com.example.testapp.core.common.LocalizedResult
import com.example.testapp.domain.IOConstants
import com.example.testapp.domain.LocalizedException
import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.usecase.ExamUseCaseFacade
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

class ExamGradeCoordinator(
    private val sessionState: MutableStateFlow<PracticeSessionState>,
    private val messageResult: MutableStateFlow<LocalizedResult?>,
    private val facade: ExamUseCaseFacade,
    private val progressSeed: () -> Long,
    private val quizIdInternal: () -> String,
    private val memoryModeActive: () -> Boolean,
    private val advanceMemoryRoundIfNeeded: suspend () -> Boolean,
    private val incrementExamCount: () -> Unit,
    private val saveProgressInternal: suspend () -> Unit
) {
    private val disposeGradeRequested = AtomicBoolean(false)

    private fun calculateElapsedTime(): Long = (System.currentTimeMillis() - progressSeed()) / 1000

    fun scheduleGradeExamAfterDispose() {
        if (!disposeGradeRequested.compareAndSet(false, true)) return
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                gradeExam()
            } finally {
                if (!sessionState.value.finished) disposeGradeRequested.set(false)
            }
        }
    }

    suspend fun gradeExam(): Int = withContext(Dispatchers.Default) {
        val state = sessionState.value
        if (memoryModeActive() && state.unansweredCount == 0) {
            if (advanceMemoryRoundIfNeeded()) return@withContext -1
        }
        if (state.finished) return@withContext state.correctCount

        val questions = state.questions
        val selectedOptions = state.questionsWithState.map { it.selectedOptions }
        if (questions.isEmpty() || selectedOptions.isEmpty()) return@withContext 0

        val result = facade.gradeExam(
            questions,
            selectedOptions,
            quizIdInternal(),
            calculateElapsedTime().toInt(),
            progressSeed()
        )
        if (result.isFailure) {
            val exception = result.exceptionOrNull()
            messageResult.value = if (exception is LocalizedException) {
                LocalizedResult(exception.key, exception.args)
            } else {
                LocalizedResult(IOConstants.SAVE_FAILED_PREFIX, listOf(exception?.message.orEmpty()))
            }
            return@withContext 0
        }

        val examResult = result.getOrNull() ?: return@withContext 0
        val updatedQuestions = state.questionsWithState.map {
            if (it.selectedOptions.isNotEmpty()) it.copy(showResult = true) else it
        }
        sessionState.value = state.copy(
            questionsWithState = updatedQuestions,
            finished = updatedQuestions.all { it.showResult }
        )
        if (examResult.examRecorded) incrementExamCount()
        saveProgressInternal()
        examResult.score
    }
}
