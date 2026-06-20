package com.example.testapp.presentation.screen.practice

import com.example.testapp.core.util.extractSourceQuestionId
import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.usecase.PracticeUseCaseFacade
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PracticeQuestionEditCoordinator(
    private val facade: PracticeUseCaseFacade,
    private val sessionState: MutableStateFlow<PracticeSessionState>
) {
    private val _editableQuestion = MutableStateFlow<Question?>(null)
    val editableQuestion: StateFlow<Question?> = _editableQuestion.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    fun prepareEditableQuestion(questionId: Int) {
        _editableQuestion.value = sessionState.value.questionsWithState
            .firstOrNull { it.question.id == questionId }
            ?.question
    }

    fun clearEditableQuestion() {
        _editableQuestion.value = null
        _saveSuccess.value = false
    }

    suspend fun saveEditedQuestion(edited: Question): Boolean {
        val result = facade.questions.save(edited.fileName ?: "edited", listOf(edited))
        _saveSuccess.value = result.isSuccess
        return result.isSuccess
    }

    fun indexOfQuestionBySourceId(sourceId: Int?): Int {
        if (sourceId == null) return -1
        return sessionState.value.questionsWithState.indexOfFirst {
            extractSourceQuestionId(it.question.id) == sourceId
        }
    }
}
