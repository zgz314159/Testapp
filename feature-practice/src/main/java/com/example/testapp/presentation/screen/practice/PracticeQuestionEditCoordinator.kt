package com.example.testapp.presentation.screen.practice

import com.example.testapp.core.util.extractSourceQuestionId
import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.usecase.PracticeUseCaseFacade
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class PracticeQuestionEditCoordinator(
    private val facade: PracticeUseCaseFacade,
    private val sessionState: MutableStateFlow<PracticeSessionState>,
    private val scope: CoroutineScope,
) {
    private val _editableQuestion = MutableStateFlow<Question?>(null)
    val editableQuestion: StateFlow<Question?> = _editableQuestion.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    /**
     * 编辑必须回查题库原题：动态填空/原子题库会话里展示的是变体——
     * 多变体时为派生负 id，单变体时沿用源题正 id，但两种情况答案都可能
     * 已被剥掉「【标签】【N分】」，直接编辑会丢失属性标签与分数值。
     */
    fun prepareEditableQuestion(questionId: Int) {
        val displayed = sessionState.value.questionsWithState
            .firstOrNull { it.question.id == questionId }
            ?.question
        _editableQuestion.value = displayed
        val question = displayed ?: return
        val sourceId = extractSourceQuestionId(question.id)
        scope.launch {
            val fileName = question.fileName ?: return@launch
            val source = facade.questions.get(fileName).firstOrNull()
                .orEmpty()
                .firstOrNull { it.id == sourceId }
            if (source != null && _editableQuestion.value?.id == question.id) {
                _editableQuestion.value = source
            }
        }
    }

    fun clearEditableQuestion() {
        _editableQuestion.value = null
        _saveSuccess.value = false
    }

    /** 合并写回源文件，禁止整文件覆盖成单题。 */
    suspend fun saveEditedQuestion(edited: Question): Boolean {
        val fileName = edited.fileName ?: "edited"
        val sourceId = extractSourceQuestionId(edited.id)
        val existing = facade.questions.get(fileName).firstOrNull().orEmpty()
        val merged = if (existing.any { it.id == sourceId }) {
            existing.map { if (it.id == sourceId) edited.copy(id = sourceId) else it }
        } else {
            existing + edited
        }
        val result = facade.questions.save(fileName, merged)
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
