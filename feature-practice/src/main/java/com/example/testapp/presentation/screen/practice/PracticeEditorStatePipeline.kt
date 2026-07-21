package com.example.testapp.presentation.screen.practice

import com.example.testapp.core.util.splitFillAnswerParts
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState
import com.example.testapp.uicommon.util.buildEditableFillAnswer
import com.example.testapp.uicommon.util.countEditableFillBlanks
import com.example.testapp.uicommon.util.syncEditableFillAnswers

internal object PracticeEditorStatePipeline {
    data class Deletion(
        val state: PracticeSessionState,
        val removed: QuestionWithState?
    )

    fun addOption(state: PracticeSessionState, index: Int): PracticeSessionState =
        updateQuestion(state, index) { question ->
            question.copy(options = question.options.orEmpty() + "")
        }

    fun removeOption(state: PracticeSessionState, index: Int): PracticeSessionState =
        updateQuestion(state, index) { question ->
            val options = question.options.orEmpty()
            question.copy(options = if (options.size > 1) options.dropLast(1) else options)
        }

    fun updateOption(
        state: PracticeSessionState,
        questionIndex: Int,
        optionIndex: Int,
        newText: String
    ): PracticeSessionState =
        updateQuestion(state, questionIndex) { question ->
            if (optionIndex !in question.options.indices) {
                question
            } else {
                question.copy(
                    options = question.options.mapIndexed { index, option ->
                        if (index == optionIndex) newText else option
                    }
                )
            }
        }

    fun updateContent(
        state: PracticeSessionState,
        index: Int,
        content: String
    ): PracticeSessionState =
        updateQuestion(state, index) { it.copy(content = content) }

    fun updateAnswer(
        state: PracticeSessionState,
        index: Int,
        answer: String
    ): PracticeSessionState =
        updateQuestion(state, index) { it.copy(answer = answer) }

    fun updateExplanation(
        state: PracticeSessionState,
        index: Int,
        explanation: String
    ): PracticeSessionState =
        updateQuestion(state, index) { it.copy(explanation = explanation) }

    fun updateAllFields(
        state: PracticeSessionState,
        index: Int,
        content: String,
        options: List<String>,
        answer: String,
        explanation: String
    ): PracticeSessionState =
        updateQuestion(state, index) {
            it.copy(
                content = content,
                options = options,
                answer = answer,
                explanation = explanation
            )
        }

    fun delete(state: PracticeSessionState, index: Int): Deletion {
        if (index !in state.questionsWithState.indices) return Deletion(state, null)
        val updated = state.questionsWithState.toMutableList()
        val removed = updated.removeAt(index)
        return Deletion(state.copy(questionsWithState = updated), removed)
    }

    fun syncEditedQuestion(
        questionState: QuestionWithState,
        displayedQuestion: Question,
        sourceQuestion: Question
    ): QuestionWithState {
        if (QuestionTypes.isFill(sourceQuestion.type)) {
            val blankCount = countEditableFillBlanks(displayedQuestion.content).coerceAtLeast(1)
            val syncedText =
                buildEditableFillAnswer(
                    syncEditableFillAnswers(
                        splitFillAnswerParts(questionState.textAnswer),
                        blankCount
                    )
                )
            val hasAnswer = syncedText.isNotBlank()
            return questionState.copy(
                question = displayedQuestion,
                textAnswer = syncedText,
                selectedOptions = if (hasAnswer) listOf(-1) else emptyList(),
                showResult = questionState.showResult && hasAnswer,
                sessionAnswerTime = if (hasAnswer) questionState.sessionAnswerTime else 0L
            )
        }

        val selectedOptions =
            questionState.selectedOptions.distinct().filter { it in displayedQuestion.options.indices }
        val hasAnswer = selectedOptions.isNotEmpty()
        return questionState.copy(
            question = displayedQuestion,
            selectedOptions = selectedOptions,
            showResult = questionState.showResult && hasAnswer,
            sessionAnswerTime = if (hasAnswer) questionState.sessionAnswerTime else 0L
        )
    }

    private inline fun updateQuestion(
        state: PracticeSessionState,
        index: Int,
        transform: (Question) -> Question
    ): PracticeSessionState {
        if (index !in state.questionsWithState.indices) return state
        return state.copy(
            questionsWithState = state.questionsWithState.mapIndexed { currentIndex, questionState ->
                if (currentIndex == index) {
                    questionState.copy(question = transform(questionState.question))
                } else {
                    questionState
                }
            }
        )
    }
}
