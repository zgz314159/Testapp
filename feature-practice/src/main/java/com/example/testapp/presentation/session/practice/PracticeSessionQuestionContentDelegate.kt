package com.example.testapp.presentation.session.practice

import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.updateAt
import com.example.testapp.domain.usecase.PracticeUseCaseFacade
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/** 题库内容就地编辑（非 Dialog 流程） */
internal class PracticeSessionQuestionContentDelegate(
    private val facade: PracticeUseCaseFacade,
    private val sessionState: MutableStateFlow<PracticeSessionState>,
    private val scope: CoroutineScope,
    private val saveProgress: () -> Unit,
) {
    fun updateQuestionContent(
        index: Int,
        newContent: String,
    ) {
        val currentState = sessionState.value
        if (index !in currentState.questionsWithState.indices) return
        val updated =
            currentState.updateAt(index) { qws ->
                qws.copy(question = qws.question.copy(content = newContent))
            }
        sessionState.value = updated
        persistQuestionFile(updated.questionsWithState[index].question, updated, mergeById = false)
    }

    fun updateQuestionAllFields(
        index: Int,
        newContent: String,
        newOptions: List<String>,
        newAnswer: String,
        newExplanation: String,
    ) {
        val currentState = sessionState.value
        if (index !in currentState.questionsWithState.indices) return
        val updated =
            currentState.updateAt(index) { qws ->
                qws.copy(
                    question =
                        qws.question.copy(
                            content = newContent,
                            options = newOptions,
                            answer = newAnswer,
                            explanation = newExplanation,
                        ),
                )
            }
        sessionState.value = updated
        val updatedQuestion = updated.questionsWithState[index].question
        persistQuestionFile(updatedQuestion, updated, mergeById = true) {
            sessionState.value =
                sessionState.value.updateAt(index) { qws ->
                    qws.copy(question = updatedQuestion)
                }
            saveProgress()
        }
    }

    fun clearExplanation(
        index: Int,
        question: Question,
    ) {
        val currentState = sessionState.value
        if (index !in currentState.questionsWithState.indices) return
        val updated =
            currentState.updateAt(index) { qws ->
                qws.copy(question = qws.question.copy(explanation = ""))
            }
        sessionState.value = updated
        scope.launch {
            val fileName = question.fileName ?: "default.json"
            val questionsToSave =
                updated.questionsWithState
                    .map { it.question }
                    .filter { it.fileName == fileName }
            val existingQuestions = facade.questions.get(fileName).firstOrNull() ?: emptyList()
            if (existingQuestions.isNotEmpty()) {
                facade.questions.save(fileName, questionsToSave)
            }
        }
    }

    private fun persistQuestionFile(
        updatedQuestion: Question,
        state: PracticeSessionState,
        mergeById: Boolean,
        onSaved: () -> Unit = {},
    ) {
        scope.launch {
            val fileName = updatedQuestion.fileName ?: "default.json"
            val existingQuestions = facade.questions.get(fileName).firstOrNull() ?: emptyList()
            if (existingQuestions.isEmpty()) return@launch
            val toSave =
                if (mergeById) {
                    existingQuestions.map { q ->
                        if (q.id == updatedQuestion.id) updatedQuestion else q
                    }
                } else {
                    state.questionsWithState.map { it.question }.filter { it.fileName == fileName }
                }
            facade.questions.save(fileName, toSave)
            onSaved()
        }
    }
}
