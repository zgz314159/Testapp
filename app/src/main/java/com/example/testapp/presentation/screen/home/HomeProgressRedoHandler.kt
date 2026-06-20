package com.example.testapp.presentation.screen.home

import com.example.testapp.domain.QuestionTypes
import com.example.testapp.presentation.screen.practice.preferredHomePracticeProgress
import com.example.testapp.core.common.LocalizedResult
import com.example.testapp.domain.model.PracticeProgress
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.UnifiedQuestionState
import com.example.testapp.domain.usecase.GetQuestionsUseCase
import com.example.testapp.domain.usecase.SavePracticeProgressUseCase
import com.example.testapp.core.util.answerLettersToIndices
import com.example.testapp.core.util.isFillAnswerCorrect
import com.example.testapp.core.util.retainCorrectFillAnswerParts
import com.example.testapp.core.util.resolveFillCorrectAnswer
import kotlinx.coroutines.flow.first

/**
 * Handles "redo" logic: rebuilds progress by retaining only incorrect answers,
 * creating a new PracticeProgress with filtered question order.
 */
class HomeProgressRedoHandler(
    private val getQuestionsUseCase: GetQuestionsUseCase,
    private val savePracticeProgressUseCase: SavePracticeProgressUseCase
) {
    fun applyRedoRule(question: Question, state: UnifiedQuestionState): UnifiedQuestionState {
        if (QuestionTypes.isFill(question.type)) {
            val correctAnswer = resolveFillCorrectAnswer(question)
            val isCorrect = isFillAnswerCorrect(state.textAnswer, correctAnswer)
            return if (isCorrect) {
                state.copy(
                    textAnswer = correctAnswer,
                    selectedOptions = if (correctAnswer.isNotBlank()) listOf(-1) else emptyList(),
                    showResult = true
                )
            } else {
                val retainedAnswer = retainCorrectFillAnswerParts(state.textAnswer, correctAnswer)
                state.copy(
                    textAnswer = retainedAnswer,
                    selectedOptions = if (retainedAnswer.isNotBlank()) listOf(-1) else emptyList(),
                    showResult = false,
                    answerTime = 0L
                )
            }
        }

        val correctOptions = answerLettersToIndices(question.answer)
        val isCorrect = state.selectedOptions.isNotEmpty() &&
                state.selectedOptions.toSet() == correctOptions.toSet()
        return if (isCorrect) {
            state.copy(showResult = true)
        } else {
            state.copy(
                selectedOptions = emptyList(),
                textAnswer = "",
                showResult = false,
                answerTime = 0L
            )
        }
    }

    suspend fun redo(
        fileName: String,
        progressById: Map<String, PracticeProgress>,
        onSuccess: (() -> Unit)? = null,
        onProgressUpdated: (Map<String, Int>) -> Unit,
        onMessage: (LocalizedResult) -> Unit
    ) {
        val progress = preferredHomePracticeProgress(
            fileName = fileName, progressById = progressById
        ) ?: run { onSuccess?.invoke(); return }

        val fileQuestions = getQuestionsUseCase(fileName).first()
        val questionById = fileQuestions.associateBy { it.id }

        val sourceStateMap: Map<Int, UnifiedQuestionState> = if (progress.questionStateMap.isNotEmpty()) {
            progress.questionStateMap
        } else {
            val fallbackOrder = if (progress.fixedQuestionOrder.isNotEmpty()) {
                progress.fixedQuestionOrder
            } else {
                fileQuestions.map { it.id }
            }
            fallbackOrder.mapIndexed { index, questionId ->
                questionId to UnifiedQuestionState(
                    questionId = questionId,
                    selectedOptions = progress.selectedOptions.getOrElse(index) { emptyList() },
                    textAnswer = "",
                    showResult = progress.showResultList.getOrElse(index) { false },
                    analysis = progress.analysisList.getOrElse(index) { "" },
                    sparkAnalysis = progress.sparkAnalysisList.getOrElse(index) { "" },
                    baiduAnalysis = progress.baiduAnalysisList.getOrElse(index) { "" },
                    note = progress.noteList.getOrElse(index) { "" },
                    answerTime = 0L
                )
            }.toMap()
        }

        val updatedStateMap = sourceStateMap.mapValues { (questionId, state) ->
            val question = questionById[questionId]
            if (question == null) state else applyRedoRule(question, state)
        }

        val originalOrder = if (progress.fixedQuestionOrder.isNotEmpty()) {
            progress.fixedQuestionOrder
        } else {
            fileQuestions.map { it.id }
        }

        val fixedOrder = originalOrder.filter { questionId ->
            val question = questionById[questionId] ?: return@filter false
            val state = updatedStateMap[questionId] ?: return@filter false
            if (QuestionTypes.isFill(question.type)) {
                !isFillAnswerCorrect(state.textAnswer, resolveFillCorrectAnswer(question))
            } else {
                val correctOptions = answerLettersToIndices(question.answer)
                state.selectedOptions.toSet() != correctOptions.toSet() || !state.showResult
            }
        }

        if (fixedOrder.isEmpty()) {
            onSuccess?.invoke()
            return
        }

        val updatedProgress = progress.copy(
            currentIndex = 0,
            fixedQuestionOrder = fixedOrder,
            selectedOptions = fixedOrder.map { updatedStateMap[it]?.selectedOptions ?: emptyList() },
            showResultList = fixedOrder.map { updatedStateMap[it]?.showResult ?: false },
            analysisList = fixedOrder.map { updatedStateMap[it]?.analysis ?: "" },
            sparkAnalysisList = fixedOrder.map { updatedStateMap[it]?.sparkAnalysis ?: "" },
            baiduAnalysisList = fixedOrder.map { updatedStateMap[it]?.baiduAnalysis ?: "" },
            noteList = fixedOrder.map { updatedStateMap[it]?.note ?: "" },
            timestamp = System.currentTimeMillis(),
            questionStateMap = updatedStateMap
        )

        savePracticeProgressUseCase(updatedProgress)
        onProgressUpdated(mapOf(fileName to 0))
        onSuccess?.invoke()
    }
}


